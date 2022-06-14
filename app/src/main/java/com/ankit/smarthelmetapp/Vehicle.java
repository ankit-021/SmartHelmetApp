package com.ankit.smarthelmetapp;

public class Vehicle {
    String name;
    String id;
    String location;
    String Imgid;

    public Vehicle(){}

    public Vehicle(String name, String id, String location, String imgid) {
        this.name = name;
        this.id = id;
        this.location = location;
        Imgid = imgid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImgid() {
        return Imgid;
    }

    public void setImgid(String imgid) {
        Imgid = imgid;
    }
}
