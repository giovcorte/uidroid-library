package com.uidroid.uidroid;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.uidroid.uidroid.binder.IViewBinder;
import com.uidroid.uidroid.binder.ListenerViewBinder;
import com.uidroid.uidroid.factory.IViewBinderFactory;
import com.uidroid.uidroid.factory.IViewCompositeFactory;
import com.uidroid.uidroid.factory.IViewConfigurationFactory;
import com.uidroid.uidroid.factory.IViewFactory;
import com.uidroid.uidroid.handler.IClickHandler;
import com.uidroid.uidroid.handler.IViewAction;
import com.uidroid.uidroid.loader.IImageLoader;
import com.uidroid.uidroid.model.ViewConfiguration;
import com.uidroid.uidroid.view.ViewComposite;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Class which binds and unbinds data to Android views and custom views. The idea behind the
 * matching between the view with the corresponding data object and between the view's sub-views
 * with the object's sub-objects are made pairing the key annotated on the fields of both of them.
 *
 * The views and data are handled in three phases:
 *      1) bindView: attach data, listeners to a view from the provided object/configuration and
 *         propagate this down to his sub-views,
 *      2) unbindView: detach data, stops networks calls, stops animations.
 *      3) removeView: called when a reference to the view to unbind is not accessible, release all
 *         resources, stops any work, release references.
 * This class exposes the methods to manage those phases, and there need just to be called in the
 * right situation.
 * The helper objects for realize this are ViewConfigurations and IViewBinders.
 *
 * Any object, can be a valid ViewConfiguration thought annotations and the conversion is fully
 * automatic. For only attach data to custom-views and their basic sub-views (such as RecyclerViews,
 * TextViews, ImageViews, EditTexts and CheckBoxes) annotations are sufficient. You can use in a
 * buildable way the provided base *Element objects as fields in your data objects, to binds their
 * data to yours custom-view's TextViews, ImageViews, etc. You can also realize the same result
 * annotating accordingly your data objects. If you want a single object field be a full
 * ViewConfiguration and not a simple param, you can use the SimpleConfiguration annotation.
 * Configurations are identified by a String id: if a field is annotated as Id it will be the id;
 * the hash of the object will be used otherwise. Holding the id of the object that is passed on the
 * bindView(...) call enables all the functionalities of this class. Methods based on chronological
 * order are also provided if the id cannot be held.
 *
 * Custom behaviour can be implemented through an IViewBinder object. Those binder objects are
 * identified by the ViewConfiguration object id passed to it, state-full, and are responsible to:
 * attach data to the corresponding view at binding time, release resource and references at
 * unbinding time and cancel heavy operations at remove time.
 * A binder can be retrieved at any time by the corresponding id as generic IViewBinder or specific
 * implementation (safe casting) in order to call implementation specific methods.
 * In the binder methods you can manipulate data thought the ViewConfiguration object: passing the
 * object which generated the configuration doesn't provide the flexibility granted by this library.
 * With annotations you can register any binder you want, for a specific view or only if a
 * configuration or view requires it.
 *
 * Views are all tagged with a ViewTag object, which provides the information about the set of click
 * listeners attached and the respective configuration. This class can generate any CompositeView
 * annotated view, and also some defaults such as TextViews, ImageViews and RecyclerViews. View's
 * clicks are handled by a dedicated class in order to enable multiple click listeners. At binding
 * time are added the action provided with the configuration (others listeners can be registered in
 * any moment but after the unbinding of the view). Those actions are all removed at unbinding time,
 * to avoid inconsistencies and calls to duplicate events or old listeners.
 */
@SuppressWarnings("unused")
public abstract class DatabindingContext {

    public final static String DATABINDING_CONTEXT_TAG = "DatabindingContext";

    private int lastTag = 0;

    private Handler mainHandler;

    private final IClickHandler clickHandler;
    private final IImageLoader imageLoader;

    private final IViewConfigurationFactory viewConfigurationFactory;
    private final IViewBinderFactory viewBinderFactory;
    private final IViewCompositeFactory viewCompositeFactory;
    private final IViewFactory viewFactory;

    private final List<String> roots = Collections.synchronizedList(new LinkedList<>());

    private final Map<View, ViewTag> tags = new WeakHashMap<>();
    private final Map<String, DatabindingEntry> entries = new ConcurrentHashMap<>(new LinkedHashMap<>());

