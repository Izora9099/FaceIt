package com.example.faceit;

public class Course {
    private String id;
    private String name;
    private String code;
    private String timeSlot;
    private int duration;
    private boolean isCurrentlyScheduled;
    // getters and setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getTimeSlot() {
        return timeSlot;
    }
    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public boolean isCurrentlyScheduled() {
        return isCurrentlyScheduled;
    }
    public void setCurrentlyScheduled(boolean isCurrentlyScheduled) {
        this.isCurrentlyScheduled = isCurrentlyScheduled;
    }

}

