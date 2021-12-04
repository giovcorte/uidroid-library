package com.uidroid.uidroid.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.uidroid.uidroid.DatabindingContext;
import com.uidroid.uidroid.model.ViewConfiguration;

public class GenericPagerAdapter extends PagerAdapter {

    protected final DatabindingContext databindingContext;
    protected final ViewConfiguration configuration;

    protected final ViewConfiguration.IViewConfigurationFilter filter;

    public GenericPagerAdapter(DatabindingContext databindingContext,
                               ViewConfiguration configuration,
                               ViewConfiguration.IViewConfigurationFilter filter) {
        this.configuration = configuration;
        this.databindingContext = databindingContext;
        this.filter = filter;
    }

    public int getCount() {
        return configuration.getChildrenConfigurations(filter).size();
    }

    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        final ViewConfiguration model = configuration.getChildrenConfigurations(filter).get(position);
        final View view = databindingContext.buildView(container.getContext(), model.getViewType());

        container.addView(view);
        databindingContext.bindViewToConfiguration(view,
                configuration.getChildrenConfigurations(filter).get(position));

        return view;
    }

    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

}
