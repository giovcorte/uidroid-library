package com.uidroid.uidroid.binder;

import androidx.viewpager.widget.ViewPager;

import com.uidroid.annotation.UI;
import com.uidroid.uidroid.DatabindingContext;
import com.uidroid.uidroid.adapter.GenericPagerAdapter;
import com.uidroid.uidroid.model.ViewConfiguration;

@SuppressWarnings("unused")
public class ViewPagerBinder<V extends ViewPager> extends ViewBinder<V> {

    public static final String VIEW_PAGER_LIST = "viewPagerList";

    protected GenericPagerAdapter adapter;
    protected ViewConfiguration.IViewConfigurationFilter filter;

    @Override
    public void doBind(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        handleFilter(view, configuration, databindingContext);
        handleAdapter(view, configuration, databindingContext);

        view.setAdapter(adapter);
    }

    protected void handleFilter(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        filter = (key, configuration1) -> key.equals(VIEW_PAGER_LIST);
    }

    protected void handleAdapter(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        adapter = new GenericPagerAdapter(databindingContext, configuration, filter);
    }

    @Override
    public void doUnbind(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        adapter = null;
    }

    @Override
    public void doRemove(DatabindingContext databindingContext, ViewConfiguration configuration) {
        adapter = null;
    }

    public GenericPagerAdapter getAdapter() {
        return adapter;
    }

}
