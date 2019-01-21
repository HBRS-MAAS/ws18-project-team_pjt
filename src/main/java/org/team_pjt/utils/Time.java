package org.team_pjt.utils;

public class Time {
    private int day;
    private int hour;
    private int minute;

    public Time (int day, int hour, int minute){
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }
    public Time (String timeString){
        String[] time = timeString.split("\\.");
        this.day = Integer.parseInt(time[0]);
        this.hour = Integer.parseInt(time[1]);
        this.minute = Integer.parseInt(time[2]);
    }
    public Time (int timeSteps, Time singleTimeStep) {
        this.day = 0;
        this.hour = 0;
        this.minute = 0;
        for (int i = 0; i < timeSteps; i++) {
            this.add(singleTimeStep);
        }
    }
    public Time (){
        this.day = 0;
        this.hour = 0;
        this.minute = 0;
    }

    public Time clone() {
        Time t = new Time(this.day, this.hour, this.minute);
        return t;
    }

    public void add(Time t) {
        this.day += t.getDay();
        this.hour += t.getHour();
        this.minute += t.getMinute();
        if (this.minute >= 60) {
            this.minute -= 60;
            this.hour += 1;
        }
        if (this.hour >= 24) {
            this.hour -= 24;
            this.day += 1;
        }
    }

    public boolean equals(Time t) {
        return (this.day == t.getDay() && this.hour == t.getHour() && this.minute == t.getMinute());
    }

    public boolean lessThan(Time t){
        return (this.day < t.getDay() || (this.day == t.getDay() && this.hour < t.getHour()) || (this.day == t.getDay() && this.hour == t.getHour() && this.minute < t.getMinute()));
    }

    public boolean greaterThan(Time t){
        return (this.day > t.getDay() || (this.day == t.getDay() && this.hour > t.getHour()) || (this.day == t.getDay() && this.hour == t.getHour() && this.minute > t.getMinute()));
    }

    public String toString(){
        String s = "";
        s += String.format("%03d", this.day);
        s += ".";
        s += String.format("%02d", this.hour);
        s += ".";
        s += String.format("%02d", this.minute);
        return s;
    }

    public int getDay() {
        return day;
    }
    public void setDay(int day) {
        this.day = day;
    }
    public int getHour() {
        return hour;
    }
    public void setHour(int hour) {
        this.hour = hour;
    }
    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getMinute() {
        return minute;
    }
}
