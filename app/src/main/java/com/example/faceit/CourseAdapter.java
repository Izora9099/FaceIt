package com.example.faceit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courses;
    private OnCourseClickListener listener;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public CourseAdapter(List<Course> courses, OnCourseClickListener listener) {
        this.courses = courses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course, listener);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        private TextView courseCodeText;
        private TextView courseNameText;
        private TextView courseDetailsText;
        private TextView departmentText;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCodeText = itemView.findViewById(R.id.course_code_text);
            courseNameText = itemView.findViewById(R.id.course_name_text);
            courseDetailsText = itemView.findViewById(R.id.course_details_text);
            departmentText = itemView.findViewById(R.id.department_text);
        }

        public void bind(Course course, OnCourseClickListener listener) {
            // Set course information
            courseCodeText.setText(course.getCourse_code());
            courseNameText.setText(course.getCourse_name());

            // Create details string
            String details = course.getCredits() + " credits";
            if (course.getLevel_name() != null && !course.getLevel_name().isEmpty()) {
                details += " • Level " + course.getLevel_name();
            }
            courseDetailsText.setText(details);

            // Set department
            if (course.getDepartment_name() != null && !course.getDepartment_name().isEmpty()) {
                departmentText.setText("📚 " + course.getDepartment_name());
                departmentText.setVisibility(View.VISIBLE);
            } else {
                departmentText.setVisibility(View.GONE);
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(course);
                }
            });

            // Add visual feedback for click
            itemView.setOnClickListener(v -> {
                // Add ripple effect or animation here if needed
                if (listener != null) {
                    listener.onCourseClick(course);
                }
            });
        }
    }
}