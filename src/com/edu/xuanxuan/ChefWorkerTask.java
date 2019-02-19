package com.edu.xuanxuan;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 厨师工作任务:接受顾客订单,
 * 按照定订单内容制作咖啡,
 * 完成之后将咖啡放置到带取区,通知用户来取
 */
public class ChefWorkerTask implements Runnable {

    private volatile boolean work;

    public boolean isWork() {
        return work;
    }

    public void setWork(boolean work) {
        this.work = work;
    }

    //维持对咖啡厅的引用
    private CoffeeHourse coffeeHourse;

    //注入咖啡厅
    ChefWorkerTask(CoffeeHourse coffeeHourse) {
        this.coffeeHourse = coffeeHourse;
    }

    //厨师消耗订单,制作咖啡Task
    @Override
    public void run() {
        work = true;
        System.out.println("厨师开始工作!");
        while (work) {
            try {
                //得到一个订单(该方法会阻塞),直到有新的订单
                Order order = coffeeHourse.takeOrder();
                //判断咖啡类型
                Coffee coffee = order.getCoffee();
                switch (coffee) {
                    case GOUSHI:
                        System.out.println("正在制作生产goushi咖啡");
                        TimeUnit.SECONDS.sleep(3);
                        break;
                    case MAOSHI:
                        System.out.println("正在制作生产maoshi咖啡");
                        TimeUnit.SECONDS.sleep(4);
                        break;
                    case NIUSHI:
                        System.out.println("正在制作生产niushi咖啡");
                        TimeUnit.SECONDS.sleep(5);
                        break;
                    case XIANGSHI:
                        System.out.println("正在制作生产xiangshi咖啡");
                        TimeUnit.SECONDS.sleep(6);
                        break;
                    default:
                        break;
                }
                //添加进入咖啡
                coffeeHourse.addCoffee(order);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("厨师停止工作");
    }
}
