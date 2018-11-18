package org.team_pjt.objects;

import jade.core.AID;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Iterator;

public class Order {
    private String customer_agent_id;
    private int order_day;
    private int order_hour;
    private int delivery_day;
    private int delivery_hour;
    private Hashtable<String, Integer> products;

    public Order(String json_order_string) {
        products = new Hashtable<>();
        JSONObject order = new JSONObject(json_order_string);
        customer_agent_id = order.getString("customer_id");
        order_day = order.getJSONObject("orderDate").getInt("day");
        order_hour = order.getJSONObject("orderDate").getInt("hour");
        delivery_day = order.getJSONObject("deliveryDate").getInt("day");
        delivery_hour = order.getJSONObject("deliveryDate").getInt("hour");

        Iterator<String> prouct_name_it = order.getJSONObject("products").keys();

        while(prouct_name_it.hasNext()) {
            String product_name = prouct_name_it.next();
            products.put(product_name, order.getJSONObject("products").getInt(product_name));
        }
    }

    public int getOrderDay() {
        return order_day;
    }

    public void setOrderDay(int order_day) {
        this.order_day = order_day;
    }

    public int getOrderHour() {
        return order_hour;
    }

    public void setOrderHour(int order_hour) {
        this.order_hour = order_hour;
    }

    public int getDeliveryDay() {
        return delivery_day;
    }

    public void setDeliveryDay(int delivery_day) {
        this.delivery_day = delivery_day;
    }

    public int getDeliveryHour() {
        return delivery_hour;
    }

    public void setDeliveryHour(int delivery_hour) {
        this.delivery_hour = delivery_hour;
    }

    public Hashtable<String, Integer> getProducts() {
        return products;
    }

    public void setProducts(Hashtable<String, Integer> products) {
        this.products = products;
    }
}
