package com.uidroid.uidroid.model;

import static com.uidroid.uidroid.binder.RecyclerViewBinder.*;

import androidx.recyclerview.widget.RecyclerView;

import com.uidroid.annotation.UI;

import java.util.List;

@UI.ViewConfiguration(view = RecyclerView.class)
public class RecyclerViewElement {

    @UI.ConfigurationsList(key = RECYCLER_VIEW_LIST)
    public List<Object> list;

    @UI.Id
    public String id;

    @UI.Param(key = RECYCLER_VIEW_COLUMNS)
    public Integer columns;

    @UI.Param(key = RECYCLER_VIEW_ITEM_SPAN_SIZE)
    public Integer column;

    @UI.Param(key = RECYCLER_VIEW_TYPE)
    public String type;

}
