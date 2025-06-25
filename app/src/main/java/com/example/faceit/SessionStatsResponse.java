package com.example.faceit;

import java.util.List;

public class SessionStatsResponse {
    private int totalStudents;
    private int presentCount;
    private int lateCount;
    private int absentCount;
    private List<RecentCheckIn> recentCheckIns;
    private String sessionId;
    private String sessionStatus; // "ACTIVE", "ENDED"
    private long remainingTimeMs;

    // Constructor
    public SessionStatsResponse() {}

    // Getters and setters
    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }

    public int getLateCount() { return lateCount; }
    public void setLateCount(int lateCount) { this.lateCount = lateCount; }

    public int getAbsentCount() { return absentCount; }
    public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }

    public List<RecentCheckIn> getRecentCheckIns() { return recentCheckIns; }
    public void setRecentCheckIns(List<RecentCheckIn> recentCheckIns) { this.recentCheckIns = recentCheckIns; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(String sessionStatus) { this.sessionStatus = sessionStatus; }

    public long getRemainingTimeMs() { return remainingTimeMs; }
    public void setRemainingTimeMs(long remainingTimeMs) { this.remainingTimeMs = remainingTimeMs; }
}