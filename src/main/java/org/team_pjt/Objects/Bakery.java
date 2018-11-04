package org.team_pjt.Objects;

import org.json.JSONObject;

import java.util.Hashtable;

public class Bakery {
    private Hashtable<String, Product> availableProducts;

    public Bakery(String jsonString) {
        JSONObject products = new JSONObject(jsonString);
    }

    public Hashtable<String, Product> getAvailableProducts() {
        return availableProducts;
    }
}
