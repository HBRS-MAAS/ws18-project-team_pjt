package org.team_pjt.doughprep.mas_maas.objects;

public class StreetLink {
    // TODO source and target should probably be StreetNode Objects...
    private String source;
    private String guid;
    private double dist;
    private String target;

    public StreetLink(String source, String guid, double dist, String target) {
        this.source = source;
        this.guid = guid;
        this.dist = dist;
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "StreetLink [source=" + source + ", guid=" + guid + ", dist=" + dist + ", target=" + target + "]";
    }
}
