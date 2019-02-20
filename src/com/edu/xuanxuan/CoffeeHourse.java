package com.edu.xuanxuan;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 咖啡厅类,作为消息中间件,内部加锁(reentrant锁)
 * 作为整体的生产-消费模型的核心
 */
public final class CoffeeHourse {
    //记录工作状态
    private volatile boolean work;
    //线程service池
    private ExecutorService executorService = Executors.newCachedThreadPool();
    //最大厨师数
    private int maxChefNumber = 3;//默认3个厨师
    //最大座位数
    private int maxSeatNumber = 10;//默认10个座位
    //当前座位数
    private volatile int seatNumber = maxSeatNumber;
    //最大订单数
    private int maxOrderNumber = 10;//默认最多10个订单
    //营业时间
    private int time = 60;//默认营业1分钟
    //订单排队(重点,客户生产订单,餐厅消费订单,建立生产消费者模型)
    private Queue<Order> orders = new LinkedList<>();
    //订单->咖啡
    private Map<Order, Coffee> orderCoffeeMap = new HashMap<>();
    //持有厨师
    private List<ChefWorkerTask> chefWorkerTasks = new ArrayList<>(maxChefNumber);
    //可重入锁
    private Lock lock = new ReentrantLock();
    //订单condition
    private Condition orderCondition = lock.newCondition();
    //座位condition
    private Condition seatCondition = lock.newCondition();
    //咖啡condition
    private Condition coffeeCondition = lock.newCondition();


    public CoffeeHourse setMaxTime(int maxTime) {
        this.time = maxTime;
        return this;
    }

    public CoffeeHourse setMaxChefNumber(int maxChefNumber) {
        this.maxChefNumber = maxChefNumber;
        return this;
    }

    public CoffeeHourse setMaxSeatNumber(int maxSeatNumber) {
        this.maxSeatNumber = maxSeatNumber;
        this.seatNumber = maxSeatNumber;
        return this;
    }

    public CoffeeHourse setMaxOrderNumber(int maxOrderNumber) {
        this.maxOrderNumber = maxOrderNumber;
        return this;
    }

    /**
     * 添加一个顾客,方法会阻塞,直到有位置
     */
    public void addCustomer() {
        //如果咖啡厅已经停止工作了,就禁止入内
        if (!work) {
            return;
        }
        lock.lock();
        try {
            //循环等待一个conditon
            while (seatNumber <= 0) {
                try {
                    seatCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //满足条件则坐下
            --seatNumber;
            System.out.println("坐下一个顾客");
        } finally {
            lock.unlock();
        }
    }

    /**
     * 移除一个顾客
     */
    public void removeCustomer() {
        lock.lock();
        try {
            if (seatNumber < maxSeatNumber)
                ++seatNumber;
            System.out.println("离开一位顾客,当前座位数:" + seatNumber);
            seatCondition.signalAll();//同时所有等待作为的顾客们
        } finally {
            lock.unlock();
        }
    }

    /**
     * 顾客添加一个订单
     * 核心方法(阻塞)
     *
     * @param order
     */
    public Future<Coffee> addOrder(Order order) {
        lock.lock();
        try {
            while (this.orders.size() == maxOrderNumber) {
                try {
                    orderCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //添加一个订单
            orders.add(order);
            System.out.println("addOrder记录:" + order + "当前积累订单数:" + orders.size());
            orderCondition.signalAll();//通知其他线程,是其他线程获得订单列表的锁
        } finally {
            lock.unlock();
        }

        //同时为此用户开一个单独的线程,根据此用户的订单来获取咖啡
        Future<Coffee> coffeeFuture = executorService.submit(new Callable<Coffee>() {
            @Override
            public Coffee call() throws Exception {
                synchronized (orderCoffeeMap) {
                    //等待订单对应的咖啡制作完成
                    while (orderCoffeeMap.get(order) == null) {
                        orderCoffeeMap.wait();
                    }
                    return orderCoffeeMap.get(order);
                }
            }
        });
        return coffeeFuture;
    }


    /**
     * 完成咖啡制作,添加咖啡进入待取状态
     *
     * @param order
     */
    public void addCoffee(Order order) throws Exception {
        //使用了悲观锁的策略,目前只是简单实现,后续考虑加入读写锁(乐观)
        synchronized (orderCoffeeMap) {
            if (orderCoffeeMap.containsKey(order)) {
                throw new Exception("订单重复异常");
            }
            orderCoffeeMap.put(order, order.getCoffee());
            //通知其他正在阻塞的线程,让其他线程有机会获得orderCoffeeMap的锁
            orderCoffeeMap.notifyAll();
        }
    }


    /**
     * 提供一个订单(阻塞)
     */
    public Order takeOrder() {
        lock.lock();
        try {
            System.out.println("厨师申请得到订单...");
            while (this.orders.size() <= 0) {
                try {
                    orderCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //唤醒之后,提供一个订单,并通知其他线程当前订单发生变化
            Order order = orders.poll();
            orderCondition.signalAll();
            System.out.println("厨师takeOrder:记录:" + order + "还剩" + orders.size() + "个订单");
            return order;
        } finally {
            lock.unlock();
        }
    }


    /**
     * 咖啡厅关门->不再接纳新顾客(提供外部显示关闭)
     */
    public void shutDown() {
        work = false;
        //同时设置厨师工作
        chefWorkerTasks.forEach(chefWorkerTask -> {
            chefWorkerTask.setWork(false);
        });
        executorService.shutdown();
    }

    /**
     * 咖啡厅开门->开始工作->厨师就位
     */
    public void start() {
        work = true;
        for (int i = 0; i < maxChefNumber; ++i) {
            //启动厨师线程,数量为设定值
            ChefWorkerTask task = new ChefWorkerTask(this);
            task.setName("厨师" + i);
            executorService.execute(task);
            chefWorkerTasks.add(task);
        }

        //开始营业
        try {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
        }
        //关闭营业
        shutDown();
    }


    @Override
    public String toString() {
        return "CoffeeHourse{" +
                "maxChefNumber=" + maxChefNumber +
                ", maxSeatNumber=" + maxSeatNumber +
                ", seatNumber=" + seatNumber +
                ", maxOrderNumber=" + maxOrderNumber +
                ", orders=" + orders +
                '}';
    }


}