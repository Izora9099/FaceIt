package com.example.faceit;

public class RecentCheckIn {
    private String studentId;
    private String studentName;
    private String matricNumber;
    private String checkInTime;
    private String status; // "PRESENT", "PRESENT_LATE"
    private String profileImageUrl;

    // Constructor
    public RecentCheckIn() {}

    public RecentCheckIn(String studentId, String studentName, String matricNumber,
                         String checkInTime, String status, String profileImageUrl) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.matricNumber = matricNumber;
        this.checkInTime = checkInTime;
        this.status = status;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters and setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getMatricNumber() { return matricNumber; }
    public void setMatricNumber(String matricNumber) { this.matricNumber = matricNumber; }

    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
