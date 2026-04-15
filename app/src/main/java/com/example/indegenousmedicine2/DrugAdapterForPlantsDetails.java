package com.example.indegenousmedicine2;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class DrugAdapterForPlantsDetails extends RecyclerView.Adapter<DrugAdapterForPlantsDetails.DrugViewHolder> {

    private Context context;
    private List<DrugDetail> drugList;

    public DrugAdapterForPlantsDetails(Context context, List<DrugDetail> drugList) {
        this.context = context;
        this.drugList = drugList;
    }

    @NonNull
    @Override
    public DrugViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_drug_detail, parent, false);
        return new DrugViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrugViewHolder holder, int position) {
        DrugDetail drugDetail = drugList.get(position);

        // Display the vernacular name as the main title
        holder.vernacularNameTextView.setText(drugDetail.getVernacularName());
        holder.vernacularNameTextView.setTypeface(null, Typeface.BOLD);


        // Display the scientific name
        holder.scientificNameTextView.setText("Scientific Name: " + drugDetail.getScientificName());

        // Emphasize medicinal use
        holder.medicinalUseTextView.setText(drugDetail.getMedicinalUse());

        // Load the image if available
        if (drugDetail.getPhotograph() != null && !drugDetail.getPhotograph().isEmpty()) {
            Glide.with(context).load(drugDetail.getPhotograph()).into(holder.photographImageView);
            holder.photographImageView.setVisibility(View.VISIBLE);
        } else {
            holder.photographImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return drugList.size();
    }

    public static class DrugViewHolder extends RecyclerView.ViewHolder {
        TextView vernacularNameTextView, scientificNameTextView, medicinalUseTextView;
        ImageView photographImageView;

        public DrugViewHolder(@NonNull View itemView) {
            super(itemView);
            vernacularNameTextView = itemView.findViewById(R.id.vernacularNameTextView);
            scientificNameTextView = itemView.findViewById(R.id.scientificNameTextView);
            medicinalUseTextView = itemView.findViewById(R.id.medicinalUseTextView);
            photographImageView = itemView.findViewById(R.id.photographImageView);
        }
    }
}
