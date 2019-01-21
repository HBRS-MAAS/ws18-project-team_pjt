package org.team_pjt.Objects;

import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Iterator;

public class Order {
    private String customer_agent_id;
    private String guid;
    private int order_day;
    private int order_hour;
    private int delivery_day;
    private int delivery_hour;
    private Hashtable<String, Integer> products;

    public Order(String json_order_string) {
        products = new Hashtable<>();
        JSONObject order = new JSONObject(json_order_string);
        // Distinction because "_" is cut off when sending messages
        if (order.has("customer_id")) {
            customer_agent_id = order.getString("customer_id");
        }
        if(order.has("customerId")){
            customer_agent_id = order.getString("customerId");
        }
        if (order.has("order_date")) {
            order_day = order.getJSONObject("order_date").getInt("day");
            order_hour = order.getJSONObject("order_date").getInt("hour");
        }
        if(order.has("orderDate")){
            order_day = order.getJSONObject("orderDate").getInt("day");
            order_hour = order.getJSONObject("orderDate").getInt("hour");
        }

        if (order.has("delivery_date")) {
            delivery_day = order.getJSONObject("delivery_date").getInt("day");
            delivery_hour = order.getJSONObject("delivery_date").getInt("hour");
        }
        if (order.has("deliveryDate")) {
            delivery_day = order.getJSONObject("deliveryDate").getInt("day");
            delivery_hour = order.getJSONObject("deliveryDate").getInt("hour");
        }
        guid = order.getString("guid");

        Iterator<String> prouct_name_it = order.getJSONObject("products").keys();

        while(prouct_name_it.hasNext()) {
            String product_name = prouct_name_it.next();
            products.put(product_name, order.getJSONObject("products").getInt(product_name));
        }
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
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

    public String toJSONString() {
        JSONObject order = new JSONObject();

        JSONObject orderDate = new JSONObject();
        orderDate.put("day", order_day);
        orderDate.put("hour", order_hour);

        JSONObject deliveryDate = new JSONObject();
        deliveryDate.put("day", delivery_day);
        deliveryDate.put("hour", delivery_hour);

        JSONObject loc_products = new JSONObject();
        for(String product_name : products.keySet()) {
            loc_products.put(product_name, products.get(product_name));
        }

        order.put("customerId", customer_agent_id);
        order.put("guid", guid);
        order.put("orderDate", orderDate);
        order.put("deliveryDate", deliveryDate);
        order.put("products", loc_products);
        return order.toString();
    }
}
