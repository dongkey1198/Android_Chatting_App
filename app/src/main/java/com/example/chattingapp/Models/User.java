package com.example.chattingapp.Models;

public class User {

    public String userId, name, age, email, imageURL, phone_num;

    public User(){}

    public User(String userId, String name, String age, String email, String imageURL, String phone_num){
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.email = email;
        this.imageURL = imageURL;
        this.phone_num = phone_num;
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

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getPhone_num() {
        return phone_num;
    }

    public void setPhone_num(String phone_num) {
        this.phone_num = phone_num;
    }
}
