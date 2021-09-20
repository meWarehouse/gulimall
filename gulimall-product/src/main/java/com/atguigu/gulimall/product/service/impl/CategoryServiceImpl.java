package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    private List<CategoryEntity> level1Menues;

//    @Resource
//    CategoryDao categoryDao;

    @Resource
    StringRedisTemplate stringRedisTemplate;


    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Resource
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {

        //1.查出所有分类数据
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2.组装成父子的树形结构
        //2.1：找出所有的一级分类
        List<CategoryEntity> level1Menues = entities.stream().filter(categoryEntity ->
                {
                    return categoryEntity.getParentCid() * 1 == 0;
                }
        ).map((menu) -> { //menu = level1Menues
            menu.setChildren(getChildrens(menu, entities)); //为一级菜单封装子分类
            return menu;
        }).sorted((menu1, menu2) -> {
            //对map中查询出的菜单进行排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());


        return level1Menues;
    }


    @Override
    public void removeMenuByIds(List<Long> asList) {



        //TODO 检查当前菜单是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    // [2,26,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {

        List<Long> paths = new ArrayList<>();

        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[parentPath.size()]);
    }

    //递归查出所有的父节点
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {

        //225,26,2
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }

        return paths;

    }

    /**
     * 级联更新所有的数据
     * @CacheEvict:失效模式
     * 同时进行多种缓存操作@Caching
     * 指定删除分区下的所有数据@CacheEvict(value = "category", allEntries = true)
     * 存储同一类型的数据，都可以指定成同一个分区
     *
     * @param category
     */
//    @CacheEvict(value = "catagory",key = "'searchLevel1'")
//    @Caching(evict={
//            @CacheEvict(value = "catagory",key = "'searchLevel1'"),
//            @CacheEvict(value = "catagory",key = "'getCatalogJson'")
//    })
    @CacheEvict(value = "catagory",allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {

        //更新自身
        this.updateById(category);

        //更新级联链表
        categoryBrandRelationService.updateCascade(category.getCatId(), category.getName());

    }

    /**
     *
     * 1.每一个需要缓存的数据都可以指定放到指定的名字的缓存【缓存分区(按照业务类型划分)】
     * 2.@Cacheable
     *      代表当前方法的结果需要缓存，如果缓存中有，方法不会调用，如果缓存中没有，调用方法将结果放入缓存
     * 3.默认行为
     *      1），如果缓存命中，方法不调用
     *      2），key 默认自动生成；缓存名::SimpleKey [](自主生成的key值)
     *      3），缓存的value值 默认使用jdk序列化机制，将序列化后的数据存入redis
     *      4），默认的 ttl=-1
     *
     *      自定义：
     *          1.指定生成的缓存使用的key： key属性指定，接受一个SpEL
     *              @Cacheable(value = {"catagory"},key = "'searchLevel1'") ==> catagory::searchLevel1
     *          2.指定缓存数据存活时间，配置文件中的ttl
     *              spring.cache.redis.time-to-live=3600000
     *          3.将数据保存为json格式
     *
     *
     *
     * @return
     */
    @Cacheable(value = {"catagory"},key = "#root.method.name",sync = true)
    @Override
    public List<CategoryEntity> searchLevel1() {
        System.out.println("searchLevel1 ..........");
        List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", "0"));
        return entities;
    }

    @Cacheable(value = {"catagory"},key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        System.out.println("进行了数据库查询........");



        //先查出所有数据
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //查出所有一级分类
//        List<CategoryEntity> category1 = this.searchLevel1();
        List<CategoryEntity> category1 = getParent_cid(entities, 0L);

        if (category1 == null) {
            return null;
        }

        //遍历封装为 Catalog2Vo
        Map<String, List<Catalog2Vo>> result = category1.stream().collect(Collectors.toMap(l1 -> l1.getCatId().toString(), l1 -> {

            //通过 一级分类查出二级分类
            List<CategoryEntity> category2 = getParent_cid(entities, l1.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            if (category2 != null) {
                catalog2Vos = category2.stream().map(l2 -> {
                    //封装 Catalog2Vo
                    Catalog2Vo catalog2Vo = new Catalog2Vo(l1.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //通过 二级分类查出三级分类
                    List<CategoryEntity> category3 = getParent_cid(entities, l2.getCatId());

                    if (category3 != null) {
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = category3.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return catalog3Vo;

                        }).collect(Collectors.toList());

                        catalog2Vo.setCatalog3List(catalog3Vos);
                    }

                    return catalog2Vo;
                }).collect(Collectors.toList());

            }

            return catalog2Vos;
        }));

        //将的到的数据转为 json 存入一份到redis 中
        String s = JSON.toJSONString(result);
        stringRedisTemplate.opsForValue().set("catalogJson", s, 1, TimeUnit.DAYS);
        return result;
    }




        /**
         * //TODO (可能产生堆外内存溢出) OutOfDirectMemoryError
         * <p>
         * 1.spring boot2.0 以后默认使用 lettuce 作为 redis 的客户端，它使用的是 netty 进行网络传输协议 5.1.8
         * 2.lettuce 的 bug 导致netty堆外内存溢出 -Xmx100m netty 如果没有指定堆外内存默认会使用  -Xmx100m
         * 可以通过 -Dio.netty.maxDirectMemory 进行设置
         * 解决方案
         * 1.升级 lettuce 的客户端re
         * 2.切换 jedis
         *
         * @return
         */
    public Map<String, List<Catalog2Vo>> getCatalogJson1() {

        //存入 redis 的数据都以 json 格式 查询出后又将 json 转化为对象  【序列化与反序列化】

        /**
         * 缓存穿透 缓存雪崩 缓存击穿
         *
         *  1.空结果缓存，解决缓存穿透
         *  2.设置过期时间（随机时间）：解决缓存雪崩
         *  3.加锁：解决缓存击穿
         *
         */


        //1.先查询缓存中有没有 有则返回没有则查询数据库
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {

            System.out.println("缓存不命中.................查询数据库...................");
            //缓存中没有 查询数据库
            Map<String, List<Catalog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();

            return catalogJsonFromDb;


        }

        System.out.println("缓存命中1...........................");

        //缓存中有则将数据反序列化为想要的对象
        Map<String, List<Catalog2Vo>> stringListMap = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });

        return stringListMap;


    }


    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        RLock lock = redissonClient.getLock("catalogy-lock");

        lock.lock();

        Map<String, List<Catalog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }

        return dataFromDb;

    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        //TODO 本地锁 synchronized JUC(lock) 在分布式情况下，想要锁住所有，必须使用分布式锁

        //在 redis 中设置一个坑位
