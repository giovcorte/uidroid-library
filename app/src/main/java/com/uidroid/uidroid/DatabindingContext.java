package com.uidroid.uidroid;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
import com.uidroid.uidroid.view.IViewComposite;

import java.util.ArrayList;
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
 * The views and data are handled in two phases:
 *      1) configureView: attach data, listeners to a view from the provided object/configuration and
 *         propagate this down to his sub-views.
 *      2) unconfigureView: detach data, stops networks calls, stops animations.
 * This class exposes the methods to manage those phases, and there need just to be called in the
 * right situation.
 * When the binding method ends, the root ViewConfiguration object is returned. It can be stored and
 * used for restore later your views. This library enables two-way databinding between your views
 * and the configuration, so every dynamic change you will apply to your views should be repeated on
 * the relative ViewConfiguration objects, in order to have an up-to-date configuration for later
 * re-binding.
 * The main methods of this class return the ViewConfigurations product of the bindings. You can use
 * this configurations to restore later your view tree. The bindings to this view tree will be held
 * for manipulating views and handling events, until the unbind phase.
 *
 * Any object can be a valid ViewConfiguration thought annotations and the conversion is fully
 * automatic. For only attach data to custom-views and their sub-views (such as RecyclerViews,
 * TextViews, ImageViews, etc.. or others custom-views) annotations are sufficient.If you want an
 * object field be a full ViewConfiguration and not a param, you can use the FieldConfiguration
 * annotation. Configurations are identified by a String id: if a field is annotated as Id it will
 * be used as the id; the hash of the object will be used otherwise. Holding the id of the object
 * that is passed on the configureView(...) call enables all the functionalities of this class.
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

    public final static String COMMON_ID = "commonId";
    public final static String COMMON_KEY = "commonKey";
    public final static String COMMON_BINDER = "commonBinder";
    public final static String COMMON_VIEW = "commonView";

    private int lastTag = 0;

    private Handler mainHandler;

    private final IClickHandler clickHandler;
    private final IImageLoader imageLoader;

    private final IViewConfigurationFactory viewConfigurationFactory;
    private final IViewBinderFactory viewBinderFactory;
    private final IViewCompositeFactory viewCompositeFactory;
    private final IViewFactory viewFactory;

    private final Map<View, ViewTag> tags =
            new WeakHashMap<>();

    private final Map<String, DatabindingEntry> entries =
            new ConcurrentHashMap<>(new LinkedHashMap<>());

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
     * Binds an Android View to the ViewConfiguration annotated object and stores the bindings.
     *
     * @param view Android view.
     * @param object ViewConfiguration annotated java object.
     */
    public void configureView(View view, Object object) {
        if (object == null) {
            DatabindingLogger.log(DatabindingLogger.Level.INFO, "Cannot bind null object");
            return;
        }

        configureView(view, buildViewConfigurationForObject(object));
    }

    /**
     * Binds an Android View to the ViewConfiguration annotated object and stores the bindings. The
     * id is provided for later retrieval of bindings and configuration and operations on the view.
     *
     * @param view   Generic Android View to bind.
     * @param object Generic ViewConfiguration annotated object.
     * @param id     String id for the root binding.
     */
    public void configureView(View view, Object object, String id) {
        if (object == null) {
            DatabindingLogger.log(DatabindingLogger.Level.INFO, "Cannot bind null object");
            return;
        }

        configureView(view, buildViewConfigurationForObject(object).cloneWithId(id));
    }

    /**
     * Binds the provided list to the specified view, first converting and returning the list into a
     * bindable configuration.
     *
     * @param view Android view.
     * @param objects List of any objects annotated with @UI.ViewConfiguration.
     */
    public void configureView(View view, List<Object> objects) {
        if (objects == null) {
            DatabindingLogger.log(DatabindingLogger.Level.INFO, "Cannot bind null objects");
            return;
        }

        configureView(view, buildViewConfigurationForList(view, objects, null, null));
    }

    /**
     * Binds the provided list to the specified view, first converting and returning the list into a
     * bindable configuration.
     *
     * @param view Android view.
     * @param objects List of any objects annotated with @UI.ViewConfiguration.
     * @param binder Class representing the desired binder.
     * @param id String id for this binding.
     */
    public void configureView(View view, List<Object> objects, Class<?> binder, String id) {
        if (objects == null) {
            DatabindingLogger.log(DatabindingLogger.Level.INFO, "Cannot bind null object");
            return;
        }

        final ViewConfiguration configuration = buildViewConfigurationForList(
                view, objects, binder, id);

        configureView(view, buildViewConfigurationForList(view, objects, binder, id));
    }

    /**
     * Binds an Android View to the specified IViewConfiguration and stores the latter as the root
     * of the view tree. The id here is provided and overrides the original configuration id.
     *
     * @param view          A generic Android's View to bind.
     * @param configuration A IViewConfiguration representing the data for the view.
     */
    public void configureView(View view, ViewConfiguration configuration, String id) {
        if (!isValidViewAndConfiguration(view, configuration)) {
            DatabindingLogger.log(DatabindingLogger.Level.INFO, "Cannot bind null view and/or object");
            return;
        }

        configureView(view, configuration.cloneWithId(id));
    }

    /**
     * Binds an Android View to the respective IViewConfiguration, and stores the latter as the
     * root of the view tree.
     *
     * @param view          A generic Android's View to bind.
     * @param configuration A IViewConfiguration representing the data for the view.
     */
    public void configureView(View view, ViewConfiguration configuration) {
        if (!isValidViewAndConfiguration(view, configuration)) {
            DatabindingLogger.log(DatabindingLogger.Level.INFO,
                    "Cannot bind null view and/or object");
            return;
        }

        buildBindingEntry(view, configuration);

        bindView(view, configuration);
        bindViewComposite(view, configuration);
        bindAction(view, configuration.getAction());
    }

    /**
     * Unbinds all children views starting from the provided view. It will be always tagged with the
     * appropriate ViewTag, if view tag will be lef untouched.
     *
     * @param view Android view which has been binded as root.
     */
    public void unconfigureView(View view) {
        final ViewTag tag = getViewTag(view);
        final ViewConfiguration configuration =
                getDatabindingEntry(tag.configurationId).configuration;

        unconfigureView(view, configuration);
    }

    /**
     * Unbinds, starting from the specified view and configuration, going down through the children,
     * the associated bindings. Removes them based on the provided remove boolean.
     *
     * @param view Android view to unbind.
     * @param configuration IViewConfiguration to unbind.
     */
    public void unconfigureView(View view, ViewConfiguration configuration) {
        if (!isValidViewAndConfiguration(view, configuration)) {
            return;
        }

        final Queue<ViewBindingPair> queue = new LinkedList<>();
        queue.add(new ViewBindingPair(view, configuration));

        while (!queue.isEmpty()) {
            final ViewBindingPair binding = queue.poll();
            final ViewTag tag = getViewTag(view);

            if (binding == null) {
                continue;
            }

            getDatabindingEntry(binding.configuration.id()).binder
                    .unbindView(this, binding.configuration, binding.view);


            clickHandler.unsubscribeActions(tag.uuid);
            entries.remove(binding.configuration.id());
            tags.remove(view);

            // Unbinding composite views
            final IViewComposite composite = buildViewComposite(view);

            for (IViewComposite.IViewCompositeChild compositeChild: composite.getChildrenViews()) {
                final ViewConfiguration childConfiguration =
                        binding.configuration.getChildConfigurationByKey(compositeChild.key());

                if (isValidViewAndConfiguration(compositeChild.view(), childConfiguration)) {
                    queue.add(new ViewBindingPair(compositeChild.view(), childConfiguration));
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

                    if (isValidViewAndConfiguration(childView, childConfiguration)) {
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
     * Returns the ViewTag object with that provided view has been tagged with, create a new one
     * otherwise. The non-null safety has been implemented to guarantee an always available view tag
     * which conforms to the library needing.
     *
     * @param view Android view.
     * @return ViewTag object if any.
     */
    @NonNull
    private ViewTag getViewTag(View view) {
        final ViewTag tag = tags.get(view);

        if (tag == null) {
            lastTag = lastTag + 1;

            final ViewTag newTag = new ViewTag(lastTag, COMMON_ID);
            tags.put(view, newTag);

            return newTag;
        } else {
            return tag;
        }
    }

    /**
     * Finds and returns the IViewBinder implemented by the provided class type associated with the
     * provided id, if any.
     *
     * @param view View representing the ViewConfiguration relative id.
     * @param type Class type of desired return IViewBinder.
     * @param <T>  Generic IViewBinder T implementation.
     * @return T binder
     */
    public <T> T getViewBinder(View view, Class<? extends T> type) {
        final ViewTag tag = getViewTag(view);

        return getViewBinder(tag.configurationId, type);
    }

    /**
     * Finds and returns the IViewBinder implemented by the provided class type associated with the
     * provided id, if any.
     *
     * @param id   String representing the ViewConfiguration relative id.
     * @param type Class type of desired return IViewBinder.
     * @param <T>  Generic IViewBinder T implementation.
     * @return T binder.
     */
    public <T> T getViewBinder(String id, Class<? extends T> type) {
        return ObjectUtils.castObject(getDatabindingEntry(id).binder, type);
    }

    /**
     * Finds and returns the IViewBinder associated with the provided id, if any.
     *
     * @param id String id representing the IViewBinder
     * @return the IViewBinder associated with the provided id.
     */
    public IViewBinder getViewBinder(String id) {
        return getDatabindingEntry(id).binder;
    }

    /**
     * Returns the first T implementation of a parent IViewBinder, starting from the key
     * provided. Returns null if nothing found.
     *
     * @param view   ANdroid view for the ViewConfiguration starting point
     * @param type Class of the returned IViewBinder
     * @param <T>  Generic place-holder
     * @return First T IViewBinder implementation
     */
    public <T> T getParentViewBinder(View view, Class<? extends T> type) {
        final ViewTag tag = getViewTag(view);

        return getParentViewBinder(tag.configurationId, type);
    }

    /**
     * Returns the first T implementation of a parent IViewBinder, starting from the key
     * provided. Returns null if nothing found.
     *
     * @param id   String id for the ViewConfiguration starting point
     * @param type Class of the returned IViewBinder
     * @param <T>  Generic place-holder
     * @return First T IViewBinder
     */
    public <T> T getParentViewBinder(String id, Class<? extends T> type) {
        final DatabindingEntry entry = getDatabindingEntry(id);

        if (!isValidConfiguration(entry.configuration)) {
            return null;
        }

        if (!entry.configuration.hasParent()) {
            final IViewBinder binder = entry.binder;

            if (type.isAssignableFrom(binder.getClass())) {
                return ObjectUtils.castObject(binder, type);
            }
        }

        ViewConfiguration parent = entry.configuration.getParentConfiguration();

        while (parent != null) {
            final IViewBinder binder = getDatabindingEntry(parent.id()).binder;

            if (type.isAssignableFrom(binder.getClass())) {
                return ObjectUtils.castObject(binder, type);
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
     * Subscribes a generic listener to the binding defined by the id.
     *
     * @param listener Object
     * @param id String
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> void subscribeListenerToBinding(T listener, String id) {
        final ListenerViewBinder<View, T> binder = getViewBinder(id, ListenerViewBinder.class);

        if (binder != null) {
            binder.subscribeListener(listener);
        }
    }

    /**
     * Removes the listener identified by the provided id.
     *
     * @param id String id of binding.
     */
    @SuppressWarnings("unchecked")
    public synchronized  <T> void unsubscribeListenerFromBinding(String id) {
        final ListenerViewBinder<View, T> binder = getViewBinder(id, ListenerViewBinder.class);

        if (binder != null) {
            binder.unsubscribeListener();
        }
    }

    /**
     * Builds and returns the ViewConfiguration for the provided object, if it can be built.
     *
     * @param object Generic @UI.ViewConfiguration annotated object
     * @return IViewConfiguration for the provided object
     */
    public ViewConfiguration buildViewConfigurationForObject(Object object) {
        if (object == null) {
            return emptyConfiguration;
        }

        if (object instanceof ViewConfiguration) {
            ViewConfiguration configuration = (ViewConfiguration) object;

            if (isValidConfiguration(configuration)) {
                return configuration;
            }

            return emptyConfiguration;
        }

        try {
            return viewConfigurationFactory.build(object);
        } catch (DatabindingException e) {
            DatabindingLogger.log(DatabindingLogger.Level.ERROR, e.getMessage());
            return emptyConfiguration;
        }
    }

    /**
     * Builds and returns the ViewConfiguration for the provided object, if it can be built.
     *
     * @param view Android view.
     * @param objects List of @ViewConfiguration annotated objects to bind to the provided view.
     * @param binder Optional class type for custom binder.
     * @param id Optional String id for identify this binding.
     * @return ViewConfiguration for the provided parameters.
     */
    public ViewConfiguration buildViewConfigurationForList(View view, List<?> objects,
                                                           Class<?> binder,
                                                           String id) {
        final List<ViewConfiguration> configurations = new ArrayList<>();

        for (Object o: objects) {
            configurations.add(viewConfigurationFactory.build(o));
        }

        final ViewConfiguration configuration = new ViewConfiguration(
                id,
                view.getClass().getCanonicalName(),
                binder != null ? binder.getCanonicalName() : COMMON_BINDER);

        configuration.addChildrenConfigurations(COMMON_KEY, configurations);

        return configuration;
    }

    /**
     * Builds and loads the binding tree for the current configuration, so they will be available in
     * a synchronous way to listeners and operations.
     *
     * @param configuration IViewConfiguration from which starting to build the tree bindings.
     */
    private void buildBindingEntry(View view, ViewConfiguration configuration) {
        final ViewTag tag = getViewTag(view);

        if (isValidConfiguration(configuration) &&
                (tag.configurationId == null || tag.configurationId.equals(COMMON_ID))) {
            tag.configurationId = configuration.id();
        }

        final Queue<ViewConfiguration> configurations = new LinkedList<>();
        configurations.add(configuration);

        while (!configurations.isEmpty()) {
            final ViewConfiguration current = configurations.poll();

            if (isValidConfiguration(current) && isMissingDatabindingEntry(current.id())) {
                createDatabindingEntry(current);
            }

            if (isValidConfiguration(current)) {
                configurations.addAll(current.getChildrenConfigurations());
            }
        }

    }

    /**
     * Creates a databinding entry object representing the binding between the configuration and his
     * view in this context.
     *
     * @param configuration IViewConfiguration.
     */
    private void createDatabindingEntry(ViewConfiguration configuration) {
        final IViewBinder binder = buildBinderForConfiguration(configuration);

        entries.put(configuration.id(), new DatabindingEntry(configuration, binder));
    }

    /**
     * Returns the DatabindingEntry associated with the provided id. An empty entry is returned when
     * no object with the specified id is present in this context.
     *
     * @param id String id.
     * @return DatabindingEntry id associated.
     */
    private DatabindingEntry getDatabindingEntry(String id) {
        if (id == null) {
            return DatabindingEntry.emptyEntry;
        }

        final DatabindingEntry entry = entries.get(id);

        if (entry != null) {
            return entry;
        }

        return DatabindingEntry.emptyEntry;
    }

    /**
     * Checks if the provided id has a valid entry in this databinding context.
     *
     * @param id String for the entry.
     * @return boolean.
     */
    private boolean isMissingDatabindingEntry(String id) {
        return entries.get(id) == null;
    }

    /**
     * Checks if the provided view and configuration can be processed in this context.
     *
     * @param view Android view.
     * @param configuration ViewConfiguration object.
     * @return boolean true if view and configuration are considered valid, false otherwise.
     */
    private boolean isValidViewAndConfiguration(View view, ViewConfiguration configuration) {
        return isValidView(view) && isValidConfiguration(configuration);
    }

    /**
     * Checks of the provided view is null or not.
     *
     * @param view Android view.
     * @return boolean true if the view is non null.
     */
    private boolean isValidView(View view) {
        return view != null;
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
    private void bindView(View view, ViewConfiguration configuration) {
        if (!isValidConfiguration(configuration)) {
            return;
        }

        if (isMissingDatabindingEntry(configuration.id())) {
            createDatabindingEntry(configuration);
        }

        getDatabindingEntry(configuration.id()).binder
                .bindView(this, configuration, view);
    }

    /**
     * Creates and returns the correct IViewBinder for the provided IViewConfiguration, if any.
     *
     * @param configuration IViewConfiguration from which creates the binder.
     * @return an IViewBinder that can bind the configuration.
     */
    private IViewBinder buildBinderForConfiguration(ViewConfiguration configuration) {
        String type;

        if (configuration.binder() != null) {
            type = configuration.binder();
        } else {
            type = configuration.view();
        }

        try {
            return viewBinderFactory.build(type);
        } catch (DatabindingException e) {
            DatabindingLogger.log(DatabindingLogger.Level.INFO, e.getMessage());
            return emptyBinder;
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
        if (!isValidViewAndConfiguration(view, configuration)) {
            return;
        }

        final List<IViewComposite.IViewCompositeChild> compositeChildren =
                buildViewComposite(view).getChildrenViews();

        for (IViewComposite.IViewCompositeChild compositeChild : compositeChildren) {
            final View childView = compositeChild.view();
            final ViewConfiguration childConfiguration =
                    configuration.getChildConfigurationByKey(compositeChild.key());

            if (isValidView(childView) && !isValidConfiguration(childConfiguration)) {
                childView.setVisibility(VisibilityFallbackFactory
                        .createVisibilityFallback(compositeChild.fallback()));
                continue;
            }

            configureView(childView, childConfiguration);
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
        final ViewTag tag = getViewTag(view);

        if (action != null) {
            clickHandler.subscribeAction(tag.uuid, action);

            view.setOnClickListener(v -> clickHandler.executeActions(tag.uuid));
        }
    }

    /**
     * Checks if the provided configuration can be handled by this context. Empty and null
     * configurations are discarded.
     *
     * @param configuration ViewConfiguration object to validate.
     * @return boolean true if configuration is valid, false otherwise.
     */
    private boolean isValidConfiguration(ViewConfiguration configuration) {
        return configuration != null && !configuration.id().equals(COMMON_ID);
    }

    /**
     * Builds the composite view from the @CompositeView annotated android view provided.
     *
     * @param view Android view.
     * @return IViewComposite mapping.
     */
    private IViewComposite buildViewComposite(View view) {
        try {
            return viewCompositeFactory.build(view);
        } catch (DatabindingException e) {
            DatabindingLogger.log(DatabindingLogger.Level.INFO, e.getMessage());
            return emptyComposite;
        }
    }

    /**
     * Creates a new view based on the provided String classType and Android context.
     *
     * @param context Android Context context.
     * @param classType String which identifies the type of the desired view.
     * @return Android view.
     */
    public View buildView(Context context, String classType) {
        try {
            return viewFactory.build(context, classType);
        } catch (DatabindingException e) {
            DatabindingLogger.log(DatabindingLogger.Level.INFO, e.getMessage());
            return null;
        }
    }

    /**
     * Returns the current IImageLoader instance used to load images from urls, files and resources.
     *
     * @return IImageLoader object.
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

        private final Integer uuid;
        private String configurationId;

        private ViewTag(Integer uuid, String configurationId) {
            this.uuid = uuid;
            this.configurationId = configurationId;
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

        private final ViewConfiguration configuration;
        private final IViewBinder binder;

        private final static DatabindingEntry emptyEntry =
                new DatabindingEntry(emptyConfiguration, emptyBinder);

        /**
         * Default constructor to create an entry in this context.
         *
         * @param binder IViewBinder: is the binding. Can be null if the view is only a container.
         * @param configuration ViewConfiguration associated to this binding.
         */
        private DatabindingEntry(ViewConfiguration configuration, IViewBinder binder) {
            this.configuration = configuration;
            this.binder = binder;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof DatabindingEntry) {
                DatabindingEntry entry = (DatabindingEntry) obj;
                return entry.configuration.id().equals(configuration.id());
            }
            return super.equals(obj);
        }
    }

    /**
     * IViewBinder empty implementations to avoid null values when generating binders from views or
     * retrieving them from the entries.
     */
    public static final IViewBinder emptyBinder = new IViewBinder() {

        @Override
        public void bindView(DatabindingContext databindingContext, ViewConfiguration configuration, View view) {

        }

        @Override
        public void unbindView(DatabindingContext databindingContext, ViewConfiguration configuration, View view) {

        }

    };

    /**
     * IViewComposite empty implementations to avoid null values when generating composites from
     * views.
     */
    public static final IViewComposite emptyComposite = new IViewComposite() {

        @Override
        public void putChildView(String key, View view, int fallback) {

        }

        @Override
        public List<IViewCompositeChild> getChildrenViews() {
            return Collections.emptyList();
        }

        @Override
        public IViewComposite.IViewCompositeChild getChildView(String key) {
            return null;
        }

    };

    /**
     * Implementation of an empty configuration to avoid null values.
     */
    public static final ViewConfiguration emptyConfiguration =
            new ViewConfiguration(COMMON_ID, COMMON_VIEW, COMMON_BINDER);

}
