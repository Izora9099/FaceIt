package com.example.faceit;

import java.util.List;

// Main response for courses list
public class CoursesListResponse {
    private int count;
    private String next;
    private String previous;
    private List<Course> results;

    // Getters and setters
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public String getNext() { return next; }
    public void setNext(String next) { this.next = next; }

    public String getPrevious() { return previous; }
    public void setPrevious(String previous) { this.previous = previous; }

    public List<Course> getResults() { return results; }
    public void setResults(List<Course> results) { this.results = results; }
}