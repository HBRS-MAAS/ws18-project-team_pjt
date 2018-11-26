package org.team_pjt.doughprep.mas_maas.objects;

import java.awt.geom.Point2D;

public class Truck {
    private String guid;
    private int loadCapacity;
    private Point2D locaiton;

    public Truck(String guid, int loadCapacity, Point2D locaiton) {
        super();
        this.guid = guid;
        this.loadCapacity = loadCapacity;
        this.locaiton = locaiton;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getLoadCapacity() {
        return loadCapacity;
    }

    public void setLoadCapacity(int loadCapacity) {
        this.loadCapacity = loadCapacity;
    }

    public Point2D getLocaiton() {
        return locaiton;
    }

    public void setLocaiton(Point2D locaiton) {
        this.locaiton = locaiton;
    }

    @Override
    public String toString() {
        return "Truck [guid=" + guid + ", loadCapacity=" + loadCapacity + ", locaiton=" + locaiton + "]";
    }
}
