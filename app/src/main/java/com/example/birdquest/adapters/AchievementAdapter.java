package com.example.birdquest.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.birdquest.R;
import com.example.birdquest.models.Achievement;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private List<Achievement> achievementList;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());


    public AchievementAdapter(Context context, List<Achievement> achievementList) {
        this.context = context;
        this.achievementList = achievementList;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievementList.get(position);

        holder.tvAchievementName.setText(achievement.getName());
        holder.tvAchievementDescription.setText(achievement.getDescription());

        if (achievement.getXpReward() > 0) {
            holder.tvAchievementXpReward.setText(String.format(Locale.getDefault(), "+%d XP", achievement.getXpReward()));
            holder.tvAchievementXpReward.setVisibility(View.VISIBLE);
        } else {
            holder.tvAchievementXpReward.setVisibility(View.GONE);
        }

        if (achievement.getUnlockedAt() != null) {
            holder.tvAchievementDate.setText(dateFormat.format(achievement.getUnlockedAt()));
            holder.tvAchievementDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvAchievementDate.setVisibility(View.GONE);
        }

        // Handle Icon loading (example using Glide for URLs, or local drawables)
        if (achievement.getIconUrl() != null && !achievement.getIconUrl().isEmpty()) {
            Glide.with(context)
                    .load(achievement.getIconUrl())
                    .placeholder(R.drawable.ic_baseline_star_24) // Default placeholder
                    .error(R.drawable.ic_baseline_star_24)       // Error placeholder
                    .into(holder.ivAchievementIcon);
        } else {
            holder.ivAchievementIcon.setImageResource(R.drawable.ic_baseline_star_24); // Default
        }
    }

    @Override
    public int getItemCount() {
        return achievementList == null ? 0 : achievementList.size();
    }

    public void updateAchievements(List<Achievement> newAchievements) {
        this.achievementList.clear();
        if (newAchievements != null) {
            this.achievementList.addAll(newAchievements);
        }
        notifyDataSetChanged();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAchievementIcon;
        TextView tvAchievementName, tvAchievementDescription, tvAchievementXpReward, tvAchievementDate;

        AchievementViewHolder(View itemView) {
            super(itemView);
            ivAchievementIcon = itemView.findViewById(R.id.ivAchievementIcon);
            tvAchievementName = itemView.findViewById(R.id.tvAchievementName);
            tvAchievementDescription = itemView.findViewById(R.id.tvAchievementDescription);
            tvAchievementXpReward = itemView.findViewById(R.id.tvAchievementXpReward);
            tvAchievementDate = itemView.findViewById(R.id.tvAchievementDate);
        }
    }
}