package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.exception.NoStockException;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareLockVo;
import com.rabbitmq.client.Channel;
import jdk.net.SocketFlow;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sun.util.resources.ga.LocaleNames_ga;

import javax.annotation.Resource;

//@RabbitListener(queues = "stock.release.stock.queue")
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;

    @Autowired
    WareSkuService wareSkuService;


    /**
     * 库存解锁
     *  1.库存自动解锁
     *      下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚，之前的锁定库存就需要自动解锁
     *      (此情况下库存工作单还在，可以按库存工作单信息与MQ信息解锁)
     *  2.订单失败由于锁库存失败导致 ==》 此情况下锁库存的所有操作都回滚，此时就没有锁库存的工作单
     *
     * 只要解锁失败，一定要告诉服务器，解锁失败 还要将消息重新发会MQ  ===》 MQ 一直处理失败-->一直发送 --> 死循环  ？？？
     *
     *
     */
   /* @RabbitHandler
   public void handleStockLockedRelease(Message message, StockLockedTo to, Channel channel){

       System.out.println("收到库存信息....");
       Long taskId = to.getTaskId();//库存工作单id
       StockDetailTo detail = to.getDetail();
       Long detailId = detail.getId();
       //解锁
       //1.解锁前需要去数据库查询关于这个订单锁定的库存信息
       //有：只能证明库存锁定成功了
       //       解锁：解锁还要看订单情况
       //           1.没有这个订单，必须解锁
       //           2.有这个订单，还需判断该订单状态
       //               订单状态：已取消：解锁库存
       //                        没取消：不能解锁
       //没有：库存锁定失败了，库存进行了回滚，这种情况无需解锁
       WareOrderTaskDetailEntity w = wareOrderTaskDetailEntity.getById(detailId);
       if(w != null){
           //解锁
           WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getById(taskId);
           //查看订单情况
           String orderSn = wareOrderTaskEntity.getOrderSn();
           R r = orderFeignService.getOrderStatus(orderSn);
           if(r.getCode() == 0){
               //调用成功
               Integer  status= r.getData(new TypeReference<Integer>() {
               });
               //status==null 没有改订单，说明应某些情况订单服务回滚了   status==4 订单被取消  这两种情况都需要解锁订单
               if(status == null || status == 4){
                   //解锁订单业务
                   unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum());
                   //TODO 当解锁完成开启手动签收
                   try {
                       channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
           }else{
               //远程调用失败
               //TODO 此时不能一定要防止订单被签收 要将处理失败的消息重新放回MQ 中
               try {
                   channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }

       }else{
           //无需解锁
           try {
               channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
   }*/

    /**
     * 延时的
     * @param to
     */
    @Override
    public void releaseStockLock(StockLockedTo to) {
        Long taskId = to.getTaskId();//库存工作单id
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();

        WareOrderTaskDetailEntity w = wareOrderTaskDetailService.getById(detailId);
        if(w != null){
            //解锁
            WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getById(taskId);
            //查看订单情况
            String orderSn = wareOrderTaskEntity.getOrderSn();
            R r = orderFeignService.getOrderStatus(orderSn);
            if(r.getCode() == 0){
                //调用成功
                Integer status= r.getData(new TypeReference<Integer>() {
                });
                //status==null 没有改订单，说明应某些情况订单服务回滚了   status==4 订单被取消  这两种情况都需要解锁订单
                if(status == null || status == 4){
                    //解锁订单业务
                    if(w.getLockStatus() == 1){
                        //只有当前工作单状态为 1(未解锁) 时才需要解锁
                        unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum(),detailId);
                        //TODO 当解锁完成开启手动签收
                    }
                }
            }else{
                //远程调用失败
                //TODO 此时不能一定要防止订单被签收 要将处理失败的消息重新放回MQ 中
               throw new RuntimeException("远程调用失败");
            }

        }else{
            //无需解锁
        }

    }

    /**
     * 订单取消的
     * @param to
     */
