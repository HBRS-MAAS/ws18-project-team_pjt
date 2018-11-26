package org.team_pjt.doughprep.mas_maas.objects;

import java.util.Vector;

public class MetaInfo {
    private int bakeries;
    private Vector<Client> customers; //TODO this will need to change (only in place to match the json file)
    private int durationInDays;
    private int products;
    private int orders;

    public MetaInfo(int bakeries, Vector<Client> customers, int durationInDays, int products, int orders) {
        this.bakeries = bakeries;
        this.customers = customers;
        this.durationInDays = durationInDays;
        this.products = products;
        this.orders = orders;
    }

    public int getBakeries() {
        return bakeries;
    }

    public void setBakeries(int bakeries) {
        this.bakeries = bakeries;
    }

    public Vector<Client> getCustomers() {
        return customers;
    }

    public void setCustomers(Vector<Client> customers) {
        this.customers = customers;
    }

    public int getDurationInDays() {
        return durationInDays;
    }

    public void setDurationInDays(int durationInDays) {
        this.durationInDays = durationInDays;
    }

    public int getProducts() {
        return products;
    }

    public void setProducts(int products) {
        this.products = products;
    }

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "MetaInfo [bakeries=" + bakeries + ", customers=" + customers + ", durationInDays=" + durationInDays
                + ", products=" + products + ", orders=" + orders + "]";
    }
}
