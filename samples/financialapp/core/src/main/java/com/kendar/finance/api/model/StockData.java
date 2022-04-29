package com.kendar.finance.api.model;

public class StockData {
    private String id;
    private double price;
    private double changeInPercent;
    private double peg;
    private double dividend;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setChangeInPercent(double changeInPercent) {
        this.changeInPercent = changeInPercent;
    }

    public double getChangeInPercent() {
        return changeInPercent;
    }

    public void setPeg(double peg) {
        this.peg = peg;
    }

    public double getPeg() {
        return peg;
    }

    public void setDividend(double dividend) {
        this.dividend = dividend;
    }

    public double getDividend() {
        return dividend;
    }
}
