package com.edu.xuanxuan;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 一个咖啡厅有固定数量的座位,顾客排队点餐,
 * 几个厨师接单,做不同的咖啡,各个咖啡用时不同
 * 咖啡完成,通知用户
 * 不使用阻塞队列,全部使用wait-notify实现
 * 多线程模拟此场景
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        CoffeeHourse coffeeHourse = new CoffeeHourse().setMaxSeatNumber(20).setMaxOrderNumber(10).setMaxChefNumber(3);
        //咖啡厅开门营业
        executorService.execute(() -> {
            //开始营业
            coffeeHourse.start();
            try {
                TimeUnit.SECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //关闭营业
            coffeeHourse.shutDown();
        });
        TimeUnit.SECONDS.sleep(2);
        //客户进入咖啡厅(当天有1000个顾客来喝咖啡)
        for (int i = 0; i < 100; ++i) {
            executorService.execute(new CustomerOrderTask(coffeeHourse, "顾客:" + i));
        }
    }
}


