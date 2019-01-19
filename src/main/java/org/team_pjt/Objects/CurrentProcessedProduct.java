package org.team_pjt.Objects;

public class CurrentProcessedProduct {
    //    public CurrentProcessedProduct(String sOrderId, String sProduct) {
//        this.sOrderId = sOrderId;
//        this.sProduct = sProduct;
//        this.iAmount = -1;
//
//
//    }
    private Integer iDeliveryDay;
    private String sOrderId;
    private String sProduct;
    private Integer iAmount;

    public String getsOrderId() {
        return sOrderId;
    }

    public Integer getiDeliveryDay() {
        return iDeliveryDay;
    }

    public String getsProduct() {
        return sProduct;
    }

    public Integer getiAmount() {
        return iAmount;
    }

    public CurrentProcessedProduct(String sOrderId, String sProduct, Integer iAmount, Integer iDeliveryDay) {
        this.sOrderId = sOrderId;
        this.sProduct = sProduct;
        this.iAmount = iAmount;
        this.iDeliveryDay = iDeliveryDay;
    }
}
