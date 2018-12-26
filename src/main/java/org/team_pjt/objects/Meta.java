package org.team_pjt.objects;

import java.util.Hashtable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.team_pjt.utils.Time;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Meta {
    private int bakeries;
    private Hashtable<String, Integer> customers;
    private int durationInDays;
    private int products;
    private int orders;
    private Time timeStep;

    public Meta() {
        this.customers = new Hashtable<>();
        this.timeStep = new Time();
    }

    public int getBakeries() {
        return bakeries;
    }
    public void setBakeries(int bakeries) {
        this.bakeries = bakeries;
    }
    public Hashtable<String, Integer> getCustomers() {
        return customers;
    }
    public void setCustomers(Hashtable<String, Integer> customers) {
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
    public void setTimeStep(Time timeStep) {
        this.timeStep = timeStep;
    }

    public Time getTimeStep() {
        return timeStep;
    }
}
