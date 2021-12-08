package com.uidroid.uidroid.binder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.uidroid.uidroid.DatabindingContext;
import com.uidroid.uidroid.adapter.GenericRecyclerViewAdapter;
import com.uidroid.uidroid.model.ViewConfiguration;

@SuppressWarnings("unused")
public class RecyclerViewBinder<V extends RecyclerView, L extends RecyclerViewBinder.IRecyclerViewBinderListener> extends ListenerViewBinder<V, L> {

    public static class Type {
        public final static String VERTICAL = "recyclerViewVertical";
        public final static String HORIZONTAL = "recyclerViewHorizontal";
        public final static String GRID = "recyclerViewGrid";
        public final static String HORIZONTAL_SNAPPING = "recyclerViewHorizontalSnapping";
    }

    public final static String RECYCLER_VIEW_POSITION = "recyclerViewPosition";
    public final static String RECYCLER_VIEW_TYPE = "recyclerViewType";
    public final static String RECYCLER_VIEW_ITEM_SPAN_SIZE = "recyclerViewItemSpanSize";
    public final static String RECYCLER_VIEW_COLUMNS = "recyclerViewColumns";
    public final static String RECYCLER_VIEW_LIST = "recyclerViewList";

    protected ViewConfiguration.IViewConfigurationFilter filter;

    protected GenericRecyclerViewAdapter adapter;
    protected LinearLayoutManager layoutManager;

    public interface IRecyclerViewBinderListener {
        void onBottomScrolled();
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
        view.scrollToPosition(configuration.getIntegerParam(RECYCLER_VIEW_POSITION, 0));
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

    protected void handleVertical(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        layoutManager = new LinearLayoutManager(view.getContext());
    }

    protected void handleHorizontal(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        layoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);
    }

    protected void handleHorizontalSnapping(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        layoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);

        final SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(view);
    }

    protected void handleFilter(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        filter = configuration1 -> configuration1.key().equals(RECYCLER_VIEW_LIST);
    }

    protected void handleAdapter(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        adapter = new GenericRecyclerViewAdapter(databindingContext, configuration, filter);
    }

    protected void handleScrollListener(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        view.addOnScrollListener(new RecyclerViewConfiguratorScrollListener());
    }

    protected void handleItemAnimator(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        view.setItemAnimator(null);
    }

    @Override
    public void doUnbind(RecyclerView view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        if (layoutManager != null) {
            configuration.putParam(RECYCLER_VIEW_POSITION, layoutManager.findFirstVisibleItemPosition());
            layoutManager = null;
            adapter = null;

            view.setOnScrollChangeListener(null);
        }
    }

    public GenericRecyclerViewAdapter getAdapter() {
        return adapter;
    }

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
