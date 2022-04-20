# uidroid-library
View binding made stateful and customizable for Android applications

To import and use this library in yours projects, add the following statements in your app module gradle:

    implementation 'com.github.giovcorte.uidroid-library:uidroid:3.0.0'
    annotationProcessor('com.github.giovcorte.uidroid-library:processor:3.0.0')
    implementation 'com.github.giovcorte.uidroid-library:annotation:3.0.0'
    implementation 'com.github.giovcorte.uidroid-library:processor:3.0.0'
    
and this in your project gradle file (under repositories):

    maven { url 'https://jitpack.io' }
    
For now the documentation is provided as detailed comments in the source code, in the DatabindingContext and ViewConfiguration classes. A more user friendly docu is coming! This library doesn't use any external library, so it's light weight and dependencies free.

Ecxample usages:

Model class for a TextView:

```java
@UI.ViewConfiguration(view = TextView.class)
public class TextViewElement {

    @UI.Param(key = TEXT)
    public String text;

    @UI.Param(key = FACE)
    public Integer face;

    @UI.Param(key = GRAVITY)
    public Integer gravity;

    @UI.Param(key = COLOR)
    public Integer color;

}
```

Model class with corrspective custom view and binder for a text with checkbox:

```java
@UI.ViewConfiguration(view = ItemTextCheckbox.class)
public class ItemTextCheckboxElement {

    @UI.Configuration(key = "text")
    public TextViewElement text;

    @UI.Configuration(key = "checkbox")
    public CheckBoxElement checkBox;

    @UI.Param(key = SELECTED)
    public Boolean selected;
    
}
```

```java
@UI.CustomView
public class ItemTextCheckbox extends ConstraintLayout {

    @UI.View(key = "text")
    public TextView text;
    @UI.View(key = "checkbox")
    public CheckBox checkBox;

    // Constructors...
    public ItemTextCheckbox(@NonNull Context context) {
        ...
    }

}
```

```java
@UI.BinderFor(view = ItemTextCheckbox.class)
public class ItemCheckBoxTextBinder extends SingleSelectableViewBinder<ItemTextCheckbox> {

    private boolean selected;

    public ItemCheckBoxTextBinder() {
        super();
    }

    @Override
    public void doBind(ItemTextCheckbox view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        selected = configuration.getChildConfigurationByKey("checkbox").getBooleanParam(CHECKBOX_SELECTED, false);
        databindingContext.bindAction(view.getCheckBox(), () -> {
            if (!selected) {
                selected = true;

                final ViewConfiguration parent = configuration.getParentConfiguration();
                List<ViewConfiguration> children =
                        parent.getChildrenConfigurations((key, configuration1) ->
                                configuration1.hasChild("checkbox"));

                for (ViewConfiguration child: children) {
                    child.getChildConfigurationByKey("checkbox").putParam(CHECKBOX_SELECTED, false);
                }

                notifyItemSelected(configuration, databindingContext);
            } else {
                view.getCheckBox().setChecked(true);
            }
        });
    }

    @Override
    public void doUnbind(ItemTextCheckbox view, ViewConfiguration configuration, DatabindingContext databindingContext) {

    }

    @Override
    public void doRemove(DatabindingContext databindingContext, ViewConfiguration configuration) {
        selected = false;
    }
}
```

Model class for displaying annotated objects (also different) in a list:

```java
@UI.ViewConfiguration(view = RecyclerView.class)
public class RecyclerViewElement {

    @UI.ConfigurationsList(key = RECYCLER_VIEW_LIST)
    public List<Object> recyclerViewList;

    @UI.Param(key = RECYCLER_VIEW_COLUMNS)
    public Integer columns;

    @UI.Param(key = RECYCLER_VIEW_ITEM_SPAN_SIZE)
    public Integer column;

    @UI.Param(key = RECYCLER_VIEW_TYPE)
    public String type;

}
```

As seen, basic android views such as ImageViews, TextViews, CheckBoxes, RecyclerViews are supported by defaults. For others base or custom views you have to build your own classes.
Any object can be annotated to be bindable to your custom views or a base android view. For those who wants to create a view-model layer the library generates the builders for all your models, in order to make composition of view easy.

After creating you views, models, and binder classes, in order to bind a object to a view you have only to call bindView() from your DatabindingContext instance.
This instance enables also restoring views, getting a references to binders, set custom listeners to binders.

In order to use DatabindingContext, you have to extend it and @UI annotate:

```java
@UI()
public class BaseDatabindingContext extends DatabindingContext {

    /**
     * Default constructor.
     *
     * @param imageLoader              IImageLoader instance for loading images.
     * @param clickHandler             IClickHandler instance for handling multiple click on views.
     * @param viewBinderFactory        IViewBinderFactory instance providing view binders.
     * @param viewConfigurationFactory IViewConfigurationFactory instance, maps objects into configurations.
     * @param viewCompositeFactory     IViewCompositeFactory instance for creating IViewComposites.
     * @param viewFactory              IViewFactory instance for generating views.
     */
    public BaseDatabindingContext(IImageLoader imageLoader, IClickHandler clickHandler, IViewBinderFactory viewBinderFactory, IViewConfigurationFactory         viewConfigurationFactory, IViewCompositeFactory viewCompositeFactory, IViewFactory viewFactory) {
        super(imageLoader, clickHandler, viewBinderFactory, viewConfigurationFactory, viewCompositeFactory, viewFactory);
    }
    
    // customize...

}
```

And to obtain an instance, if you don't want to manually create/instantiate your factories:
```java
DatabindingContext databindingContext = new BaseDatabindingContextProvider(context)
                .repository(repository)                              // dependencies required by binders (examples)
                .sharedPreferencesManager(sharedPreferencesManager)  // dependencies required by binders (examples)
                .build();
```
