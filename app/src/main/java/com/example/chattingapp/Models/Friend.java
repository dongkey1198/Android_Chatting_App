package com.example.chattingapp.Models;

public class Friend {
    private String id;

    public Friend(){};

    public Friend(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
