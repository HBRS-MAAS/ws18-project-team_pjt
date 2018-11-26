package org.team_pjt.doughprep.mas_maas.objects;

public class Packaging {
    private int boxingTemp;
    private int breadsPerBox;

    public Packaging(int boxingTemp, int breadsPerBox) {
        super();
        this.boxingTemp = boxingTemp;
        this.breadsPerBox = breadsPerBox;
    }

    private int getBoxingTemp() {
        return boxingTemp;
    }

    private void setBoxingTemp(int boxingTemp) {
        this.boxingTemp = boxingTemp;
    }

    private int getBreadsPerBox() {
        return breadsPerBox;
    }

    private void setBreadsPerBox(int breadsPerBox) {
        this.breadsPerBox = breadsPerBox;
    }

    @Override
    public String toString() {
        return "Packaging [boxingTemp=" + boxingTemp + ", breadsPerBox=" + breadsPerBox + "]";
    }
}
