package com.example.predictibill.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserModel {
    private String userId;
    private String name;
    private String email;
    @Exclude private String password;
    private Timestamp createdDate;

    public User() {}

    public User(String userId, String name, String email, Timestamp createdDate) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.createdDate = createdDate;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    @Exclude public String getPassword() { return password; }
    public Timestamp getCreatedDate() { return createdDate; }
}