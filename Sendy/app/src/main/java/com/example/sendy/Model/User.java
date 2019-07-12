package com.example.sendy.Model;

public class User {

    public static String Email,Password;

    public User() {
    }

    public  User(String Email,String Password){
      this.Email =Email;
      this.Password = Password;

    }

    public static String getEmail() {
        return Email;
    }

    public static void setEmail(String email) {
        Email = email;
    }

    public static String getPassword() {
        return Password;
    }

    public static void setPassword(String password) {
        Password = password;
    }
}
