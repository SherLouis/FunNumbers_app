package com.llightning.funnumbers;

public class Date {
    private int year = -1;
    private int month = -1;
    private int day = -1;

    public Date(int year, int month, int day){
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public Date(){}

    public Date(int dayNumber){
        int[] daysInMonths={31,29,31,30,31,30,31,31,30,31,30,31};
        int cumulDays = 0;
        for(int i=0; i<daysInMonths.length; i++){
            if(dayNumber <= cumulDays+daysInMonths[i]){
                this.month = i+1;
                this.day = dayNumber - cumulDays - 1;
                break;
            }
            cumulDays += daysInMonths[i];
        }
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }
}