//    @Transactional
    @Override
    public void releaseStockLock(OrderTo to) {

        //通过订单号获取锁订单任务
        WareOrderTaskEntity wareTask = wareOrderTaskService.getOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", to.getOrderSn()));
        List<WareOrderTaskDetailEntity> wareTaskDetails = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().
                eq("task_id", wareTask.getId()).
                eq("lock_status", 1));

        //解锁
        for (WareOrderTaskDetailEntity detail : wareTaskDetails) {
            unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum(),detail.getId());
        }
    }

    @Transactional(propagation = Propagation.REQUIRED )
    public void unLockStock(Long skuId, Long wareId, Integer num, Long detailId){
//
//       UPDATE `wms_ware_sku`
//       SET
//       stock_locked = stock_locked - 数量
//       WHERE
//       sku_id = ? AND ware_id = ?
      baseMapper.unLockStock(skuId,wareId,num);

      //更新库存工作单的状态
       WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
       entity.setId(detailId);
       entity.setLockStatus(2);
       wareOrderTaskDetailService.updateById(entity);

   }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();

        /**
         * skuId: 1
         * wareId: 1
         */
        String skuId = (String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId)){
            queryWrapper.eq("sku_id",skuId);
        }

        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId)){
            queryWrapper.eq("ware_id",wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //判断如果还没有这条库存记录就插入
        List<WareSkuEntity> entities = this.baseMapper.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(entities == null || entities.size() == 0){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);

            try {
                R info = productFeignService.info(skuId);

                if(info.getCode() == 0){
                    Map<String,Object>  skuInfo = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            }catch (Exception e){}


            this.baseMapper.insert(wareSkuEntity);
        }else{

            this.baseMapper.addStock(skuId,wareId,skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> hasStock(List<Long> skuIds) {

        List<SkuHasStockVo> skuHasStockVos = skuIds.stream().map(id -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();

            Long stock = baseMapper.hasStock(id);

            skuHasStockVo.setId(id);
            skuHasStockVo.setHasStock(stock == null ? false : stock > 0 );

            return skuHasStockVo;
        }).collect(Collectors.toList());


        return skuHasStockVos;
    }

    /**
     * 为某个订单锁定库存  ==》 按照下单的收货地址，找到一个就近仓库，锁定库存
     * 默认只要时运行时异常都会回滚
     *
     * 库存解锁场景
     *  1.下订单成功，订单过期没有支付被系统自动取消，或被用户手动取消，都要解锁库存
     *  2.下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚，那么之前锁定的库存就需要自动解锁
     *
     * @param lockVo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareLockVo lockVo) {

        /**
         * 开始锁定库存
         * 1.保存库存中作但详情
         *  用来追溯
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(lockVo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);


        String orderSn = lockVo.getOrderSn();
        List<WareLockVo.LockInfo> infoList = lockVo.getInfoList();
        List<LockStockVo> lockStockVoList = infoList.stream().map(info -> {
            LockStockVo lockStockVo = new LockStockVo();
            lockStockVo.setSkuId(info.getSkuId());
            lockStockVo.setNum(info.getNum());
            //通过skuid 查找有库存的厂库id
            List<Long> wIds = this.baseMapper.findHsstockWare(info.getSkuId());
            lockStockVo.setWareId(wIds);
            return lockStockVo;
        }).collect(Collectors.toList());


        for (LockStockVo lockStockVo : lockStockVoList) {
            boolean flag = false;
            Integer num = lockStockVo.getNum();
            Long skuId = lockStockVo.getSkuId();
            List<Long> wareIds = lockStockVo.getWareId();

            if (wareIds == null || wareIds.size() == 0) {
                //没有任何厂库有库存或足够的库存
                throw new NoStockException(skuId);
            }

            //有库存
            //1.如果每个商品都锁定成功，将当前商品锁定了几件的工作单记录发送给 MQ
            //2.锁定失败，前面保存的工作单信息就回滚了，发送出去的消息，即使要解锁，由于在数据库中也查不到id，所以也就没有必要解锁
            //  1:1-2-1  2:2-3-1  3:3-1-2(x) 3号错误 1 2 3都要回滚 这样库存锁定了但没有锁定库存的工作单详情 所以发送给MQ的信息必须包含库存订单详情  防止回滚后找不到数据
            for (Long wareId : wareIds) {
                Long stock = this.baseMapper.lockStock(skuId, wareId, num);
                if (stock == 1L) {
                    //锁库存成功 退出
                    flag = true;
                    //TODO 库存锁定成功，需要给 MQ 发送消息
                    //保存锁库存详情
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
//                    taskDetailEntity.setId();
                    taskDetailEntity.setTaskId(wareOrderTaskEntity.getId());
                    taskDetailEntity.setSkuId(skuId);
                    taskDetailEntity.setSkuNum(num);
                    taskDetailEntity.setWareId(wareId);
                    taskDetailEntity.setLockStatus(1);
                    wareOrderTaskDetailService.save(taskDetailEntity);
                    /**
                     * 发送锁定库存的信息给MQ
                     * 那个任务(taskId) 的那个 sku 在那个厂库(wareid) 下锁定了多少库存
                     */
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setTaskId(wareOrderTaskEntity.getId());
                    StockDetailTo detailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity,detailTo);
                    stockLockedTo.setDetail(detailTo);
                    //String exchange, String routingKey, Object object
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",stockLockedTo);
                    // ？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？ ==》 监听解锁
                    break;
                } else {
                    //当前厂库 锁库存失败 重试下一个厂库
                }
            }

            if (!flag) {
                //当前商品所有仓库都没有锁住
                throw new NoStockException(skuId);
            }

        }
        //能够到这就表明一定锁库存成功
        return true;
    }



    @Data
    class LockStockVo{
        Integer num;
        Long skuId;
        List<Long> wareId;
    }



}