package com.example.imageuploaderapp;

public class FarmerData {
    private String name;
    private String address;
    private String cropType;
    private String livestockType;

    // Default constructor required for calls to DataSnapshot.getValue(FarmerData.class)
    public FarmerData() {
    }

    public FarmerData(String name, String address, String cropType, String livestockType) {
        this.name = name;
        this.address = address;
        this.cropType = cropType;
        this.livestockType = livestockType;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCropType() {
        return cropType;
    }

    public void setCropType(String cropType) {
        this.cropType = cropType;
    }

    public String getLivestockType() {
        return livestockType;
    }

    public void setLivestockType(String livestockType) {
        this.livestockType = livestockType;
    }
}