//        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock","111");
        String uuid = UUID.randomUUID().toString();
        // redis 的占锁 一设置锁的过期时间必须是一个原子操作 防止不确定因素导致锁无法删除形成死锁
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);

        if (lock) {


            //设置过期时间
//            stringRedisTemplate.expire("lock",30,TimeUnit.SECONDS);
            //加锁成功... 执行业务
//            Map<String, List<Catalog2Vo>> dataFromDb = getDataFromDb();
            //删除锁 为了保证删除的锁一定是属于自己的，可以个锁加一个唯一标识
//            stringRedisTemplate.delete("lock");
//            String lockValue = stringRedisTemplate.opsForValue().get("lock");
//            if(uuid.equals(lockValue)){
//                stringRedisTemplate.delete("lock");
//            }

            //获取对比值与删锁也必须是一个原子操作 redis 提供了一个 脚本
//            String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
            //执行脚本
//            Integer lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Integer>(script, Integer.class), Arrays.asList("lock"), uuid);

            System.out.println("获取锁成功。。。。。。。。。。。。。。。。。。。。");
            Map<String, List<Catalog2Vo>> dataFromDb = null;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }


            return dataFromDb;
        } else {
            //加锁失败... 重试 synchronized ()
            System.out.println("获取锁失败.........等待重试");
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();
        }


    }

    private Map<String, List<Catalog2Vo>> getDataFromDb() {

        //先判断 缓存中是否有数据
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            System.out.println("缓存命中2...........................");
            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        }

        System.out.println("进行了数据库查询........");

        //先查出所有数据
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //查出所有一级分类
//        List<CategoryEntity> category1 = this.searchLevel1();
        List<CategoryEntity> category1 = getParent_cid(entities, 0L);

        if (category1 == null) {
            return null;
        }

        //遍历封装为 Catalog2Vo
        Map<String, List<Catalog2Vo>> result = category1.stream().collect(Collectors.toMap(l1 -> l1.getCatId().toString(), l1 -> {

            //通过 一级分类查出二级分类
            List<CategoryEntity> category2 = getParent_cid(entities, l1.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            if (category2 != null) {
                catalog2Vos = category2.stream().map(l2 -> {
                    //封装 Catalog2Vo
                    Catalog2Vo catalog2Vo = new Catalog2Vo(l1.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //通过 二级分类查出三级分类
                    List<CategoryEntity> category3 = getParent_cid(entities, l2.getCatId());

                    if (category3 != null) {
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = category3.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return catalog3Vo;

                        }).collect(Collectors.toList());

                        catalog2Vo.setCatalog3List(catalog3Vos);
                    }

                    return catalog2Vo;
                }).collect(Collectors.toList());

            }

            return catalog2Vos;
        }));

        //将的到的数据转为 json 存入一份到redis 中
        String s = JSON.toJSONString(result);
        stringRedisTemplate.opsForValue().set("catalogJson", s, 1, TimeUnit.DAYS);
        return result;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithLocateLock() {

        //TODO 本地锁 synchronized JUC(lock) 在分布式情况下，想要锁住所有，必须使用分布式锁

        //本地锁
        synchronized (this) {

            //先判断 缓存中是否有数据
            String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
            if (!StringUtils.isEmpty(catalogJson)) {
                System.out.println("缓存命中2...........................");
                return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
                });
            }


            System.out.println("进行了数据库查询........");


            //先查出所有数据
            return getDataFromDb();
        }


    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> all, Long parent_cid) {
//        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", l1.getCatId()));

        return all.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());

    }


    /**
     * 获取某一个菜单的子菜单
     *
     * @param root 当前需要获取子菜单的菜单
     * @param all  所有菜单
     * @return
     */
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() * 1 == root.getCatId() * 1;
        }).map(CategoryEntity -> {
            //1.找到所有的子菜单
            CategoryEntity.setChildren(getChildrens(CategoryEntity, all));//递归查找子菜单
            return CategoryEntity;
        }).sorted((menu1, menu2) -> {
            //对map中查询出的菜单进行排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

}