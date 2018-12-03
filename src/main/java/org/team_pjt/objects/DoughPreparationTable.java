package org.team_pjt.objects;

public class DoughPreparationTable implements Runnable {
    private String sGuid;
    private int iRestingTime;
    private int iAmountOfItem;
    private String sCurrentPreparedProduct;
    private Boolean bBusy;
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

    private void startPreparing() {
        bBusy = true;
        int iPreparingTime = iAmountOfItem * iRestingTime;
        int iWorkTime = 0;
        System.out.println(sGuid + " is preparing");
        while(iWorkTime < iPreparingTime){
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            iWorkTime++;
        }
        bBusy = false;
        System.out.println(sGuid + " finished Preparing for product " + sCurrentPreparedProduct);
        Thread.currentThread().interrupt();
        return;
    }
}
