package com.example.petrolnavigatorapp.utils;

import java.util.List;

public class UserReport<T> {

    private String targetType;
    private String targetName;
    private List<String> senders;
    private String lastReportDate;
    private T data;
    private int counter;

    public UserReport(String targetType, String targetName, List<String> senders){
        this.targetType = targetType;
        this.targetName = targetName;
        this.senders = senders;
        counter = 0;
    }

    public UserReport(String targetType, String targetName, List<String> senders, T data, int counter){
        this.targetType = targetType;
        this.targetName = targetName;
        this.senders = senders;
        this.data = data;
        this.counter = counter;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public List<String> getSenders() {
        return senders;
    }

    public void setSenders(List<String> senders) {
        this.senders = senders;
    }

    public String getLastReportDate() {
        return lastReportDate;
    }

    public void setLastReportDate(String lastReportDate) {
        this.lastReportDate = lastReportDate;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
