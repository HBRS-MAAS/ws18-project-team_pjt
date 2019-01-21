package org.team_pjt.Objects;

import java.util.Iterator;
import java.util.Vector;

public class KneadingPreparingMachine {
    private String sGuid;
    private String sCurrentKneadedProduct;
    private String sCurrentKneadedOrder;
    private Vector<Integer> vTime;
    private int iBusyTime = 0;
    private int iAmount = -1;

    public void setiAmount(int iAmount) {
        this.iAmount = iAmount;
    }

    public int getiAmount() {
        return iAmount;
    }

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

    public String getsCurrentKneadedOrder() {
        return sCurrentKneadedOrder;
    }

    public void setsCurrentKneadedOrder(String sCurrentKneadedOrder) {
        this.sCurrentKneadedOrder = sCurrentKneadedOrder;
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

    public KneadingPreparingMachine(String sGuid) {
        this.sGuid = sGuid;
        sCurrentKneadedProduct = "";
        sCurrentKneadedOrder = "";
        bBusy = false;
        vTime = new Vector();
    }

    public void startKneading(String sKneadingMachineGuid){
        bBusy = true;
        Iterator<Integer> iVTimeIterator = vTime.iterator();

        while(iVTimeIterator.hasNext()){
            Integer iNext = iVTimeIterator.next();
            iBusyTime += iNext;
        }

        iBusyTime = iBusyTime * iAmount;
//        System.out.println("Gesamte PreparationTime " + iBusyTime);
    }

    public boolean checkKneadingMachineState(String sDoughManager){
        if(iWorkTime < iBusyTime){
//            System.out.println("DoughManager: " + sDoughManager + " KneadingPreparingMachine: " + sGuid + " Momentane Arbeitszeit " + iWorkTime + " momentan gebackenes Produkt: " + sCurrentKneadedProduct + " Order: " + sCurrentKneadedOrder);
            return false;
        } else {
            iWorkTime = 0;
            iBusyTime = 0;
            vTime.clear();
            bBusy = false;
//            sCurrentKneadedProduct = "";
//            sCurrentKneadedOrder = "";
//            System.out.println(sGuid + " finished kneading and resting");
            return true;
        }
    }


}
