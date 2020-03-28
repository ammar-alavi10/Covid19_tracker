package com.ammar.myapplication;

public class User {

    private String username;
    private String password;
    private String email;
    private int status;

    public User(String username, String password, String email, int status) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}
