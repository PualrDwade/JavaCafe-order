# Java多线程练手项目-咖啡厅排队订餐
* 一个咖啡厅有固定数量的座位,顾客排队点餐
* 几个厨师接单,做不同的咖啡,各个咖啡用时不同
* 咖啡完成,通知用户
* 不使用阻塞队列,全部使用wait-notify实现(或者使用JDK1.5提供的ReenTrantLock实现)
* 项目结构:
```
'    |-- README.md',
'    |-- src',
'        |-- com',
'            |-- edu',
'                |-- xuanxuan',
'                    |-- ChefWorkerTask.java', ---厨师线程
'                    |-- Coffee.java', ---咖啡类型枚举
'                    |-- CoffeeHourse.java', ---咖啡厅实体,实现生产者-消费者
'                    |-- CustomerOrderTask.java', ---顾客线程
'                    |-- Main.java', ---主线程,调度&启动类
'                    |-- Order.java', ---订单Entry
```
