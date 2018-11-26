package org.team_pjt.doughprep.mas_maas.messages;
import java.util.Vector;

public class BakingRequest extends GenericGuidMessage {
    private int bakingTemp;
    private float bakingTime;

    public BakingRequest(Vector<String> guids, String productType, int bakingTemp, float bakingTime) {
        super(guids, productType);
        this.bakingTemp = bakingTemp;
        this.bakingTime = bakingTime;
    }

    public int getBakingTemp() {
        return bakingTemp;
    }

    public void setBakingTemp(int bakingTemp) {
        this.bakingTemp = bakingTemp;
    }

    public float getBakingTime() {
        return bakingTime;
    }

    public void setBakingTime(float bakingTime) {
        this.bakingTime = bakingTime;
    }

    @Override
    public String toString() {
        return "BakingRequest [bakingTemp=" + bakingTemp + ", bakingTime=" + bakingTime + "]";
    }
}
