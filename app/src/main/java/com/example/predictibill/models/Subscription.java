package com.example.predictibill.models;

public class Subscription {

    private String name;
    private String billingCycle;
    private String category;
    private String subscriptionId;
    private double price;
    private String dueDate;
    private String status;  // Example extra field (e.g., "paid", "unpaid")

    // No-argument constructor required for Firestore deserialization
    public Subscription() {
    }

    // Full constructor
    public Subscription(String name, String billingCycle, String category,
                        String subscriptionId, double price, String dueDate, String status) {
        this.name = name;
        this.billingCycle = billingCycle;
        this.category = category;
        this.subscriptionId = subscriptionId;
        this.price = price;
        this.dueDate = dueDate;
        this.status = status;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