    /**
     * Default constructor.
     *
     * @param imageLoader IImageLoader instance for loading images.
     * @param clickHandler IClickHandler instance for handling multiple click on views.
     * @param viewBinderFactory IViewBinderFactory instance providing view binders.
     * @param viewConfigurationFactory IViewConfigurationFactory instance, maps objects into configurations.
     * @param viewCompositeFactory IViewCompositeFactory instance for creating IViewComposites.
     * @param viewFactory IViewFactory instance for generating views.
     */
    public DatabindingContext(IImageLoader imageLoader,
                              IClickHandler clickHandler,
                              IViewBinderFactory viewBinderFactory,
                              IViewConfigurationFactory viewConfigurationFactory,
                              IViewCompositeFactory viewCompositeFactory,
                              IViewFactory viewFactory) {
        this.imageLoader = imageLoader;
        this.clickHandler = clickHandler;
        this.viewBinderFactory = viewBinderFactory;
        this.viewConfigurationFactory = viewConfigurationFactory;
        this.viewCompositeFactory = viewCompositeFactory;
        this.viewFactory = viewFactory;
    }

    /**
     * Binds an Android View to the @ViewConfiguration annotated object and stores the bindings.
     *
     * @param view   Generic Android View to bind.
     * @param object Generic ViewConfiguration annotated object.
     */
    public void bindView(View view, Object object) {
        if (isInvalidViewConfiguration(view, object)) {
            DatabindingLogger.log(DatabindingLogger.Level.ERROR,
                    "Cannot bind null view/object");
            return;
        }

        bindViewToConfiguration(view, buildViewConfigurationForObject(object));
    }

    /**
     * Binds an Android View to the ViewConfiguration annotated object and stores the bindings. The
     * id is provided for later retrieval of bindings and configuration and operations on the view.
     *
     * @param view   Generic Android View to bind.
     * @param object Generic ViewConfiguration annotated object.
     * @param id     String id for the root binding.
     */
    public void bindView(View view, Object object, String id) {
        if (isInvalidViewConfiguration(view, object) || id == null) {
            DatabindingLogger.log(DatabindingLogger.Level.ERROR,
                    "Cannot bind null view/object/id");
            return;
        }

        bindViewToConfiguration(view, buildViewConfigurationForObject(object).cloneWithId(id));
    }

    /**
     * Binds an Android View to the specified IViewConfiguration and stores the latter as the root
     * of the view tree. The id here is provided and overrides the original configuration id.
     *
     * @param view          A generic Android's View to bind.
     * @param configuration A IViewConfiguration representing the data for the view.
     */
    public void bindViewToConfiguration(View view, ViewConfiguration configuration, String id) {
        bindViewToConfiguration(view, configuration.cloneWithId(id));
    }

    /**
     * Binds an Android View to the respective IViewConfiguration, and stores the latter as the
     * root of the view tree.
     *
     * @param view          A generic Android's View to bind.
     * @param configuration A IViewConfiguration representing the data for the view.
     */
    public void bindViewToConfiguration(View view, ViewConfiguration configuration) {
        if (isInvalidViewConfiguration(view, configuration)) {
            DatabindingLogger.log(DatabindingLogger.Level.ERROR,
                    "Cannot bind null view and/or object");
            return;
        }

        buildViewTag(view, configuration);
        buildBindingEntry(configuration);

        bindViewBinder(view, configuration);
        bindViewComposite(view, configuration);
        bindAction(view, configuration.getAction());

        if (!configuration.hasParent()) {
            roots.add(configuration.getId());
        }
    }

    /**
     * Re-binds the stored IViewConfiguration (if any) to the provided view.
     *
     * @param view Android view to bind.
     * @param id   String representing the cached root IViewConfiguration.
     */
    public void restoreView(View view, String id) {
        if (view == null || id == null) {
            DatabindingLogger.log(DatabindingLogger.Level.INFO,
                    "Cannot restore a null view and/or id");
            return;
        }

        bindViewToConfiguration(view, getViewConfigurationById(id));
    }

    /**
     * Removes all binders and the configurations associated with this id (root
     * IViewConfiguration and his children's binders).
     *
     * @param id String representing the starting IViewConfiguration root.
     */
    public void removeView(String id) {
        final ViewConfiguration configuration = getDatabindingEntry(id).configuration;

        if (configuration != null) {
            final Queue<ViewConfiguration> queue = new LinkedList<>();
            queue.add(configuration);

            while (!queue.isEmpty()) {
                final ViewConfiguration current = queue.poll();

                if (current != null) {
                    final IViewBinder binder = getDatabindingEntry(current.getId()).binder;

                    if (binder != null) {
                        binder.removeView(this, current);
                    }

                    entries.remove(current.getId());
                    roots.remove(current.getId());

                    queue.addAll(current.getChildrenConfigurations());
                }
            }
        }
    }

