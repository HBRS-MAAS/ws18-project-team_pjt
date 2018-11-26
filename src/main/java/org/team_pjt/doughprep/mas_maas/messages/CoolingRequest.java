package org.team_pjt.doughprep.mas_maas.messages;

public class CoolingRequest {
    private String productName;
    private int coolingRate;
    private int quantity;
    private int boxingTemp;

    public CoolingRequest(String productName, int coolingRate, int quantity, int boxingTemp) {
        super();
        this.productName = productName;
        this.coolingRate = coolingRate;
        this.quantity = quantity;
        this.boxingTemp = boxingTemp;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getCoolinRate() {
        return coolingRate;
    }

    public void setCoolinRate(int coolingRate) {
        this.coolingRate = coolingRate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getBoxingTemp() {
        return boxingTemp;
    }

    public void setBoxingTemp(int boxingTemp) {
        this.boxingTemp = boxingTemp;
    }

    @Override
    public String toString() {
        return "CoolingRequest [productName=" + productName + ", coolingRate=" + coolingRate + ", quantity=" + quantity
                + ", boxingTemp=" + boxingTemp + "]";
    }
}
