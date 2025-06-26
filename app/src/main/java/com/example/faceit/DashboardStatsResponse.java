package com.example.faceit;

public class DashboardStatsResponse {
    private int total_students;
    private int total_courses;
    private int total_departments;
    private int total_attendance_today;

    // Teacher-specific fields
    private int my_courses;
    private int my_students;
    private int my_attendance_today;

    // Getters and setters
    public int getTotal_students() { return total_students; }
    public void setTotal_students(int total_students) { this.total_students = total_students; }

    public int getTotal_courses() { return total_courses; }
    public void setTotal_courses(int total_courses) { this.total_courses = total_courses; }

    public int getTotal_departments() { return total_departments; }
    public void setTotal_departments(int total_departments) { this.total_departments = total_departments; }

    public int getTotal_attendance_today() { return total_attendance_today; }
    public void setTotal_attendance_today(int total_attendance_today) { this.total_attendance_today = total_attendance_today; }

    public int getMy_courses() { return my_courses; }
    public void setMy_courses(int my_courses) { this.my_courses = my_courses; }

    public int getMy_students() { return my_students; }
    public void setMy_students(int my_students) { this.my_students = my_students; }

    public int getMy_attendance_today() { return my_attendance_today; }
    public void setMy_attendance_today(int my_attendance_today) { this.my_attendance_today = my_attendance_today; }
}