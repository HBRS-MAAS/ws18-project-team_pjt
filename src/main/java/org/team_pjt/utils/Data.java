package org.team_pjt.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jade.lang.acl.ACLMessage;

public class Data{
    private JSONArray dataArray = new JSONArray();
    private JSONArray orders = new JSONArray();

    public void retrieve(String fileName) {
        File file = new File(fileName);
        String filePath = file.getAbsolutePath();
        String fileContent = "";

        try {
            fileContent = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            dataArray = new JSONArray(fileContent);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getName() {
        List<String> name = new ArrayList();

        try {
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject customerData = dataArray.getJSONObject(i);
                name.add(customerData.getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return name;
    }

    public List<String> getID() {
        List<String> id = new ArrayList();

        try {
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject customerData = dataArray.getJSONObject(i);
                id.add(customerData.getString("guid"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return id;
    }

    public int getOrder(String id) {
        String customerID = "";

        //Take Orders from Customer (based on the name)
        try {
            for (int i = 0; i < dataArray.length(); i++) {
                customerID = dataArray.getJSONObject(i).getString("guid");

                if (customerID.equals(id)) {
                    orders = dataArray.getJSONObject(i).getJSONArray("orders");

                    return orders.length();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public JSONObject getCurrentOrder(LocalDate date) {
        JSONObject order_date = new JSONObject();

        //Check Date
        try {
            for (int i = 0; i < orders.length(); i++) {
                order_date = orders.getJSONObject(i).getJSONObject("order_date");

                int day = order_date.getInt("hour");
                int month = order_date.getInt("day");

				/*if ((day == date.getDayOfMonth()) && (month == date.getMonthValue()) ) {
					return orders.getJSONObject(i);
				}*/

                return orders.getJSONObject(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject checkAvailability(JSONObject order, String bakeryName,
                                        List<String> sellType, List<String> sellPrice) {
        JSONObject orderProduct = new JSONObject();
        JSONObject orderPrice = new JSONObject();
        JSONObject bakeryPrice = new JSONObject();
        List<String> orderType = new ArrayList();
        String orderID = "";

        //Get All Order Type
        try {
            orderID = order.getString("guid");
            bakeryPrice.put("guid", orderID);

            orderProduct = order.getJSONObject("products");

            Iterator iter = orderProduct.keys();
            while(iter.hasNext()){
                String key = (String)iter.next();
                orderType.add(key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Compare Order Type with Sell Type
        try {
            for (int i = 0; i < orderType.size(); i++) {
                for (int j = 0; j < sellType.size(); j++) {
                    if (orderType.get(i).equals(sellType.get(j))) {
                        orderPrice.put(sellType.get(j), sellPrice.get(j));
                        break;
                    }
                }
            }
            bakeryPrice.put("products", orderPrice);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //System.out.println("Bakery Price: " + bakeryPrice);

        return bakeryPrice;
    }

    public Map<String, List<String>> getProduct(String id) {
        String bakeryID = "";
        JSONArray products = new JSONArray();

        List<String> productType = new ArrayList();
        List<String> productPrice = new ArrayList();

        //Get Product List
        try {
            for (int i = 0; i < dataArray.length(); i++) {
                bakeryID = dataArray.getJSONObject(i).getString("guid");

                if (bakeryID.equals(id)) {
                    products = dataArray.getJSONObject(i).getJSONArray("products");

                    for (int j = 0; j < products.length(); j++) {
                        productType.add(products.getJSONObject(j).getString("guid"));
                        //productPrice.add(BigDecimal.valueOf(products.getJSONObject(i).getDouble("salesPrice")).floatValue());
                        productPrice.add(Double.toString(products.getJSONObject(j).getDouble("salesPrice")));
                    }

                    Map<String,List<String>> map = new HashMap();
                    map.put("productType", productType);
                    map.put("productPrice", productPrice);
                    return map;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONObject findTheCheapest(JSONObject proposal) {
        JSONObject confirmation = new JSONObject();
        JSONObject product = new JSONObject();

        List<String> bakeryName = new ArrayList();
        List<String> productTypes = new ArrayList();

        String chosenBakery = "";

        //Get All Bakery Name
        try {
            Iterator iter = proposal.keys();
            while(iter.hasNext()) {
                String key = (String)iter.next();
                bakeryName.add(key);

                product = proposal.getJSONObject(key);
                Iterator iter2 = product.keys();
                while(iter2.hasNext()) {
                    String key2 = (String)iter2.next();
                    if (!productTypes.contains(key2)) {
                        productTypes.add(key2);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Get The Cheapest Price
        try {
            for (String type : productTypes) {
                Double min_price = Double.MAX_VALUE;
                for (String name : bakeryName) {
                    product = proposal.getJSONObject(name);

                    if (min_price > product.getDouble(type) && product.getDouble(type) != 0) {
                        chosenBakery = name;
                        min_price = product.getDouble(type);
                    }
                }

                if (confirmation.has(chosenBakery)) {
                    type = type + ", " + confirmation.getString(chosenBakery);
                }

                confirmation.put(chosenBakery, type);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return confirmation;
    }

    /*
    Orders are randomly generated. They are saved in JSON format and includes ID and ordered
    product list.
    16 Nov 2018: only one order in orders. Customer ID is randomly generated to make sure
    there are no duplication. Number of product ordered is also randomly generated.
    */
    public List<JSONObject> makeOrder(List<String> product_types) {
        List<JSONObject> orders = new ArrayList<>();
        JSONObject order = new JSONObject();

        String customer_id = generateRandomID(10);
        int min = 0;
        int max = 50;
        int total_order = 0;

        try {
            order.put("id", customer_id);
            //JSONArray products = new JSONArray();
            JSONObject product = new JSONObject();

            for(String product_type : product_types ) {
                total_order = ThreadLocalRandom.current().nextInt(min, max + 1);
                product.put(product_type, total_order);
                //products.add(product);
            }

            order.put("Product List", product);
            orders.add(order);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return orders;
    }

    private String generateRandomID(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz"
                + "0123456789";
        String str = new Random().ints(length, 0, chars.length())
                .mapToObj(i -> "" + chars.charAt(i))
                .collect(Collectors.joining());
        return str;
    }
}