    /**
     * Unbinds all children views starting from the provided view. It will be always tagged with the
     * appropriate ViewTag, if view tag will be lef untouched.
     *
     * @param view Android view which has been binded as root.
     */
    public void unbindView(View view) {
        final ViewTag tag = getViewTag(view);

        if (tag != null) {
            final ViewConfiguration configuration =
                    getDatabindingEntry(tag.configurationId).configuration;

            if (configuration != null) {
                unbindView(view, configuration);
            }
        }
    }

    /**
     * Unbinds the bindings starting from the provided view and going down through his children,
     * without removing anything from the active configurations/binders.
     *
     * @param configuration IViewConfiguration to unbind.
     */
    public void unbindView(View view, ViewConfiguration configuration) {
        unbindView(view, configuration, false);
    }

    /**
     * Unbinds, starting from the specified view and configuration, going down through the children,
     * the associated bindings. Removes them based on the provided remove boolean.
     *
     * @param view Android view to unbind.
     * @param configuration IViewConfiguration to unbind.
     * @param remove boolean to decide if remove the binder or not.
     */
    public void unbindView(View view, ViewConfiguration configuration, boolean remove) {
        if (isInvalidViewConfiguration(view, configuration)) {
            return;
        }

        final Queue<ViewBindingPair> queue = new LinkedList<>();
        queue.add(new ViewBindingPair(view, configuration));

        while (!queue.isEmpty()) {
            final ViewBindingPair binding = queue.poll();

            if (binding == null) {
                continue;
            }

            final ViewTag tag = getViewTag(view);
            final IViewBinder binder = getDatabindingEntry(binding.configuration.getId()).binder;

            if (binder != null) {
                binder.unbindView(this, binding.configuration, binding.view);
            }

            if (tag != null) {
                clickHandler.unsubscribeActions(tag.uuid);
            }

            if (remove) {
                entries.remove(binding.configuration.getId());
                tags.remove(view);
            }

            // Unbinding composite views
            final ViewComposite composite = buildViewComposite(view);

            if (composite != null) {
                for (ViewConfiguration childConfiguration : binding.configuration.getChildrenConfigurations()) {
                    final ViewComposite.ChildView childView = composite.getSubView(childConfiguration.getKey());

                    if (childView != null && childView.view != null) {
                        queue.add(new ViewBindingPair(childView.view, childConfiguration));
                    }
                }
            }

            // Unbinding children that cannot be caught from composite
            if (view instanceof ViewGroup) {
                final ViewGroup viewGroup = (ViewGroup) view;

                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    final View childView = viewGroup.getChildAt(i);
                    final ViewTag childTag = getViewTag(view);

                    final ViewConfiguration childConfiguration =
                            binding.configuration.getChildConfigurationById(childTag.configurationId);

                    if (childView != null && childConfiguration != null) {
                        queue.add(new ViewBindingPair(childView, childConfiguration));
                    }
                }
            }
        }
    }

    /**
     * Class representing a pair consisting of an Android view and his ViewConfiguration data.
     */
    private static final class ViewBindingPair {

        private final View view;
        private final ViewConfiguration configuration;

        private ViewBindingPair(View view, ViewConfiguration configuration) {
            this.view = view;
            this.configuration = configuration;
        }

    }

    /**
     * Returns the ViewTag object with that provided view has been tagged with, null otherwise.
     *
     * @param view Android view.
     * @return ViewTag object if any.
     */
    public ViewTag getViewTag(View view) {
        return tags.get(view);
    }

    /**
     * Finds and returns the IViewBinder implemented by the provided class type associated with the
     * provided id, if any.
     *
     * @param id   String representing the ViewConfiguration relative id
     * @param type Class type of desired return IViewBinder
     * @param <T>  Generic IViewBinder implementation
     * @return T binder
     */
    public <T> T getViewBinderById(String id, Class<? extends T> type) {
        return ObjectUtils.castObject(getDatabindingEntry(id).binder, type);
    }

    /**
     * Finds and returns the IViewBinder associated with the provided id, if any.
     *
     * @param id String id representing the IViewBinder
     * @return the IViewBinder associated with the provided id.
     */
    @SuppressWarnings("unused")
    public IViewBinder getViewBinderById(String id) {
        return getDatabindingEntry(id).binder;
    }

    /**
     * Returns the first T implementation of a parent IViewBinder, starting from the key
     * provided. Returns null if nothing found.
     *
     * @param id   String id for the IViewConfiguration starting point
     * @param type Class of the returned IViewBinder
     * @param <T>  Generic place-holder
     * @return First T IViewBinder implementation
     */
    @SuppressWarnings("unused")
    public <T> T getParentViewBinderByClass(String id, Class<? extends T> type) {
        DatabindingEntry entry = getDatabindingEntry(id);

        if (entry.configuration == null) {
            return null;
        }

        if (!entry.configuration.hasParent()) {
            final IViewBinder databinding = entry.binder;

            if (databinding != null && type.isAssignableFrom(databinding.getClass())) {
                return ObjectUtils.castObject(databinding, type);
            }
        }

        ViewConfiguration parent = entry.configuration.getParentConfiguration();

        while (parent != null) {
            final IViewBinder databinding = getDatabindingEntry(parent.getId()).binder;

            if (databinding != null && type.isAssignableFrom(databinding.getClass())) {
                return ObjectUtils.castObject(databinding, type);
            }

            parent = parent.getParentConfiguration();
        }

        return null;
    }

    /**
     * Returns the root IViewConfiguration identified at bind-time with the provided id
     *
     * @param id String
     * @return root IViewConfiguration stored by the provided id
     */
    public ViewConfiguration getViewConfigurationById(String id) {
        return getDatabindingEntry(id).configuration;
    }

    /**
     * Returns last ViewConfiguration binded object.
     *
     * @return ViewConfiguration object.
     */
    public ViewConfiguration getLastRootViewConfiguration() {
        if (roots.isEmpty()) {
            return null;
        }

        final DatabindingEntry entry = entries.get(roots.get(roots.size() - 1));

        if (entry != null) {
            return entry.configuration;
        }

        return null;
    }

    /**
     * Returns in chronological order the current binded or un-binded but active root
     * ViewConfigurations ids.
     *
     * @return List of String ids.
     */
    public List<String> getRootViewConfigurationsHistory() {
        return roots;
    }

    /**
     * Subscribes a generic listener to the binding defined by the id.
     *
     * @param listener Object
     * @param id String
     */
    @SuppressWarnings({"unchecked", "unused"})
    public synchronized <T> void subscribeListenerToBinding(T listener, String id) {
        ListenerViewBinder<View, T> binder = getViewBinderById(id, ListenerViewBinder.class);

        if (binder != null) {
            binder.subscribeListener(listener);
        }
    }

    /**
     * Removes the listener identified by the provided id.
     *
     * @param id String id of binding.
     */
    @SuppressWarnings({"unchecked", "unused"})
    public synchronized  <T> void unsubscribeListenerFromBinding(String id) {
        ListenerViewBinder<View, T> binder = getViewBinderById(id, ListenerViewBinder.class);

        if (binder != null) {
            binder.unsubscribeListener();
        }
    }

    /**
     * Builds and returns the IViewConfiguration for the provided object, if it can be built.
     *
     * @param object Generic @UI.ViewConfiguration annotated object
     * @return IViewConfiguration for the provided object
     */
    public ViewConfiguration buildViewConfigurationForObject(Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof ViewConfiguration) {
            return (ViewConfiguration) object;
        }

        return viewConfigurationFactory.build(object);
    }

    /**
     * Builds and loads the binding tree for the current configuration, so they will be available in
     * a synchronous way to listeners and operations.
     *
     * @param configuration IViewConfiguration from which starting to build the tree bindings.
     */
    private void buildBindingEntry(ViewConfiguration configuration) {
        if (configuration.hasParent()) {
            return;
        }

        final Queue<ViewConfiguration> configurations = new LinkedList<>();
        configurations.add(configuration);

        while (!configurations.isEmpty()) {
            final ViewConfiguration current = configurations.poll();

            if (current != null && !entries.containsKey(current.getId())) {
                createDatabindingEntry(current);
            }

            if (current != null) {
                configurations.addAll(current.getChildrenConfigurations());
            }
        }

    }

    /**
     * Returns the specified binder if a type is provided.
     * The default binder is returned otherwise (specified by the view type).
     *
     * @param configuration IViewConfiguration.
     * @return String representing the desired binder type.
     */
    private String getBinderType(ViewConfiguration configuration) {
        String type;

        if (configuration.getBinderType() != null) {
            type = configuration.getBinderType();
        } else {
            type = configuration.getViewType();
        }

        return type;
    }

    /**
     * Creates a databinding entry object representing the binding between the configuration and his
     * view in this context.
     *
     * @param configuration IViewConfiguration.
     */
    private void createDatabindingEntry(ViewConfiguration configuration) {
        final IViewBinder binder = buildBinderForConfiguration(configuration);

        entries.put(configuration.getId(), new DatabindingEntry(configuration, binder));
    }

    /**
     * Returns the DatabindingEntry associated with the provided id. An empty entry is returned when
     * no object with the specified id is present in this context.
     *
     * @param id String id.
     * @return DatabindingEntry id associated.
     */
    private DatabindingEntry getDatabindingEntry(String id) {
        final DatabindingEntry entry = entries.get(id);

        if (entry != null) {
            return entry;
        }

        return DatabindingEntry.emptyEntry();
    }

    /**
     * Checks if the provided id has a valid entry in this databinding context.
     *
     * @param id String for the entry.
     * @return boolean.
     */
    private boolean hasDatabindingEntry(String id) {
        return entries.get(id) != null;
    }

    /**
     * Tags a view to identify it thought his lifecycle. Supports null configuration, in case of
     * bindings actions or view specific components before the view is bind with his configuration.
     * Later the configurationId will be valued.
     *
     * @param view Android view to be tagged.
     * @param configuration IViewConfiguration correlate to the view.
     */
    private ViewTag buildViewTag(View view, ViewConfiguration configuration) {
        ViewTag tag = getViewTag(view);

        if (tag == null) {
            if (configuration != null) {
                lastTag = lastTag + 1;

                tag = new ViewTag(lastTag, configuration.getId());
                tags.put(view, tag);
            }

            if (configuration == null) {
                lastTag = lastTag + 1;

                tag = new ViewTag(lastTag);
                tags.put(view, tag);
            }
        } else if (configuration != null) {
            if (tag.configurationId == null) {
                tag.configurationId = configuration.getId();
            }
        }

        return tag;
    }


    /**
     * Checks if either the view or configuration is null.
     *
     * @param view Android view to be bind.
     * @param configuration IViewConfiguration for view.
     * @return boolean which indicate if the binding can proceed.
     */
    private boolean isInvalidViewConfiguration(View view, Object configuration) {
        return view == null || configuration == null;
    }

    /**
     * Executes the provided runnable on the UI main thread.
     *
     * @param runnable Runnable action to execute
     */
    public void runOnUIThread(Runnable runnable) {
        if (this.mainHandler == null) {
            this.mainHandler = new Handler(Looper.getMainLooper());
        }

        this.mainHandler.post(runnable);
    }

    /**
     * Binds a view with his IViewBinder if is present into the loaded binders map.
     *
     * @param view Android view.
     * @param configuration IViewConfiguration configuration.
     */
    private void bindViewBinder(View view, ViewConfiguration configuration) {
        if (!hasDatabindingEntry(configuration.getId())) {
            createDatabindingEntry(configuration);
        }

        IViewBinder binder = getDatabindingEntry(configuration.getId()).binder;

        if (binder != null) {
            binder.bindView(this, configuration, view);
        }
    }

    /**
     * Creates and returns the correct IViewBinder for the provided IViewConfiguration, if any.
     *
     * @param configuration IViewConfiguration from which creates the binder.
     * @return an IViewBinder that can bind the configuration.
     */
    private IViewBinder buildBinderForConfiguration(ViewConfiguration configuration) {
        try {
            return viewBinderFactory.build(getBinderType(configuration));
        } catch (RuntimeException e) {
            DatabindingLogger.log(DatabindingLogger.Level.INFO, e.getMessage());
            return null;
        }
    }

    /**
     * Binds each of the annotated sub-views of the specified view to the correct configuration
     * child. The mapping is made with the annotations key parameters.
     *
     * @param view Android view
     * @param configuration IViewConfiguration to bind to view
     */
    private void bindViewComposite(View view, ViewConfiguration configuration) {
        if (isInvalidViewConfiguration(view, configuration)) {
            return;
        }

        final ViewComposite compositeView = buildViewComposite(view);

        if (compositeView != null) {
            final List<ViewComposite.ChildView> childViews = compositeView.getSubViews();

            for (ViewComposite.ChildView childView1: childViews) {
                final String key = childView1.key;
                final View childView = childView1.view;

                if (childView != null && configuration.getChildConfigurationByKey(key) == null) {
                    childView.setVisibility(VisibilityFallbackFactory
                            .createVisibilityFallback(childView1.fallback));
                    continue;
                }

                bindViewToConfiguration(childView, configuration.getChildConfigurationByKey(key));
            }
        }
    }

    /**
     * Sets to the provided view an action which will be fired when the view.onClick(View v) is
     * called.
     *
     * @param view Android view where set the listener.
     * @param action IViewAction listener.
     */
    public void bindAction(View view, IViewAction action) {
        ViewTag tag = getViewTag(view);

        if (tag == null) {
            tag = buildViewTag(view, null);
        }

        if (action != null) {
            clickHandler.subscribeAction(tag.uuid, action);

            final ViewTag finalTag = tag;
            view.setOnClickListener(v -> clickHandler.executeActions(finalTag.uuid));
        }
    }

    /**
     * Builds the composite view from the @CompositeView annotated android view provided.
     *
     * @param view Android view.
     * @return IViewComposite mapping.
     */
    private ViewComposite buildViewComposite(View view) {
        return viewCompositeFactory.build(view);
    }

    /**
     * Creates a new view based on the provided String classType and Android context.
     *
     * @param context Android Context context.
     * @param classType String which identifies the type of the desired view.
     * @return Android view.
     */
    public View buildView(Context context, String classType) {
        return viewFactory.build(context, classType);
    }

    /**
     * Returns the current IImageLoader instance used to load images from urls, files and resources.
     *
     * @return the current IImageLoader instance.
     */
    public IImageLoader getImageLoader() {
        return imageLoader;
    }

    /**
     * Utility class to create a fallback if a @Configuration for a sub-view is null.
     */
    private final static class VisibilityFallbackFactory {

        public static int createVisibilityFallback(int code) {
            switch (code) {
                case 0:
                    return View.GONE;
                case 1:
                    return View.INVISIBLE;
                case 2:
                default:
                    return View.VISIBLE;
            }
        }

    }

    /**
     * Utility class to cast an object to the specified class type.
     */
    private final static class ObjectUtils {

        public static <T> T castObject(Object object, Class<? extends T> type) {
            if (object == null) {
                return null;
            }

            try {
                return type.cast(object);
            } catch (ClassCastException e) {
                return null;
            }
        }

    }

    /**
     * Class which represents the information about a view. It holds a unique identifier, generated
     * in a progressive way, the configuration id from the ViewConfiguration which binds the view,
     * and an object of data if needed. Use this data inner object to store custom information, do
     * not replace the whole tag or some inconsistencies can occur (the method getViewTag() is
     * made for this purpose).
     */
    public final static class ViewTag {

        public final Integer uuid;
        public String configurationId;

        public Object data;

        public ViewTag(Integer uuid) {
            this.uuid = uuid;
        }

        public ViewTag(Integer uuid, String configurationId) {
            this.uuid = uuid;
            this.configurationId = configurationId;
        }

        public ViewTag(Integer uuid, String configurationId, Object data) {
            this.uuid = uuid;
            this.configurationId = configurationId;
            this.data = data;
        }

    }

    /**
     * Class which represents the binding of a configuration to his view. It contains the binder
     * itself, and the path specific to the parent hierarchy of this binding. It's easy to maintain
     * and improve the algorithms and data structures of the DatabindingContext thought this object,
     * but it's recommended to not hold any reference to the view in this entry, neither in the
     * context.
     */
    private final static class DatabindingEntry {

        final ViewConfiguration configuration;
        final IViewBinder binder;

        /**
         * Default constructor to create an entry in this context.
         *
         * @param binder IViewBinder: is the binding. Can be null if the view is only a container.
         * @param configuration ViewConfiguration associated to this binding.
         */
        DatabindingEntry(ViewConfiguration configuration, IViewBinder binder) {
            this.configuration = configuration;
            this.binder = binder;
        }

        static DatabindingEntry emptyEntry() {
            return new DatabindingEntry(null, null);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof DatabindingEntry) {
                DatabindingEntry entry = (DatabindingEntry) obj;
                return entry.configuration.getId().equals(configuration.getId());
            }
            return super.equals(obj);
        }
    }

}

