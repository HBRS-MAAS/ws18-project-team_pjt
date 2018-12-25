package org.team_pjt.Objects;

public class DoughPreparationTable {
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

//    @Override
//    public void run() {
//        startPreparing();
//    }

    public void startPreparing() {
        bBusy = true;
        iPreparingTime = iAmountOfItem * iRestingTime;

        System.out.println(sGuid + " is preparing");
//        bBusy = false;
//        System.out.println(sGuid + " finished Preparing for product " + sCurrentPreparedProduct);
//        Thread.currentThread().interrupt();
//        bBusy = true;
//        Iterator<Integer> iVTimeIterator = vTime.iterator();
//
//        while(iVTimeIterator.hasNext()){
//            iBusyTime += iVTimeIterator.next();
//        }
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
//    if(iWorkTime < iBusyTime){
//        System.out.println("DoughManager: " + sDoughManager + " KneadingPreparingMachine: " + sGuid + " Momentane Arbeitszeit " + iWorkTime + " momentan gebackenes Produkt" + sCurrentKneadedProduct);
//        return false;
//    } else {
//        iWorkTime = 0;
//        vTime.clear();
//        bBusy = false;
//        System.out.println(sGuid + " finished kneading and resting");
//        return true;
//    }
}
