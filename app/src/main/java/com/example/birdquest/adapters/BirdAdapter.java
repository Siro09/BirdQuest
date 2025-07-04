package com.example.birdquest.adapters; // Or your appropriate package

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
import com.example.birdquest.models.Bird;

import java.util.ArrayList;
import java.util.List;

public class BirdAdapter extends RecyclerView.Adapter<BirdAdapter.BirdViewHolder> {

    private List<Bird> birdList = new ArrayList<>();
    private Context context;

    public BirdAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public BirdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bird, parent, false);
        return new BirdViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BirdViewHolder holder, int position) {
        Bird currentBird = birdList.get(position);
        holder.textViewCommonName.setText(currentBird.getCommonName());
        holder.textViewLatinName.setText(currentBird.getLatinName());

        // Load image using Glide
        if (currentBird.imageUrl!=null  ) {
            Glide.with(context)
                    .load(currentBird.getImageUrl())
                    .placeholder(R.drawable.ic_bird_placeholder) // Optional: a placeholder image
                    .error(R.drawable.ic_launcher_foreground) // Optional: an error image
                    .into(holder.imageViewBird);
        } else {
            // Set a default image if URL is null or empty
            holder.imageViewBird.setImageResource(R.drawable.ic_bird_placeholder); // Create this drawable
        }
    }

    @Override
    public int getItemCount() {
        return birdList.size();
    }

    public void setBirds(List<Bird> birds) {
        this.birdList = birds;
        notifyDataSetChanged(); // Notify adapter that data has changed
    }

    static class BirdViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewBird;
        private TextView textViewCommonName;
        private TextView textViewLatinName;

        public BirdViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewBird = itemView.findViewById(R.id.imageViewBird);
            textViewCommonName = itemView.findViewById(R.id.textViewCommonName);
            textViewLatinName = itemView.findViewById(R.id.textViewLatinName);

            // You can set an OnClickListener for the item here if needed
            // itemView.setOnClickListener(v -> { /* Handle item click */ });
        }
    }
}