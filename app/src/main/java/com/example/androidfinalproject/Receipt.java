package com.example.androidfinalproject;

public class Receipt {
    int id;
    String vendor;
    String date;
    double total;

    String category;

    public Receipt(int id, String vendor, String date, double total, String category) {
        this.id = id;
        this.vendor = vendor;
        this.date = date;
        this.total = total;
        this.category = category;
    }
}
