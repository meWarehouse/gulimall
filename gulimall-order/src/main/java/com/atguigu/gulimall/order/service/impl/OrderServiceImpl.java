package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.OrderConstant;
import com.atguigu.common.to.SeckillOrderTo;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemeberRespVo;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.exception.NoStockException;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.OrderInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    ExecutorService executor;

    @Autowired
    StringRedisTemplate redisTemplate;

    private ThreadLocal<OrderSubmitVo> ordersubmit = new ThreadLocal<>();

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderItemService orderItemService;


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    PaymentInfoService paymentInfoService;




    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo getConfirmInfo() throws ExecutionException, InterruptedException {

        MemeberRespVo member = OrderInterceptor.threadLocal.get();
        System.out.println("主线程 线程："+Thread.currentThread().getId());

        //异步 feign 调用丢失上下文
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        OrderConfirmVo confirmVo = new OrderConfirmVo();

        CompletableFuture<Void> addressTask = CompletableFuture.runAsync(() -> {
            System.out.println("addressTask 线程："+Thread.currentThread().getId());
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> memberAddress = memberFeignService.getMemberAddressByMemberId(member.getId());
            confirmVo.setAddress(memberAddress);
        }, executor);


        //feign 在远程调用之前要调用构造请求，调用很多的拦截器
        //RequestInterceptor
        CompletableFuture<Void> itemsTask = CompletableFuture.runAsync(() -> {
            System.out.println("itemsTask 线程："+Thread.currentThread().getId());
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> currentUserCartitems = cartFeignService.getCurrentUserCartitems();
            confirmVo.setItems(currentUserCartitems);
        }, executor).thenRunAsync(()->{
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            //TODO 远程批量查询有无库存
            R r = wareFeignService.hasStock(collect);
            if(r.getCode() != 0){
                Map<Long, Boolean> collect1 = collect.stream().collect(Collectors.toMap(c -> c, c -> false));
                confirmVo.setHasStock(collect1);
            }else{
                List<HasStockVo> data = r.getData(new TypeReference<List<HasStockVo>>(){});
                Map<Long, Boolean> collect1 = data.stream().collect(Collectors.toMap(HasStockVo::getId, HasStockVo::getHasStock));
                confirmVo.setHasStock(collect1);
            }


        },executor);

        CompletableFuture<Void> integrationTask = CompletableFuture.runAsync(() -> {
            confirmVo.setIntegration(member.getIntegration());
        }, executor);

        //TODO 防重令牌  order:token:userId==>uuid
        String token = UUID.randomUUID().toString().replace("-", "");
        //给redis 中存储一份
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+member.getId(),token,30, TimeUnit.MINUTES);
        //给页面存储一份
        confirmVo.setOrderTocken(token);

        CompletableFuture.allOf(addressTask,itemsTask,integrationTask).get();

        return confirmVo;
    }

    /**
     * 事务传播
     * 事务是使用代理来控制
     */
    @Transactional(timeout = 30) // a 事务的所有设置会传播到和它共用一个事务的方法 与a事务共用的b事务的所有设置都无效
    public void a(){
        //在同一个对象内事务方法互调默认失效，原因，绕过了动态代理
//        b(); //a事务 ============》没用
//        c(); //c事务 与a的不是同一个事务 a 如果发生回滚 c 不会回滚 ============》没用

        /**
         * 想要实现 b c 必须经过代理
         */
        OrderServiceImpl orderService = (OrderServiceImpl) AopContext.currentProxy();
        orderService.c();
        orderService.b();

        int i = 10/0;
    }

    //Propagation.REQUIRE b需要一个事务 如果调用b的方法也有事务，则b与其调用者使用同一个事务，b的所有事务设置都会被调用者的事务所覆盖
    @Transactional(propagation = Propagation.REQUIRED,timeout = 9)
    public void b(){

    }

    //Propagation.REQUIRES_NEW 需要一个新的事务 与其调用者不使用同一个事务
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void  c(){

    }

    /**
     *  本地事务，在分布式系统，只能控制自己的回滚无法控制其他服务的回滚
     *  分布式事务：最大的原因：网络问题+分布式机器
     *
     *  @Transactional(isolation = Isolation.REPEATABLE_READ)
     *
     */
