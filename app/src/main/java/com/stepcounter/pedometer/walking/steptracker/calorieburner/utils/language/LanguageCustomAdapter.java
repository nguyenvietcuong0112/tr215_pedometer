package com.stepcounter.pedometer.walking.steptracker.calorieburner.utils.language;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.stepcounter.pedometer.walking.steptracker.calorieburner.R;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.adapter.LanguageModel;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.databinding.ItemCustomLanguageBinding;

import java.util.ArrayList;

public class LanguageCustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<LanguageModel> data = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private int checkedPosition = -1;


    public LanguageCustomAdapter( ArrayList<LanguageModel> newData) {

        this.data = newData;

    }

    public void updateData(ArrayList<LanguageModel> newData) {
        data.clear();
        if (newData != null && !newData.isEmpty()) {
            data.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCustomLanguageBinding binding = ItemCustomLanguageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        return new TabItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((TabItemViewHolder) holder).bind(position);
    }

    class TabItemViewHolder extends RecyclerView.ViewHolder {

        private ItemCustomLanguageBinding binding;

        public TabItemViewHolder(ItemCustomLanguageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }

        public void bind(int position) {
            LanguageModel languageModel = data.get(position);
            binding.tvTitle.setText(languageModel.getLanguageName());
            if(languageModel.getImage()==0){
                binding.ivAvatar.setVisibility(View.GONE);
            }else {
                binding.ivAvatar.setVisibility(View.VISIBLE);
                binding.ivAvatar.setImageResource(languageModel.getImage());
            }

            if (checkedPosition == -1) {
                binding.imgSelected.setImageDrawable(null);
            } else {
                if (checkedPosition == getAdapterPosition()) {
                    binding.imgSelected.setImageResource(R.drawable.ic_checked_language);
                } else {
                    binding.imgSelected.setImageDrawable(null);
                }
            }
            binding.getRoot().setOnClickListener(v -> {
                onItemClickListener.onPreviousPosition(checkedPosition);
                binding.imgSelected.setImageResource(R.drawable.ic_checked_language);
                if (checkedPosition != getAdapterPosition()) {
                    notifyItemChanged(checkedPosition);
                    checkedPosition = getAdapterPosition();
                    onItemClickListener.onItemNewClick(position, languageModel, LanguageCustomAdapter.this);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemClickListener {
        void onItemNewClick(int pos, LanguageModel itemTabModel,LanguageCustomAdapter adapter);

        void onPreviousPosition(int pos);
    }

    public void unselectAll() {
        checkedPosition = -1;
        notifyDataSetChanged();
    }
}
