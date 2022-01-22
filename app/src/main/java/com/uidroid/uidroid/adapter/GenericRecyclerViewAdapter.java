package com.uidroid.uidroid.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uidroid.uidroid.DatabindingContext;
import com.uidroid.uidroid.model.ViewConfiguration;

import java.util.List;

/**
 * The only adapter needed for bind any ViewConfiguration object into an Android RecyclerView.
 */
@SuppressWarnings("unused")
public class GenericRecyclerViewAdapter extends RecyclerView.Adapter<GenericViewHolder> {

    private final DatabindingContext databindingContext;
    private final ViewConfiguration configuration;

    private ViewConfiguration.IViewConfigurationFilter filter;

    /**
     * Constructor.
     *
     * @param databindingContext DatabindingContext.
     * @param configuration IViewConfiguration
     */
    public GenericRecyclerViewAdapter(DatabindingContext databindingContext,
                                      ViewConfiguration configuration,
                                      ViewConfiguration.IViewConfigurationFilter filter) {
        this.databindingContext = databindingContext;
        this.configuration = configuration;
        this.filter = filter;
    }

    @NonNull
    @Override
    public GenericViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final ViewConfiguration holderConfiguration = configuration.getChildrenConfigurations(filter).get(viewType);
        final View view = databindingContext.buildView(parent.getContext(), holderConfiguration.getViewType());

        return new GenericViewHolder(view, holderConfiguration);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
        databindingContext.bindViewToConfiguration(holder.itemView, holder.model);
    }

    @Override
    public void onViewRecycled(@NonNull GenericViewHolder holder) {
        databindingContext.unbindView(holder.itemView, holder.model);
        super.onViewRecycled(holder);
    }

    /**
     * Adds a list of UI.ViewConfiguration annotated object to the current recycler view.
     *
     * @param list Generic list of UI.ViewConfiguration annotated object
     */
    public void addItems(String key, List<Object> list) {
        for (Object object: list) {
            addItem(key, object);
        }
    }

    /**
     * Appends a new child IViewConfiguration to the root configuration and displays it, if the key
     * is admissible by the adapter filter
     *
     * @param key String representing the key for the given child Configuration.
     * @param childConfiguration IViewConfiguration representing the new item for the list.
     */
    public void addItem(String key, ViewConfiguration childConfiguration) {
        configuration.addChildConfiguration(key, childConfiguration);
        databindingContext.runOnUIThread(() ->
                notifyItemInserted(configuration.getChildrenConfigurations(filter).size()));
    }

    /**
     * Appends a new child to the root configuration and displays it, if the key
     * is admissible by the adapter filter.
     *
     * @param key String representing the key for the given child Configuration.
     * @param object Generic UI.ViewConfiguration annotated object.
     */
    public void addItem(String key, Object object) {
        ViewConfiguration configuration = databindingContext.buildViewConfigurationForObject(object);

        if (configuration != null) {
            addItem(key, configuration);
        }
    }

    /**
     * Remove the item for the given key, if multiple match are found, removes the first only.
     *
     * @param key String representing the key for the given child Configuration.
     */
    public synchronized void removeItem(String key) {
        final int position = itemPositionByKey(key);

        if (position != -1) {
            configuration.removeChildByFilter((key1, model) -> key1.equals(key));

            notifyItemRemoved(position);
        }
    }

    /**
     * Removes the item in the specified position.
     *
     * @param position integer for the position desired.
     */
    public synchronized void removeItem(int position) {
        if (position >= 0 && position < configuration.getChildrenConfigurations(filter).size()) {
            configuration.removeChildByPosition(position, filter);

            notifyItemRemoved(position);
        }
    }

    /**
     * Finds and returns the position of the first element which has the specified key and satisfy
     * the provided filter.
     *
     * @param key String representing the key for the given child Configuration.
     * @return integer for the position.
     */
    public int itemPositionByKey(String key) {
        int position = 0;
        boolean found = false;

        for (ViewConfiguration configuration: configuration.getChildrenConfigurations(filter)) {
            if (configuration.getKey().equals(key)) {
                found = true;
                break;
            }

            position++;
        }

        return found ? position : -1;
    }

    @Override
    public int getItemCount() {
        return configuration.getChildrenConfigurations(filter).size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /**
     * Access to the displayed collection.
     *
     * @return the current children displayed, according to the adapter filter.
     */
    public List<ViewConfiguration> getItems() {
        return configuration.getChildrenConfigurations(filter);
    }

    /**
     * Access to the IViewConfiguration at the specified index.
     *
     * @param index integer representing the position of the desired element.
     * @return the IViewConfiguration at the given index.
     */
    public ViewConfiguration getItem(int index) {
        return configuration.getChildrenConfigurations(filter).get(index);
    }

    /**
     * Sets a new filter for the children configurations and updates the RecyclerView accord.
     *
     * @param filter ViewConfiguration.IViewConfigurationFilter filter object.
     */
    public void setFilter(ViewConfiguration.IViewConfigurationFilter filter) {
        this.filter = filter;
        notifyItemRangeChanged(0, configuration.getChildrenConfigurations(filter).size());
    }

}
