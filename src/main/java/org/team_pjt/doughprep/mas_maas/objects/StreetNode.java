package org.team_pjt.doughprep.mas_maas.objects;

import java.awt.geom.Point2D;

public class StreetNode {
    private String name;
    private String company; //TODO this will probably be an Object?
    private Point2D location;
    private String guid;
    private String type;

    public StreetNode(String name, String company, Point2D location, String guid, String type) {
        this.name = name;
        this.company = company;
        this.location = location;
        this.guid = guid;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "StreetNode [name=" + name + ", company=" + company + ", location=" + location + ", guid=" + guid
                + ", type=" + type + "]";
    }
}
