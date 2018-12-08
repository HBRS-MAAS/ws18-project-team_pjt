package org.team_pjt.objects;

import java.util.Iterator;
import java.util.Vector;

public class KneadingMachine{
    private String sGuid;
    private String sCurrentKneadedProduct;
    private Vector<Integer> vTime;
    private int iBusyTime = 0;

    public void setiWorkTime() {
        this.iWorkTime++;
    }

    private int iWorkTime = 0;

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

//    public void run(){
//        startKneading();
//    }

    public void startKneading(String sKneadingMachineGuid){
        bBusy = true;
        Iterator<Integer> iVTimeIterator = vTime.iterator();

        while(iVTimeIterator.hasNext()){
          iBusyTime += iVTimeIterator.next();
        }
//        System.out.println(sKneadingMachineGuid + sGuid + " is Kneading and Resting product " + sCurrentKneadedProduct);
    }

    public boolean checkKneadingMachineState(String sDoughManager){
        if(iWorkTime < iBusyTime){
            System.out.println("DoughManager: " + sDoughManager + " KneadingMachine: " + sGuid + " Momentane Arbeitszeit " + iWorkTime + " momentan gebackenes Produkt" + sCurrentKneadedProduct);
            return false;
        } else {
            iWorkTime = 0;
            vTime.clear();
            bBusy = false;
            System.out.println(sGuid + " finished kneading and resting");
            return true;
        }
    }


}
