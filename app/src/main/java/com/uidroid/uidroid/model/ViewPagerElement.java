package com.uidroid.uidroid.model;

import static com.uidroid.uidroid.binder.ViewPagerBinder.VIEW_PAGER_LIST;

import androidx.viewpager.widget.ViewPager;

import com.uidroid.annotation.UI;

import java.util.List;

@UI.ViewConfiguration(view = ViewPager.class)
public class ViewPagerElement {

    @UI.ConfigurationsList(key = VIEW_PAGER_LIST)
    public List<Object> list;

}
