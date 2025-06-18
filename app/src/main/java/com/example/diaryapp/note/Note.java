package com.example.diaryapp.note;

public class Note {
    public String noteId, title,  message, date, imageBase64, userId, locationName;
    double latitude, longitude;

    public Note() {}

    public Note(String noteId, String title, String message, String date, String imageBase64, String userId, double latitude, double longitude, String locationName) {
        this.noteId = noteId;
        this.title = title;
        this.message = message;
        this.date = date;
        this.imageBase64 = imageBase64;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
