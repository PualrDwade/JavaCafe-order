package com.edu.xuanxuan;

import java.util.Date;

/**
 * 订单类
 */
public class Order {
    //订单日期
    private Date date = new Date();
    //订单内容(咖啡枚举类型)
    private Coffee coffee;
    //订单所属顾客
    private CustomerOrderTask customerOrderTask;

    public Order(Coffee coffee, CustomerOrderTask customerOrderTask) {
        this.coffee = coffee;
        this.customerOrderTask = customerOrderTask;
    }

    @Override
    public String toString() {
        return "订单时间:" + date + "--咖啡品种:" + coffee.name() + "--所属顾客:" + customerOrderTask;
    }

    public Date getDate() {
        return date;
    }

    public Coffee getCoffee() {
        return coffee;
    }

    public CustomerOrderTask getCustomerOrderTask() {
        return customerOrderTask;
    }
}
