package com.example.plantcareapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.plantcareapp.R;
import com.example.plantcareapp.models.Plant;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PlantsAdapter extends RecyclerView.Adapter<PlantsAdapter.PlantViewHolder> {
    private List<Plant> plants;
    private final OnPlantActionListener actionListener;

    public interface OnPlantActionListener {
        void onPlantClick(Plant plant);
        void onPlantDelete(Plant plant);
    }


    public PlantsAdapter(List<Plant> plants, OnPlantActionListener actionListener) {
        this.plants = plants;
        this.actionListener = actionListener;
    }


    public PlantsAdapter(List<Plant> plants,
                         OnPlantClickListener clickListener,
                         OnPlantDeleteListener deleteListener) {
        this(plants, new OnPlantActionListener() {
            @Override
            public void onPlantClick(Plant plant) {
                clickListener.onPlantClick(plant);
            }
            @Override
            public void onPlantDelete(Plant plant) {
                deleteListener.onPlantDelete(plant);
            }
        });
    }


    public interface OnPlantClickListener {
        void onPlantClick(Plant plant);
    }

    public interface OnPlantDeleteListener {
        void onPlantDelete(Plant plant);
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plants.get(position);
        holder.bind(plant, actionListener);
    }

    @Override
    public int getItemCount() {
        return plants.size();
    }

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
        notifyDataSetChanged();
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView lastWateredTextView;
        private final ImageButton deleteButton;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.plant_name);
            lastWateredTextView = itemView.findViewById(R.id.last_watered);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }

        public void bind(Plant plant, OnPlantActionListener actionListener) {
            nameTextView.setText(plant.getName());

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String lastWatered = "Последний полив: " + sdf.format(plant.getLastWateredDate());
            lastWateredTextView.setText(lastWatered);

            itemView.setOnClickListener(v -> actionListener.onPlantClick(plant));
            deleteButton.setOnClickListener(v -> actionListener.onPlantDelete(plant));
        }


        public void bind(Plant plant,
                         OnPlantClickListener clickListener,
                         OnPlantDeleteListener deleteListener) {
            bind(plant, new OnPlantActionListener() {
                @Override
                public void onPlantClick(Plant p) {
                    clickListener.onPlantClick(p);
                }
                @Override
                public void onPlantDelete(Plant p) {
                    deleteListener.onPlantDelete(p);
                }
            });
        }
    }
}