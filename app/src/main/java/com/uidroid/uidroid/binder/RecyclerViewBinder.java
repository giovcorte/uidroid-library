package com.uidroid.uidroid.binder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.uidroid.annotation.UI;
import com.uidroid.uidroid.DatabindingContext;
import com.uidroid.uidroid.adapter.GenericRecyclerViewAdapter;
import com.uidroid.uidroid.model.ViewConfiguration;

public class RecyclerViewBinder<V extends RecyclerView, L extends RecyclerViewBinder.IRecyclerViewBinderListener> extends ListenerViewBinder<V, L> {

    public static class Type {
        public final static String VERTICAL = "recyclerViewVertical";
        public final static String HORIZONTAL = "recyclerViewHorizontal";
        public final static String GRID = "recyclerViewGrid";
        public final static String HORIZONTAL_SNAPPING = "recyclerViewHorizontalSnapping";
    }

    public final static String RECYCLER_VIEW_TYPE = "recyclerViewType";
    public final static String RECYCLER_VIEW_ITEM_SPAN_SIZE = "recyclerViewItemSpanSize";
    public final static String RECYCLER_VIEW_COLUMNS = "recyclerViewColumns";
    public final static String RECYCLER_VIEW_LIST = "recyclerViewList";

    protected int currentPosition = 0;

    protected ViewConfiguration.IViewConfigurationFilter filter;

    protected GenericRecyclerViewAdapter adapter;
    protected LinearLayoutManager layoutManager;

    public interface IRecyclerViewBinderListener {
        void onBottomScrolled();
    }

    public RecyclerViewBinder() {
        super();
    }

    @Override
    public void doBind(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        handleFilter(view, configuration, databindingContext);
        handleAdapter(view, configuration, databindingContext);
        handleType(view, configuration, databindingContext);
        handleScrollListener(view, configuration, databindingContext);
        handleItemAnimator(view, configuration, databindingContext);

        view.setLayoutManager(layoutManager);
        view.setAdapter(adapter);
        view.scrollToPosition(currentPosition);
    }

    protected void handleType(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        final String type = configuration.getStringParam(RECYCLER_VIEW_TYPE, Type.VERTICAL);

        if (type.equals(Type.VERTICAL)) {
            handleVertical(view, configuration, databindingContext);
        }

        if (type.equals(Type.HORIZONTAL)) {
            handleHorizontal(view, configuration, databindingContext);
        }

        if (type.equals(Type.GRID)) {
            handleGrid(view, configuration, databindingContext);
        }

        if (type.equals(Type.HORIZONTAL_SNAPPING)) {
            handleHorizontalSnapping(view, configuration, databindingContext);
        }
    }

    @SuppressWarnings("unused")
    protected void handleGrid(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        layoutManager = new GridLayoutManager(view.getContext(),
                configuration.getIntegerParam(RECYCLER_VIEW_COLUMNS, 1));
        ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItem(position).getIntegerParam(RECYCLER_VIEW_ITEM_SPAN_SIZE,
                                configuration.getIntegerParam(RECYCLER_VIEW_COLUMNS, 1));
            }
        });
    }

    @SuppressWarnings("unused")
    protected void handleVertical(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        layoutManager = new LinearLayoutManager(view.getContext());
    }

    @SuppressWarnings("unused")
    protected void handleHorizontal(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        layoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);
    }

    @SuppressWarnings("unused")
    protected void handleHorizontalSnapping(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        layoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);

        final SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(view);
    }

    @SuppressWarnings("unused")
    protected void handleFilter(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        filter = (key, configuration1) -> key.equals(RECYCLER_VIEW_LIST);
    }

    @SuppressWarnings("unused")
    protected void handleAdapter(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        adapter = new GenericRecyclerViewAdapter(databindingContext, configuration, filter);
    }

    @SuppressWarnings("unused")
    protected void handleScrollListener(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        view.addOnScrollListener(new RecyclerViewConfiguratorScrollListener());
    }

    @SuppressWarnings("unused")
    protected void handleItemAnimator(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        view.setItemAnimator(null);
    }

    @Override
    public void doUnbind(RecyclerView view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        if (adapter != null && layoutManager != null) {
            currentPosition = layoutManager.findFirstVisibleItemPosition();
            layoutManager = null;
            adapter = null;

            view.setOnScrollChangeListener(null);
        }

        unbindViewGroup(databindingContext, view, configuration, filter);
    }

    @Override
    public void doRemove(DatabindingContext databindingContext, ViewConfiguration configuration) {
        currentPosition = 0;
        layoutManager = null;
        adapter = null;
    }

    @SuppressWarnings("unused")
    public GenericRecyclerViewAdapter getAdapter() {
        return adapter;
    }

    @SuppressWarnings("unused")
    public void setFilter(ViewConfiguration.IViewConfigurationFilter filter) {
        this.filter = filter;
        adapter.setFilter(filter);
    }

    protected class RecyclerViewConfiguratorScrollListener extends RecyclerView.OnScrollListener {

        public RecyclerViewConfiguratorScrollListener() {
            super();
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            handleOnScrolled(recyclerView, dx, dy);
        }

        @SuppressWarnings("unused")
        protected void handleOnScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (layoutManager != null) {
                final int totalItemCount = layoutManager.getItemCount();
                final int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                final int visibleThreshold = 5;

                if (getListener() != null
                        && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    getListener().onBottomScrolled();
                }
            }
        }

    }

}