//    @GlobalTransactional //开启 seata 全局事务  //不适合高并发场景
    @Transactional
    @Override
    public SubmitorderRespVo submitOrder(OrderSubmitVo submitVo) throws NoStockException {


        ordersubmit.set(submitVo);
        MemeberRespVo member = OrderInterceptor.threadLocal.get();

        SubmitorderRespVo respVo = new SubmitorderRespVo();
        //1.验证令牌
        String orderToken = submitVo.getOrderToken();

        //删除令牌
        //===》验证 删除令牌必须是一个原子操作 成功返回1 失败返回0
        String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                "then\n" +
                "    return redis.call(\"del\",KEYS[1])\n" +
                "else\n" +
                "    return 0\n" +
                "end";
        //execute(RedisScript<T> script, List<K> keys, Object... args);
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + member.getId()), submitVo.getOrderToken());
        if (execute == 0L) {
            //删除失败
            respVo.setCode(1);
            return respVo;
        } else {
            //删除成功 执行业务
            //下单：去创建订单，校验令牌，锁定库存...


            //1.创建订单，订单项
            OrderCreateTo order = createOrder();
            //2.检价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            if (Math.abs(payAmount.subtract(submitVo.getPayprice()).doubleValue()) < 0.01) {
                //金额对比
                //保存订单及订单项
                saveOrder(order);
                //锁定库存 有异常需要回滚
                WareLockVo wareLockVo = new WareLockVo();
                wareLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<WareLockVo.LockInfo> lockInfos = order.getOrderItem().stream().map(item -> {
                    WareLockVo.LockInfo lockInfo = new WareLockVo.LockInfo();
                    lockInfo.setNum(item.getSkuQuantity());
                    lockInfo.setSkuId(item.getSkuId());
                    return lockInfo;
                }).collect(Collectors.toList());
                wareLockVo.setInfoList(lockInfos);

                //问题：库存成功了，但是网络原因超时了，订单回滚，库存不回滚
                //为了保证高并发，库存服务自己回滚，可以发消息个库存服务；
                //库存服务本身也可以自动解锁模式 使用消息队列
                //TODO 调用厂库服务锁定库存
                R r = wareFeignService.orderLockStock(wareLockVo);
                if (r.getCode() == 0) {
                    //成功
                    respVo.setOrder(order.getOrder());

                    //TODO 模拟其他远程服务 调用失败
//                    int i = 10 /0;

                    // TODO 订单创建成功，给MQ 发送消息
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());

                    return respVo;
                } else {
                    //锁定失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
//                        return respVo;
                }
            } else {
                //对比失败
                respVo.setCode(2);
                return respVo;
            }
        }


    }

    @Override
    public Integer orderStatusByOrdersn(String orderSn) {
        OrderEntity orderEntity = baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        if(orderEntity != null){
            return orderEntity.getStatus();
        }else{

            return null;
        }
    }

    /**
     * 关单
     * @param orderEntity
     */
    @Override
    public void closeOrder(OrderEntity orderEntity) {
        //查询当前订单状态
        OrderEntity order = this.getById(orderEntity.getId());
        if(order == null){
            return;
        }
        //只有是代付款状态的才需要处理
        if(order.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()){
            //关闭订单
            OrderEntity enrty = new OrderEntity();
            enrty.setId(order.getId());
            enrty.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(enrty);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(order,orderTo);

            //TODO 关闭叮当成功在给 MQ 发送一个消息  ==》
            // 防止由于网络等原因导致关闭订单服务卡死，使得关闭订单在库存解锁时候成功
            // 这样就会使得库存无法解锁 ==》 发确定关闭订单的消息给由交换机路由到库存释放的队列进行释放库存
            rabbitTemplate.convertAndSend("order-event-exchange","order.release.other",orderTo);

        }

    }

    /**
     * 支付宝
     * @param orderSn
     * @return
     */
    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn ", orderSn));
        List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));

        payVo.setBody(orderItemEntities.get(0).getSkuAttrsVals());
        payVo.setOut_trade_no(orderSn);
        payVo.setSubject(orderItemEntities.get(0).getSkuName());
        BigDecimal bigDecimal = orderEntity.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(bigDecimal.toString());
        return payVo;
    }

    @Override
    public PageUtils listWithItem(Map<String, Object> params) {

        MemeberRespVo memeber = OrderInterceptor.threadLocal.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id",memeber.getId()).orderByDesc("id")
        );

        List<OrderEntity> records = page.getRecords();
        List<OrderEntity> collect = records.stream().map(orderEntity -> {
            List<OrderItemEntity> itemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderEntity.getOrderSn()));
            orderEntity.setItemEntity(itemEntities);
            return orderEntity;
        }).collect(Collectors.toList());

        page.setRecords(collect);

        return new PageUtils(page);
    }

    /**
     * 支付宝支付成功后的异步回调处理
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        //保存订单订单流水
        PaymentInfoEntity paymentInfo = new PaymentInfoEntity();
        paymentInfo.setOrderSn(vo.getOut_trade_no());
        paymentInfo.setAlipayTradeNo(vo.getTrade_no());
        paymentInfo.setTotalAmount(new BigDecimal(vo.getTotal_amount()));
        paymentInfo.setPaymentStatus(vo.getTrade_status());
        paymentInfoService.save(paymentInfo);

        //修改订单状态
        if("TRADE_FINISHED".equals(vo.getTrade_status()) || "TRADE_SUCCESS".equals(vo.getTrade_status())){

            this.baseMapper.updateOrderStatus(vo.getOut_trade_no(),OrderStatusEnum.PAYED.getCode());
        }
        return "success";

    }

    @Override
    public void handleSeckillOrder(SeckillOrderTo orderTo) {

        //TODO 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderTo.getOrderSn());
        orderEntity.setMemberId(orderTo.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = orderTo.getSeckillPrice().multiply(new BigDecimal(orderTo.getNum().toString()));
        orderEntity.setPayAmount(multiply);

        this.save(orderEntity);

        //TODO 保存订单项信息
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(orderTo.getOrderSn());
        itemEntity.setRealAmount(multiply);
        itemEntity.setSkuQuantity(orderTo.getNum());

        orderItemService.save(itemEntity);


    }

    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
//        List<OrderItemEntity> orderItemEntities = order.getOrderItem();

        this.save(orderEntity);

        List<OrderItemEntity> orderItemEntities = order.getOrderItem().stream().map(item -> {
            item.setOrderId(orderEntity.getId());
            return item;
        }).collect(Collectors.toList());

        orderItemService.saveBatch(orderItemEntities);

    }

    /**
     * 创建订单
     * @return
     */
    private OrderCreateTo createOrder(){
        OrderCreateTo order = new OrderCreateTo();

        //创建订单号
        String orderSn = IdWorker.getTimeId();

        //根据订单号构建订单
        OrderEntity orderEntity = buildOrder(orderSn);
        order.setOrder(orderEntity);

        //构建订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
        order.setOrderItem(orderItemEntities);

        //验价
        //计算价格信息
        computePrice(orderEntity,orderItemEntities);

//        order.setOrder(orderEntity);
//        order.setOrderItem(orderItemEntities);

        return order;
    }

    /**
     * 计算价格信息
     * @param orderEntity
     * @param orderItemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        BigDecimal promotionAmount = new BigDecimal("0.0");
        BigDecimal couponAmount = new BigDecimal("0.0");
        BigDecimal integrationAmount = new BigDecimal("0.0");
        BigDecimal realAmount = new BigDecimal("0.0");
        Integer giftGrowth = 0;
        Integer giftIntegration = 0;
        if(orderItemEntities != null && orderItemEntities.size() > 0){
            for (OrderItemEntity entity : orderItemEntities) {
                promotionAmount = promotionAmount.add(entity.getPromotionAmount());
                couponAmount = couponAmount.add(entity.getCouponAmount()) ;
                integrationAmount = integrationAmount.add(entity.getIntegrationAmount());
                //已经减去优惠后的每项的价格
                BigDecimal multiply = entity.getSkuPrice().multiply(new BigDecimal(entity.getSkuQuantity()));
                realAmount = realAmount.add(multiply);

                //积分 成长值
                giftGrowth = giftGrowth +  entity.getGiftGrowth();
                giftIntegration = giftIntegration + entity.getGiftIntegration();
            }
        }
        //订单总额
        orderEntity.setTotalAmount(realAmount);
        //应付总额
        orderEntity.setPayAmount(realAmount.add(orderEntity.getFreightAmount()));
        //运费总额
        //促销优惠
        //积分抵扣
        //优惠卷抵扣
        //价格调整优惠
        orderEntity.setDiscountAmount(new BigDecimal("0.0"));
        orderEntity.setCouponAmount(couponAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
        orderEntity.setPromotionAmount(promotionAmount);
        //运费已算

        //自动确认收货时间
        orderEntity.setAutoConfirmDay(7);
        //积分成长值
        orderEntity.setGrowth(giftGrowth);
        orderEntity.setIntegration(giftIntegration);

        //TODO 收货状态 下单成功后修改
        orderEntity.setDeleteStatus(0);

        //订单状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

    }

    /**
     * 构建订单项
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> currentUserCartitems = cartFeignService.getCurrentUserCartitems();
        if(currentUserCartitems != null && currentUserCartitems.size() > 0){
            List<OrderItemEntity> collect = currentUserCartitems.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItem(item);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return collect;
        }else{
            return null;
        }

    }

    /**
     * 构建某一个订单项
     *  1.订单信息
     *  2.商品的SPU 信息
     *  3.商品的sku信息
     *  4.优惠信息
     *  5.积分信息
     * @return
     * @param item
     */
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItem = new OrderItemEntity();

        //商品的SPU 信息
        R r = productFeignService.getSpuinfoBySkuid(item.getSkuId());
        if(r.getCode() == 0){
            SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
            });
            orderItem.setSpuBrand(data.getBrandId().toString());
            orderItem.setSpuId(data.getId());
            orderItem.setSpuName(data.getSpuName());
            orderItem.setCategoryId(data.getCatalogId());
        }


        // 3.商品的sku信息
        orderItem.setSkuId(item.getSkuId());
        orderItem.setSkuName(item.getTitle());
        orderItem.setSkuPic(item.getImage());
        orderItem.setSkuPrice(item.getPrice());
        orderItem.setSkuQuantity(item.getCount());
        String skuAttrs = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
        orderItem.setSkuAttrsVals(skuAttrs);

        //积分信息

