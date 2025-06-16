package com.example.predictibill.models;

import java.util.Date;
import com.google.firebase.Timestamp;

public class Subscription {
    private String id;
    private String userId;
    private String name;
    private double price;
    private String category;
    private String status;
    private String billingCycle;
    private String startDate;
    private String nextBillingDate;
    private String paymentMethod;
    private String note;
    private Date createdAt;
    private String dueDate;
    private String subscriptionId;

    private Timestamp updatedAt;

    private String lastPaidDue;

    public Subscription() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(String nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getLastPaidDue() {
        return lastPaidDue;
    }

    public void setLastPaidDue(String lastPaidDue) {
        this.lastPaidDue = lastPaidDue;
    }


    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

}