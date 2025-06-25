package com.example.faceit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecentCheckInsAdapter extends RecyclerView.Adapter<RecentCheckInsAdapter.CheckInViewHolder> {

    private List<RecentCheckIn> checkIns;

    public RecentCheckInsAdapter(List<RecentCheckIn> checkIns) {
        this.checkIns = checkIns;
    }

    @NonNull
    @Override
    public CheckInViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_checkin, parent, false);
        return new CheckInViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckInViewHolder holder, int position) {
        RecentCheckIn checkIn = checkIns.get(position);

        holder.studentNameText.setText(checkIn.getStudentName());
        holder.matricNumberText.setText(checkIn.getMatricNumber());
        holder.checkInTimeText.setText(checkIn.getCheckInTime());

        // Set status with color coding
        String status = checkIn.getStatus();
        holder.statusText.setText(status);

        if ("PRESENT".equals(status)) {
            holder.statusText.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else if ("PRESENT_LATE".equals(status)) {
            holder.statusText.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
        } else {
            holder.statusText.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
        }
    }

    @Override
    public int getItemCount() {
        return checkIns.size();
    }

    static class CheckInViewHolder extends RecyclerView.ViewHolder {
        TextView studentNameText, matricNumberText, checkInTimeText, statusText;

        public CheckInViewHolder(@NonNull View itemView) {
            super(itemView);
            studentNameText = itemView.findViewById(R.id.student_name_text);
            matricNumberText = itemView.findViewById(R.id.matric_number_text);
            checkInTimeText = itemView.findViewById(R.id.checkin_time_text);
            statusText = itemView.findViewById(R.id.status_text);
        }
    }
}