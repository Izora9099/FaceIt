package com.example.faceit;

import java.util.List;

public class TeacherAuthResponse {
    // JWT response fields from Django's TokenObtainPairView
    private String access;  // JWT access token
    private String refresh; // JWT refresh token

    // Additional fields we might add later
    private String teacherId;
    private String name;
    private List<String> assignedCourses;
    private boolean success;
    private String message;

    // Default constructor
    public TeacherAuthResponse() {}

    // Constructor with parameters
    public TeacherAuthResponse(String access, String refresh, String teacherId,
                               String name, List<String> assignedCourses, boolean success, String message) {
        this.access = access;
        this.refresh = refresh;
        this.teacherId = teacherId;
        this.name = name;
        this.assignedCourses = assignedCourses;
        this.success = success;
        this.message = message;
    }

    // Getters and setters for JWT fields
    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }

    // Legacy getters for compatibility (map to JWT fields)
    public String getToken() {
        return access; // Return access token as the main token
    }

    public void setToken(String token) {
        this.access = token;
    }

    // Other getters and setters
    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAssignedCourses() {
        return assignedCourses;
    }

    public void setAssignedCourses(List<String> assignedCourses) {
        this.assignedCourses = assignedCourses;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}