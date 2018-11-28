package org.team_pjt.objects;

import org.json.JSONObject;

import java.util.Iterator;

public class
Product {

    private String guid;
    private int boxingTemp;
    private Double salesPrice;
    private int breadsPerOven;
    private int breadsPerBox;
    private int itemPrepTime; // Time to prepare a single item on a prepTable
    private int kneadingTime; // Time inside a kneading machine
    private int bakingTemp;
    private int cooling;
    private int proofing;
    private int baking;
    private int restingTime;
    private int filling;
    private int decorating;
    private int sprinkling;
    private int coolingRate;
    private int recipeDuration;
    private Double productionCost;

    public Product(String guid, int boxingTemp, Double salesPrice, int breadsPerOven, int breadsPerBox, int itemPrepTime, int doughPrepTime, int bakingTemp, int coolingRate, int bakingTime, int restingTime, Double productionCost) {
        this.guid = guid;
        this.boxingTemp = boxingTemp;
        this.salesPrice = salesPrice;
        this.breadsPerOven = breadsPerOven;
        this.breadsPerBox = breadsPerBox;
        this.itemPrepTime = itemPrepTime;
        this.kneadingTime = doughPrepTime;
        this.bakingTemp = bakingTemp;
        this.cooling = coolingRate;
        this.baking = bakingTime;
        this.restingTime = restingTime;
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
        Iterator<Object> step_iterator = recipe.getJSONArray("steps").iterator();
        while(step_iterator.hasNext()) {
            JSONObject step = (JSONObject)step_iterator.next();
            String action = step.getString("action");
            switch (action) {
                case "kneading": this.kneadingTime = step.getInt("duration");
                case "resting": this.restingTime = step.getInt("duration");
                case "item preparation": this.itemPrepTime = step.getInt("duration");
                case "cooling": this.cooling = step.getInt("duration");
                case "proofing": this.proofing = step.getInt("duration");
                case "filling": this.filling = step.getInt("duration");
                case "baking": this.baking = step.getInt("duration");
                case "decorating": this.decorating = step.getInt("duration");
                case "sprinkling": this.sprinkling = step.getInt("duration");
            }
        }
        this.bakingTemp = recipe.getInt("bakingTemp");
//        this.coolingRate = recipe.getInt("coolingRate");
    }

    public int getCoolingRate() {
        return coolingRate;
    }

    public void setCoolingRate(int coolingRate) {
        this.coolingRate = coolingRate;
    }

    public int getRecipeDuration() {
        return recipeDuration;
    }

    public void setRecipeDuration(int recipeDuration) {
        this.recipeDuration = recipeDuration;
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

    public void setItemPrepTime(int itemPrepTime) {
        this.itemPrepTime = itemPrepTime;
    }

    public void setKneadingTime(int kneadingTime) {
        this.kneadingTime = kneadingTime;
    }

    public void setBakingTemp(int bakingTemp) {
        this.bakingTemp = bakingTemp;
    }

    public void setCooling(int cooling) {
        this.cooling = cooling;
    }

    public void setBaking(int baking) {
        this.baking = baking;
    }

    public void setRestingTime(int restingTime) {
        this.restingTime = restingTime;
    }

    public void setProductionCost(Double productionCost) {
        this.productionCost = productionCost;
    }

    public int getSprinkling() {
        return sprinkling;
    }

    public void setSprinkling(int sprinkling) {
        this.sprinkling = sprinkling;
    }

    public int getDecorating() {
        return decorating;
    }

    public void setDecorating(int decorating) {
        this.decorating = decorating;
    }

    public int getFilling() {
        return filling;
    }

    public void setFilling(int filling) {
        this.filling = filling;
    }

    public int getProofing() {
        return proofing;
    }

    public void setProofing(int proofing) {
        this.proofing = proofing;
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

    public int getItemPrepTime() {
        return itemPrepTime;
    }

    public int getKneadingTime() {
        return kneadingTime;
    }

    public int getBakingTemp() {
        return bakingTemp;
    }

    public int getCooling() {
        return cooling;
    }

    public int getBaking() {
        return baking;
    }

    public int getRestingTime() {
        return restingTime;
    }

    public Double getProductionCost() {
        return productionCost;
    }
}
