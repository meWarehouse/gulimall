package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author zero
 * @create 2020-09-10 22:58
 */
@Controller
public class IndexController {

    @Resource
    CategoryService categoryService;

    @Resource
    RedissonClient redisson;


    @Resource
    StringRedisTemplate stringRedisTemplate;

    //index/json/catalog.json
    @ResponseBody
    @GetMapping("/index/js/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson(){
        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }

    @GetMapping({"/","/index.html"})
     public String indexPage(Model model){

        //查出一级菜单的数据
        List<CategoryEntity> entities = categoryService.searchLevel1();
        model.addAttribute("categorys",entities);

        // classpath:/templates/ .html
        return "index";

     }

     @ResponseBody
     @GetMapping("/hello")
     public String hello(){

//         RBloomFilter<Object> bloomFilter = redisson.getBloomFilter();

         ////1、获取一把锁，只要锁的名字一样，就是同一把锁
         RLock lock = redisson.getLock("my-lock");

         //加锁
         lock.lock(); //阻塞式等待，默认加的锁都是30s时间
         /**
          * 问题：在锁到了以后，不会自动续期
          *     1、锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s
          *     2、加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后删除
          */

//         lock.lock(10,TimeUnit.SECONDS); //10秒自动解锁，自动解锁时间一定要大于业务的执行时间。
         /**
          * 问题：在锁到了以后，不会自动续期
          *     1. 如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是我们指定的时间。
          *     2. 如果我们未指定锁的超时时间，就使用30*1000【lockWatchdogTimeout看门狗的默认时间】
          *         只要占锁成功，就会启动一个定时任务[重新给锁设定过期时间，新的过期时间默认就是看门狗时间] 每隔十秒就会自动续期，续成30s
          *         internalLockLeaseTime【看门狗时间】 / 3,
          */

         //最佳实战：
         //1、lock.lock(10, TimeUnit.SECONDS); 省掉了整个续期操作，手动解锁

         try {
             System.out.println("获取到锁，开始执行任务。。。。："+Thread.currentThread().getId());
             try{ TimeUnit.SECONDS.sleep(30); }catch( InterruptedException e ){ e.printStackTrace(); }
         }catch (Exception e){
             e.printStackTrace();
         }finally {
             //解锁
             System.out.println("释放锁。。。。"+Thread.currentThread().getId());
             lock.unlock();
         }


         return "hello";
     }

    /**
     * 读写锁
     *  保证一定能读到最新数据，修改期间，写锁是一个排他锁（互斥锁）。读锁是一个共享锁，写锁没释放，读就必须等待
     */
    @ResponseBody
    @GetMapping("/write")
    public String write(){

        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");

        String uuid = "";

        readWriteLock.writeLock().lock();
        try {
            System.out.println("写锁加锁成功...."+Thread.currentThread().getId());

            uuid = UUID.randomUUID().toString();
            try{ TimeUnit.SECONDS.sleep(20); }catch( InterruptedException e ){ e.printStackTrace(); }
            stringRedisTemplate.opsForValue().set("writeValue",uuid);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            System.out.println("写锁释放...."+Thread.currentThread().getId());
            readWriteLock.writeLock().unlock();
        }



        return uuid;

    }

    @ResponseBody
    @GetMapping("/read")
    public String read(){

        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");

        String writeValue = "";

        readWriteLock.readLock().lock();
        try {

            System.out.println("读锁加锁成功..."+Thread.currentThread().getId());

            writeValue = stringRedisTemplate.opsForValue().get("writeValue");
            try{ TimeUnit.SECONDS.sleep(30); }catch( InterruptedException e ){ e.printStackTrace(); }


        }catch (Exception e){
            e.printStackTrace();
        }finally {
            System.out.println("读锁释放...."+Thread.currentThread().getId());
            readWriteLock.readLock().unlock();
        }

        return writeValue;
    }

    /**
     *  信号量（Semaphore)   https://github.com/redisson/redisson/wiki/8.-%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81%E5%92%8C%E5%90%8C%E6%AD%A5%E5%99%A8
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {

        RSemaphore park = redisson.getSemaphore("park");

        park.acquire();

        return "ok";

    }

    @GetMapping("/go")
    @ResponseBody
    public String go(){
        RSemaphore park = redisson.getSemaphore("park");
        park.release();

        return "gogo";
    }

    @GetMapping("/lockdoor")
    @ResponseBody
     public String lockDoor() throws InterruptedException {

        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);

        door.await();

        return "放假了....";
     }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
     public String gogogo(@PathVariable("id") Long id){
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();

        return id + "几班的人走了。。。。。。";
     }








}
