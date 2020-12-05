package com.example.petrolnavigatorapp.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Fuel {
    private int icon;
    private String name;
    private String type;
    private String price;
    private int reportCounter;
    private String lastReportDate;

    public Fuel(int i, String price, String name, String type) {
        this.icon = i;
        this.name = name;
        this.price = price;
        this.type = type;
        reportCounter = 0;

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        lastReportDate = dateFormat.format(date);
    }

    public Fuel(int i, String price, String name, String type, int reportCounter, String lastReportDate) {
        this.icon = i;
        this.name = name;
        this.price = price;
        this.type = type;
        this.reportCounter = reportCounter;
        this.lastReportDate = lastReportDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getReportCounter() {
        return reportCounter;
    }

    public void setReportCounter(int reportCounter) {
        this.reportCounter = reportCounter;
    }

    public String getLastReportDate() {
        return lastReportDate;
    }

    public void setLastReportDate(String lastReportDate ) {
        this.lastReportDate = lastReportDate ;
    }
}
