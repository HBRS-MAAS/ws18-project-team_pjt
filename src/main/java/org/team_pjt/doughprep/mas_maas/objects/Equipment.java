package org.team_pjt.doughprep.mas_maas.objects;

abstract public class Equipment {
    private String guid;

    public Equipment(String guid) {
        this.guid = guid;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    @Override
    public String toString() {
        return "Equipment [guid=" + guid + "]";
    }
}
