package org.team_pjt.objects;

public class DoughPreparationTable implements Runnable {
    private String sGuid;
    private int iRestingTime;
    private int iAmountOfItem;
    private String sCurrentPreparedProduct;
    private Boolean bBusy;
    private int iPreparingTime;

    public int getiWorkTime() {
        return iWorkTime;
    }

    public void setiWorkTime() {
        iWorkTime++;
    }

    private int iWorkTime = 0;

    public Boolean getbBusy() {
        return bBusy;
    }

    public DoughPreparationTable(String sGuid) {
        this.sGuid = sGuid;
        sCurrentPreparedProduct = null;
        bBusy = false;
    }
//    public int getiRestingTime() {
//        return iRestingTime;
//    }

    public void setiRestingTime(int iRestingTime) {
        this.iRestingTime = iRestingTime;
    }

//    public int getiAmountOfItem() {
//        return iAmountOfItem;
//    }
    public int getiAmountOfItem() {
        return iAmountOfItem;
    }

    public void setiAmountOfItem(int iAmountOfItem) {
        this.iAmountOfItem = iAmountOfItem;
    }

    public String getsCurrentPreparedProduct() {
        return sCurrentPreparedProduct;
    }

    public void setsCurrentPreparedProduct(String sCurrentPreparedProduct) {
        this.sCurrentPreparedProduct = sCurrentPreparedProduct;
    }

    public Boolean isBusy() {
        return bBusy;
    }

//    public void setbBusy(Boolean bBusy) {
//        this.bBusy = bBusy;
//    }

    @Override
    public void run() {
        startPreparing();
    }

    public void startPreparing() {
        bBusy = true;
        iPreparingTime = iAmountOfItem * iRestingTime;

        System.out.println(sGuid + " is preparing");
//        bBusy = false;
//        System.out.println(sGuid + " finished Preparing for product " + sCurrentPreparedProduct);
//        Thread.currentThread().interrupt();
        return;
    }

    public boolean checkPreparingStage(){
        if(iWorkTime > iPreparingTime){
            bBusy = false;
            iWorkTime = 0;
            return true;
        }
        else
            System.out.println("WorkingTime is " + iWorkTime);
            return false;
    }
}
