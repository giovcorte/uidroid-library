package com.uidroid.uidroid.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uidroid.uidroid.model.ViewConfiguration;

/**
 * Class representing the only RecyclerView.ViewHolder needed. It simply holds the view and the
 * corresponding data.
 */
public class GenericViewHolder extends RecyclerView.ViewHolder {

    public ViewConfiguration model;

    public GenericViewHolder(@NonNull View itemView, ViewConfiguration model) {
        super(itemView);
        this.itemView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        this.model = model;
    }

}
