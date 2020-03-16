package com.bahador.marketprediction;

class LocalSelectedDate {
    private int Year, Month, Day;

    LocalSelectedDate(int year, int month, int day) {
        Year = year;
        Month = month;
        Day = day;
    }

    public int getYear() {
        return Year;
    }

    public void setYear(int year) {
        Year = year;
    }

    public int getMonth() {
        return Month;
    }

    public void setMonth(int month) {
        Month = month;
    }

    public int getDay() {
        return Day;
    }

    public void setDay(int day) {
        Day = day;
    }
}
