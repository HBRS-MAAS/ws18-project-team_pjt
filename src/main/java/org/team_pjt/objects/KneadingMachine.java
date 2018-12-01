package org.team_pjt.objects;

public class KneadingMachine implements Runnable {
    private String sGuid;
    private String sCurrentKneadedProduct;
    private Integer iKneadingTime;
    private Integer iRestingTime;

    public String getsCurrentKneadedProduct() {
        return sCurrentKneadedProduct;
    }

    public void setsCurrentKneadedProduct(String sCurrentKneadedProduct) {
        this.sCurrentKneadedProduct = sCurrentKneadedProduct;
    }

    public void setiKneadingTime(Integer iKneadingTime) {
        this.iKneadingTime = iKneadingTime;
    }

    public void setiRestingTime(Integer iRestingTime) {
        this.iRestingTime = iRestingTime;
    }

    public boolean isbBusy() {
        return bBusy;
    }

    private boolean bBusy;

    public KneadingMachine(String sGuid) {
        this.sGuid = sGuid;
        sCurrentKneadedProduct = null;
        bBusy = false;
    }

    public void run(){
        startKneading();
    }

    public void startKneading(){
        bBusy = true;
        int iBusyTime = this.iKneadingTime + this.iRestingTime;
        int iWorkTime = 0;
        System.out.println(sGuid + " is Kneading and Resting");
        while(iWorkTime < iBusyTime){
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            iWorkTime ++;
        }
        bBusy = false;
        System.out.println(sGuid + " finished kneading and resting");
        Thread.currentThread().interrupt();
        return;
    }


}
