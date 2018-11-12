package org.team_pjt.objects;

public class
Product {
    private String guid;
    private int boxingTemp;
    private float salesPrice;
    private int breadsPerOven;
    private int breadsPerBox;
    private int itemPrepTime; // Time to prepare a single item on a prepTable
    private int doughPrepTime; // Time inside a kneading machine
    private int bakingTemp;
    private int coolingRate;
    private int bakingTime;
    private int restingTime;
    private float productionCost;

    public Product(String guid, int boxingTemp, float salesPrice, int breadsPerOven, int breadsPerBox, int itemPrepTime, int doughPrepTime, int bakingTemp, int coolingRate, int bakingTime, int restingTime, float productionCost) {
        this.guid = guid;
        this.boxingTemp = boxingTemp;
        this.salesPrice = salesPrice;
        this.breadsPerOven = breadsPerOven;
        this.breadsPerBox = breadsPerBox;
        this.itemPrepTime = itemPrepTime;
        this.doughPrepTime = doughPrepTime;
        this.bakingTemp = bakingTemp;
        this.coolingRate = coolingRate;
        this.bakingTime = bakingTime;
        this.restingTime = restingTime;
        this.productionCost = productionCost;
    }

    public String getGuid() {
        return guid;
    }

    public int getBoxingTemp() {
        return boxingTemp;
    }

    public float getSalesPrice() {
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

    public int getDoughPrepTime() {
        return doughPrepTime;
    }

    public int getBakingTemp() {
        return bakingTemp;
    }

    public int getCoolingRate() {
        return coolingRate;
    }

    public int getBakingTime() {
        return bakingTime;
    }

    public int getRestingTime() {
        return restingTime;
    }

    public float getProductionCost() {
        return productionCost;
    }
}
