package org.team_pjt.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class
Product {

    private String guid;
    private int boxingTemp;
    private Double salesPrice;
    private int breadsPerOven;
    private int breadsPerBox;
    private int bakingTemp;
    private JSONArray steps;
    private Double productionCost;

    public Product(String guid, int boxingTemp, Double salesPrice, int breadsPerOven, int breadsPerBox, int bakingTemp, JSONArray steps, Double productionCost) {
        this.guid = guid;
        this.boxingTemp = boxingTemp;
        this.salesPrice = salesPrice;
        this.breadsPerOven = breadsPerOven;
        this.breadsPerBox = breadsPerBox;
        this.steps = steps;
        this.bakingTemp = bakingTemp;
        this.productionCost = productionCost;
    }

    public Product() {
    }

    public Product(String json_product_string) {
        JSONObject joProduct = new JSONObject(json_product_string);
        this.guid = joProduct.getString("guid");
        this.boxingTemp = joProduct.getJSONObject("packaging").getInt("boxingTemp");
        this.breadsPerBox = joProduct.getJSONObject("packaging").getInt("breadsPerBox");
        this.salesPrice = joProduct.getDouble("salesPrice");
        this.productionCost = joProduct.getDouble("productionCost");
        JSONObject recipe = joProduct.getJSONObject("recipe");
        this.steps = recipe.getJSONArray("steps");
        this.bakingTemp = recipe.getInt("bakingTemp");
//        this.coolingRate = recipe.getInt("coolingRate");
    }

    public int getBakingTemp() {
        return bakingTemp;
    }

    public void setBakingTemp(int bakingTemp) {
        this.bakingTemp = bakingTemp;
    }

    public JSONArray getSteps() {
        return steps;
    }

    public void setSteps(JSONArray steps) {
        this.steps = steps;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public void setBoxingTemp(int boxingTemp) {
        this.boxingTemp = boxingTemp;
    }

    public void setSalesPrice(Double salesPrice) {
        this.salesPrice = salesPrice;
    }

    public void setBreadsPerOven(int breadsPerOven) {
        this.breadsPerOven = breadsPerOven;
    }

    public void setBreadsPerBox(int breadsPerBox) {
        this.breadsPerBox = breadsPerBox;
    }

    public void setProductionCost(Double productionCost) {
        this.productionCost = productionCost;
    }

    public String getGuid() {
        return guid;
    }

    public int getBoxingTemp() {
        return boxingTemp;
    }

    public Double getSalesPrice() {
        return salesPrice;
    }

    public int getBreadsPerOven() {
        return breadsPerOven;
    }

    public int getBreadsPerBox() {
        return breadsPerBox;
    }

    public Double getProductionCost() {
        return productionCost;
    }
}
