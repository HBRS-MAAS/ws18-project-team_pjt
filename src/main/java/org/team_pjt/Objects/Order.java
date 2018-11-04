package org.team_pjt.Objects;

import org.json.*;

import java.util.Hashtable;
import java.util.Set;

public class Order implements  Comparable<Order> {
    private String orderID;
    private String customerID;
    private Clock orderDate;
    private Clock deliveryDate;
    private Hashtable<String, Float> products;

    public Order(String orderID, String customerID, Clock orderDate, Clock deliveryDate, Hashtable<String, Float> products) {
        this.orderID = orderID;
        this.customerID = customerID;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.products = products;
    }

    public Order(String jsonOrderString) {
        JSONObject jsonOrder = new JSONObject(jsonOrderString);
        this.orderID = jsonOrder.getString("guid");
        this.customerID = jsonOrder.getString("customer_id");

        int day = Integer.parseInt(jsonOrder.getJSONObject("order_date").getString("day"));
        int hour = Integer.parseInt(jsonOrder.getJSONObject("order_date").getString("hour"));

        this.orderDate = new Clock(day, hour);

        day = Integer.parseInt(jsonOrder.getJSONObject("delivery_date").getString("day"));
        hour = Integer.parseInt(jsonOrder.getJSONObject("delivery_date").getString("hour"));

        this.deliveryDate = new Clock(day, hour);

        this.products = new Hashtable<>();
        JSONObject jsonProducts = jsonOrder.getJSONObject("products");
        String[] listProducts = JSONObject.getNames(jsonProducts);
        for (int i = 0; i < listProducts.length; ++i) {
            this.products.put(listProducts[i], new Float(jsonProducts.getFloat(listProducts[i])));
        }
    }

    public String getOrderID() {
        return orderID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public Clock getOrderDate() {
        return orderDate;
    }

    public Clock getDeliveryDate() {
        return deliveryDate;
    }

    public Hashtable<String, Float> getProducts() {
        return products;
    }

    public String toJSONString() {
        JSONObject obj = new JSONObject();
        JSONObject json_products = new JSONObject();
        JSONObject json_orderDate = new JSONObject();
        JSONObject json_deliveryDate = new JSONObject();

        Set<String> keys = this.products.keySet();
        for (String key: keys) {
            json_products.append(key, this.products.get(key));
        }

        json_orderDate.append("day", this.orderDate.getDay());
        json_orderDate.append("hour", this.orderDate.getHour());

        json_deliveryDate.append("day", this.deliveryDate.getDay());
        json_deliveryDate.append("hour", this.deliveryDate.getHour());

        obj.append("guid", this.orderID);
        obj.append("customer_id", this.customerID);
        obj.append("order_date", json_orderDate);
        obj.append("delivery_date", json_deliveryDate);

        obj.append("products", json_products);

        return obj.toString();
    }

    @Override
    public int compareTo(Order o) {
        return orderDate.compareTo(o.getOrderDate());
    }
}
