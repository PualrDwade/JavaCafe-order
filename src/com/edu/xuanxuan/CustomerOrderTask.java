package com.edu.xuanxuan;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 顾客任务,进入咖啡厅点咖啡喝,喝完之后离开咖啡厅
 */
public class CustomerOrderTask implements Runnable {

    private String name;

    //维持咖啡厅的引用
    private CoffeeHourse coffeeHourse;

    //注入咖啡厅实体
    CustomerOrderTask(CoffeeHourse coffeeHourse, String name) {
        this.coffeeHourse = coffeeHourse;
        this.name = name;
    }

    @Override
    public void run() {
        //首先排队,找到位置之后坐下(阻塞)
        coffeeHourse.addCustomer();
        //坐下之后发起咖啡订单并排队等候咖啡
        Future<Coffee> coffeeFuture = coffeeHourse.addOrder(new Order(Coffee.random(Coffee.class), this));
        try {
            Coffee coffee = coffeeFuture.get();
            System.out.println(name + "拿到咖啡:" + coffee);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //拿到咖啡离开
        System.out.println(name + "离开");
        coffeeHourse.removeCustomer();
    }

    @Override
    public String toString() {
        return name;
    }
}
