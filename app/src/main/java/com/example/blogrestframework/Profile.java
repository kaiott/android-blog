package com.example.blogrestframework;

public class Profile {
    int id;
    int userId;
    String username;
    String imageUrl;

    public Profile(int id, int userId, String username, String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.imageUrl = imageUrl;
    }
}
