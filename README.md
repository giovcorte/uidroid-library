# uidroid-library
View binding made stateful and customizable for Android applications

To import and use this library in yours projects, add the following statements in your app module gradle:

    implementation 'com.github.giovcorte.uidroid-library:uidroid:1.7'
    annotationProcessor('com.github.giovcorte.uidroid-library:processor:1.7')
    implementation 'com.github.giovcorte.uidroid-library:annotation:1.7'
    implementation 'com.github.giovcorte.uidroid-library:processor:1.7'
    
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

Model class and corrspective custom view for a text with checkbox:

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
