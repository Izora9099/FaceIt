package com.example.faceit;

public class Course {
    private int id;
    private String course_code;
    private String course_name;
    private int credits;
    private String description;
    private String department_name;
    private String level_name;
    private String status;
    private String created_at;

    // Default constructor
    public Course() {}

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCourse_code() { return course_code; }
    public void setCourse_code(String course_code) { this.course_code = course_code; }

    public String getCourse_name() { return course_name; }
    public void setCourse_name(String course_name) { this.course_name = course_name; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDepartment_name() { return department_name; }
    public void setDepartment_name(String department_name) { this.department_name = department_name; }

    public String getLevel_name() { return level_name; }
    public void setLevel_name(String level_name) { this.level_name = level_name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    // Helper methods
    public String getDisplayName() {
        return course_code + " - " + course_name;
    }

    public String getFullInfo() {
        return course_code + " - " + course_name + " (" + credits + " credits)";
    }

    // Additional helper methods for null safety
    public String getSafeDisplayName() {
        String code = (course_code != null) ? course_code : "Unknown";
        String name = (course_name != null) ? course_name : "Course";
        return code + " - " + name;
    }

    public boolean isActive() {
        return "active".equals(status);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}