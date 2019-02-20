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
        //自定义咖啡厅参数
        CoffeeHourse coffeeHourse = new CoffeeHourse()
                .setMaxSeatNumber(50)
                .setMaxOrderNumber(20)
                .setMaxChefNumber(10)
                .setMaxTime(1000);//单位为SECOND
        //也可以直接使用默认参数(无参构造函数)
//        CoffeeHourse coffeeHourse = new CoffeeHourse();

        //咖啡厅开门营业
        executorService.execute(() -> {
            //开始营业
            coffeeHourse.start();
        });

        //这是是确保先开门营业
        TimeUnit.SECONDS.sleep(2);

        //模拟顾客进入咖啡厅
        for (int i = 0; i < 1000; ++i) {
            executorService.execute(new CustomerOrderTask(coffeeHourse, "顾客:" + i));
        }
    }
}


