package org.team_pjt.doughprep.mas_maas.messages;

import java.util.Vector;

public class KneadingRequest extends GenericGuidMessage {
    private float kneadingTime;

    public KneadingRequest(Vector<String> guids, String productType, float kneadingTime) {
        super(guids, productType);
        this.kneadingTime = kneadingTime;
    }

    public float getKneadingTime() {
        return kneadingTime;
    }

    public void setKneadingTime(float kneadingTime) {
        this.kneadingTime = kneadingTime;
    }

    @Override
    public String toString() {
        return "KneadingRequest [kneadingTime=" + kneadingTime + "]";
    }
}
