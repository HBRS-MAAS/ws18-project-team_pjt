package org.team_pjt.objects;

import java.util.Iterator;
import java.util.Vector;

public class KneadingMachine implements Runnable {
    private String sGuid;
    private String sCurrentKneadedProduct;
    private Vector<Integer> vTime;

    public void setvTime(Integer iTime) {
        if(vTime != null){
            vTime.add(iTime);
        } else {
            vTime = new Vector<>();
            vTime.add(iTime);
        }

    }

    public String getsCurrentKneadedProduct() {
        return sCurrentKneadedProduct;
    }

    public void setsCurrentKneadedProduct(String sCurrentKneadedProduct) {
        this.sCurrentKneadedProduct = sCurrentKneadedProduct;
    }

//    public void setiKneadingTime(Integer iKneadingTime) {
//        this.iKneadingTime = iKneadingTime;
//    }
//
//    public void setiRestingTime(Integer iRestingTime) {
//        this.iRestingTime = iRestingTime;
//    }

    public boolean isbBusy() {
        return bBusy;
    }

    private boolean bBusy;

    public KneadingMachine(String sGuid) {
        this.sGuid = sGuid;
        sCurrentKneadedProduct = null;
        bBusy = false;
        vTime = new Vector();
    }

    public void run(){
        startKneading();
    }

    public void startKneading(){
        bBusy = true;
        Iterator<Integer> iVTimeIterator = vTime.iterator();
        int iBusyTime = 0;
        while(iVTimeIterator.hasNext()){
          iBusyTime += iVTimeIterator.next();
        }
//        int iBusyTime = this.iKneadingTime + this.iRestingTime;
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
        vTime.clear();
        bBusy = false;
        System.out.println(sGuid + " finished kneading and resting");
        Thread.currentThread().interrupt();
        return;
    }


}