//        orderItem.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue()/15);
//        orderItem.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue()/10);

        BigDecimal giftGrowth = item.getPrice().multiply(new BigDecimal(item.getCount().toString())).divide(new BigDecimal("15"), 2, BigDecimal.ROUND_CEILING);
        BigDecimal giftIntegration = item.getPrice().multiply(new BigDecimal(item.getCount().toString())).divide(new BigDecimal("10"), 2, BigDecimal.ROUND_CEILING);
        orderItem.setGiftGrowth(giftGrowth.intValue());
        orderItem.setGiftIntegration(giftIntegration.intValue());


        orderItem.setPromotionAmount(new BigDecimal("0.0"));
        orderItem.setCouponAmount(new BigDecimal("0.0"));
        orderItem.setIntegrationAmount(new BigDecimal(("0.0")));
        BigDecimal subtract = orderItem.getSkuPrice()
                .subtract(orderItem.getPromotionAmount())
                .subtract(orderItem.getCouponAmount())
                .subtract(orderItem.getIntegrationAmount());
//        BigDecimal multiply = subtract.multiply(new BigDecimal(item.getCount().toString()));

        orderItem.setRealAmount(subtract);

        return orderItem;
    }

    /**
     * 构建 订单
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {

        MemeberRespVo memeberRespVo = OrderInterceptor.threadLocal.get();

        OrderSubmitVo orderSubmitInfo = ordersubmit.get();


        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMemberId(memeberRespVo.getId());
        //创建订单号
        orderEntity.setOrderSn(orderSn);
        //邮费
        R fareR = wareFeignService.getFare(orderSubmitInfo.getAddrId());
        if(fareR.getCode() == 0){
            BigDecimal data = fareR.getData(new TypeReference<BigDecimal>() {
            });
            orderEntity.setFreightAmount(data);
        }
        //地址信息
        R r = memberFeignService.addressInfo(orderSubmitInfo.getAddrId());
        if(r.getCode() == 0){
            MemberAddressVo receiveAddress = r.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
            });


            orderEntity.setReceiverName(receiveAddress.getName());

            orderEntity.setReceiverPhone(receiveAddress.getPhone());

            orderEntity.setReceiverPostCode(receiveAddress.getPostCode());

            orderEntity.setReceiverProvince(receiveAddress.getProvince());

            orderEntity.setReceiverCity(receiveAddress.getCity());

            orderEntity.setReceiverRegion(receiveAddress.getRegion());

            orderEntity.setReceiverDetailAddress(receiveAddress.getDetailAddress());

        }

        //订单总额
        //运费总额
        //促销优惠
        //积分抵扣
        //优惠卷抵扣
        //价格调整优惠
//        orderEntity.setDiscountAmount(new BigDecimal("0.0"));
//        orderEntity.setCouponAmount(new BigDecimal("0.0"));
//        orderEntity.setIntegrationAmount(new BigDecimal("0.0"));
//        orderEntity.setPromotionAmount(new BigDecimal("0.0"));
        //运费已算




        return orderEntity;
    }

}