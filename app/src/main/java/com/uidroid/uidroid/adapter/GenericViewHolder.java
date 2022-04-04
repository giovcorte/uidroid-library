package com.uidroid.uidroid.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uidroid.uidroid.IData;
import com.uidroid.uidroid.IView;

/**
 * Class representing the only RecyclerView.ViewHolder needed. It simply holds the view and the
 * corresponding data.
 */
public final class GenericViewHolder extends RecyclerView.ViewHolder {

    public IData data;
    public IView view;

    public GenericViewHolder(@NonNull IView view, IData data) {
        super((View) view);
        itemView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        this.data = data;
        this.view = view;
    }

}
