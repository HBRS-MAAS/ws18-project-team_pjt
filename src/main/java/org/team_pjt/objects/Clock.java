package org.team_pjt.objects;

public class Clock implements Comparable<Clock> {
    private int day;
    private int hour;

    public Clock() {
        this.day = 0;
        this.hour = 0;
    }

    public Clock(int day, int hour) {
        this.day = day;
        this.hour = hour;
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

    public void incr() {
        this.hour = (this.hour + 1) % 24;
        if (this.hour == 0) {
            this.day++;
        }
    }

    public String toString() {
        String systemClock = "<" + String.format("%1$" + 3 + "s", this.day).replace(' ', '0') + ":" +
                String.format("%1$" + 2 + "s", this.hour).replace(' ', '0') + ">";
        return systemClock;
    }

    @Override
    public int compareTo(Clock o) {
        if (day > o.getDay()) return 1;
        if (day == o.getDay() && hour > o.getHour()) return 1;
        if (day == o.getDay() && hour == o.getHour()) return 0;
        return -1;
    }
}
