package com.example.faceit;

import java.util.List;

public class CurrentCoursesResponse {
    private boolean success;
    private List<Course> availableCourses;
    private String currentTime;
    private String message;

    // Constructor
    public CurrentCoursesResponse() {}

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public List<Course> getAvailableCourses() { return availableCourses; }
    public void setAvailableCourses(List<Course> availableCourses) { this.availableCourses = availableCourses; }

    public String getCurrentTime() { return currentTime; }
    public void setCurrentTime(String currentTime) { this.currentTime = currentTime; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

}