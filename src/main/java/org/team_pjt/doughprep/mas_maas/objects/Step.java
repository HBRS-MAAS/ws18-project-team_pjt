package org.team_pjt.doughprep.mas_maas.objects;

public class Step
{
    private String action;
    private Float duration;
    public final static String KNEADING_TIME = "kneading";
    public final static String PROOFING_TIME = "proofing";
    public final static String KNEADING_STEP = "kneading";
    public final static String COOLING_STEP =  "cooling";
    public final static String PROOFING_STEP = "proofing";
    public final static String ITEM_PREPARATION_STEP = "item preparation";

    public Step(String action, Float duration2) {
        this.action = action;
        this.duration = duration2;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Float getDuration() {
        return duration;
    }

    public void setDuration(Float duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Step [action=" + action + ", duration=" + duration + "]";
    }
}
