package com.uidroid.uidroid.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uidroid.uidroid.IAdapterDataBinding;
import com.uidroid.uidroid.IData;
import com.uidroid.uidroid.IView;
import com.uidroid.uidroid.IViewFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The only adapter needed for bind any ViewConfiguration object into an Android RecyclerView.
 */
@SuppressWarnings("unused")
public class GenericRecyclerViewAdapter extends RecyclerView.Adapter<GenericViewHolder> {

    private final IAdapterDataBinding dataBinding;
    private final IViewFactory viewFactory;
    
    private final List<IData> items;

    /**
     * Constructor.
     *
     * @param dataBinding IAdapterDataBinding dataBinding.
     * @param items List of IData objects.
     */
    public GenericRecyclerViewAdapter(IAdapterDataBinding dataBinding,
                                      IViewFactory viewFactory,
                                      List<IData> items) {
        this.dataBinding = dataBinding;
        this.viewFactory = viewFactory;
        this.items = items;
    }

    /**
     * Constructor.
     *
     * @param dataBinding IAdapterDataBinding dataBinding.
     */
    public GenericRecyclerViewAdapter(IAdapterDataBinding dataBinding,
                                      IViewFactory viewFactory) {
        this.dataBinding = dataBinding;
        this.viewFactory = viewFactory;
        this.items = new ArrayList<>();
    }

    @NonNull
    @Override
    public GenericViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final IData data = items.get(viewType);
        final IView view = viewFactory.build(data);

        return new GenericViewHolder(view, data);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
        dataBinding.bind(holder.view, holder.data);
    }

    /**
     * Adds a list of UI.ViewConfiguration annotated object to the current recycler view.
     *
     * @param newItems Generic list of IData objects
     */
    public void addItems(List<IData> newItems) {
        items.addAll(newItems);
    }

    /**
     * Appends a new child to the root configuration and displays it, if the key
     * is admissible by the adapter filter.
     *
     * @param data new IData object.
     */
    public void addItem(IData data) {
        items.add(data);
    }

    /**
     * Removes the item in the specified position.
     *
     * @param position integer for the position desired.
     */
    public synchronized void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);

            notifyItemRemoved(position);
        }
    }

    /**
     * Finds and returns the position of the first element which has the specified name.
     *
     * @param key String representing the name for the given IData element.
     * @return integer for the position.
     */
    public int itemPositionByKey(String key) {
        int position = 0;
        boolean found = false;

        for (IData data: items) {
            if (data.name().equals(key)) {
                found = true;
                break;
            }

            position++;
        }

        return found ? position : -1;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /**
     * Access to the displayed collection.
     *
     * @return the current children displayed.
     */
    public List<IData> getItems() {
        return items;
    }

    /**
     * Access to the IData at the specified index.
     *
     * @param index integer representing the position of the desired element.
     * @return the IData at the given index.
     */
    public IData getItem(int index) {
        return items.get(index);
    }

    /**
     * Access to the IData at the specified index.
     *
     * @param index integer representing the position of the desired element.
     * @param type class of the returned object.
     * @return the T object at the given index.
     */
    public <T> T getItem(int index, Class<? extends T> type) {
        IData data = getItem(index);

        if (data == null) {
            return null;
        }

        try {
            return type.cast(data);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Clears all the children and stops the tasks in each binder.
     */
    public void clearItems() {
        items.clear();
    }
    
}
