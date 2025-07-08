// [1] LeaderboardAdapter.java
package com.example.birdquest.adapters; // Sau pachetul tău

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.birdquest.R; // Asigură-te că R este importat corect
import com.example.birdquest.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();
    private String currentSortCriteria = "Nivel"; // Default sau actualizat din Activity

    public LeaderboardAdapter() {}

    public void setUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged(); // Sau folosește DiffUtil pentru performanță mai bună
    }

    public void setCurrentSortCriteria(String criteria) {
        this.currentSortCriteria = criteria;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, position + 1, currentSortCriteria);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvUserEmail, tvScoreLabel, tvScoreValue;
        ImageView ivUserAvatar; // Poți adăuga logica pentru a încărca un avatar real dacă ai URL-uri

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvScoreLabel = itemView.findViewById(R.id.tvScoreLabel);
            tvScoreValue = itemView.findViewById(R.id.tvScoreValue);
        }

        public void bind(User user, int rank, String sortCriteria) {
            tvRank.setText(String.format(Locale.getDefault(), "%d.", rank));
            tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "Utilizator Anonim");

            switch (sortCriteria) {
                case "Păsări":
                    tvScoreLabel.setText("Păsări:");
                    tvScoreValue.setText(String.valueOf(user.getUniqueCorrectBirdsIdentifiedCount()));
                    break;
                case "Quizuri":
                    tvScoreLabel.setText("Quizuri Perfecte:");
                    tvScoreValue.setText(String.valueOf(user.getPerfectQuizScores()));
                    break;
                case "Nivel": // Default
                default:
                    tvScoreLabel.setText("Nivel:");
                    tvScoreValue.setText(String.valueOf(user.getLevel()));
                    break;
            }
        }
    }
}