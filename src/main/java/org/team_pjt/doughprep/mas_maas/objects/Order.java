package org.team_pjt.doughprep.mas_maas.objects;

import java.util.Vector;

public class Order {

    private String customerId;
    private String guid;

    // I thought about using a standard java object like Calendar or Date
    // but this seemed a better fit given the json format and our limited scope
    private int orderDay;
    private int orderHour;
    private int deliveryDate;
    private int deliveryHour;
    private Vector<BakedGood> bakedGoods;

    public Order() {}

    public Order(String customerId, String guid, int orderDay, int orderHour, int deliveryDate, int deliveryHour,
            Vector<BakedGood> bakedGoods) {
        super();
        this.customerId = customerId;
        this.guid = guid;
        this.orderDay = orderDay;
        this.orderHour = orderHour;
        this.deliveryDate = deliveryDate;
        this.deliveryHour = deliveryHour;
        this.bakedGoods = bakedGoods;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getOrderDay() {
        return orderDay;
    }

    public void setOrderDay(int orderDay) {
        this.orderDay = orderDay;
    }

    public int getOrderHour() {
        return orderHour;
    }

    public void setOrderHour(int orderHour) {
        this.orderHour = orderHour;
    }

    public int getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(int deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public int getDeliveryHour() {
        return deliveryHour;
    }

    public void setDeliveryHour(int deliveryHour) {
        this.deliveryHour = deliveryHour;
    }

    public Vector<BakedGood> getBakedGoods() {
        return bakedGoods;
    }

    public void setBakedGoods(Vector<BakedGood> bakedGoods) {
        this.bakedGoods = bakedGoods;
    }

    @Override
    public String toString() {
        return "Order [customerId=" + customerId + ", guid=" + guid + ", orderDay=" + orderDay + ", orderHour="
                + orderHour + ", deliveryDate=" + deliveryDate + ", deliveryHour=" + deliveryHour + ", bakedGoods="
                + bakedGoods + "]";
    }
}
