package org.team_pjt.Objects;

import org.json.*;

import java.util.Hashtable;
import java.util.Set;

public class Order implements  Comparable<Order> {
    private String guid;
    private String customer_id;
    private Clock order_date;
    private Clock delivery_date;
    private Hashtable<String, Float> products;

    public Order(String guid, String customer_id, Clock order_date, Clock delivery_date, Hashtable<String, Float> products) {
        this.guid = guid;
        this.customer_id = customer_id;
        this.order_date = order_date;
        this.delivery_date = delivery_date;
        this.products = products;
    }

    public Order(String jsonOrderString) {
        JSONObject jsonOrder = new JSONObject(jsonOrderString);
        this.guid = jsonOrder.getString("guid");
        this.customer_id = jsonOrder.getString("customer_id");

        int day =(jsonOrder.getJSONObject("order_date").getInt("day"));
        int hour = (jsonOrder.getJSONObject("order_date").getInt("hour"));

        this.order_date = new Clock(day, hour);

        day = (jsonOrder.getJSONObject("delivery_date").getInt("day"));
        hour = (jsonOrder.getJSONObject("delivery_date").getInt("hour"));

        this.delivery_date = new Clock(day, hour);

        this.products = new Hashtable<>();
        JSONObject jsonProducts = jsonOrder.getJSONObject("products");
        String[] listProducts = JSONObject.getNames(jsonProducts);
        for (int i = 0; i < listProducts.length; ++i) {
            this.products.put(listProducts[i], new Float(jsonProducts.getFloat(listProducts[i])));
        }
    }

    public String getGuid() {
        return guid;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public Clock getOrder_date() {
        return order_date;
    }

    public Clock getDelivery_date() {
        return delivery_date;
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
            json_products.put(key, this.products.get(key));
        }

        json_orderDate.put("day", this.order_date.getDay());
        json_orderDate.put("hour", this.order_date.getHour());

        json_deliveryDate.put("day", this.delivery_date.getDay());
        json_deliveryDate.put("hour", this.delivery_date.getHour());

        obj.put("guid", this.guid);
        obj.put("customer_id", this.customer_id);
        obj.put("order_date", json_orderDate);
        obj.put("delivery_date", json_deliveryDate);

        obj.put("products", json_products);

        return obj.toString();
    }

    @Override
    public int compareTo(Order o) {
        return order_date.compareTo(o.getOrder_date());
    }
}
