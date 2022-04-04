# uidroid-library
View binding made generic and customizable for Android applications

To import and use this library in yours projects, add the following statements in your app module gradle:

    implementation 'com.github.giovcorte.uidroid-library:uidroid:1.7'
    annotationProcessor('com.github.giovcorte.uidroid-library:processor:1.7')
    implementation 'com.github.giovcorte.uidroid-library:annotation:1.7'
    implementation 'com.github.giovcorte.uidroid-library:processor:1.7'
    
and this in your project gradle file (under repositories):

    maven { url 'https://jitpack.io' }
    
A more user friendly docu is coming! This library doesn't use any external library, so it's light weight and dependencies free.

Example usages:

Example model classes:

```java
public class User {

    public String name;

    public Photo photo;
    
    public IViewAction action;

}

public class Photo {

    public String url;

}
```

View class: annotate the subviews in order to specify, for each model you want to bind, a path to find the parameter to bind. 

```java
@BindableView
@BindAction({"User.action})
public class UserView extends ConstraintLayout {

    @BindWith(paths = {"User.text:String"})
    public TextView text;
    @BindAction({"User.action})
    @BindWith(paths = {"User.photo.url:String"})
    public ImageView image;

    // Constructors...
    public UserView(@NonNull Context context) {
        ...
    }

}
```

Everywhere you want, define a static method to bind a custom view - data model pair. You can annotate parameters with @Inject in order to provide dependency to your components, it will be injected in the DataBinding constructor.

```java
@BindingMethod
public static void bindUser(@View UserView userView, @Data User user, @Inject SharedPreferences sharedPreferences) {
    // do whatever you want
}

@BindingMethod
public static void bindText(@View TextView view, @Data String text) {
    // do whatever you want, for example view.setText(text);
}
```

To bind all yopu view tree now you only have to get a DataBinding instance and call bind(..) with your parameters, using the overloading. The library also enables to display different objects in RecyclerViews. Annotate your custom views with @BindableView, the objects to display with @BindableObject(CustomView.class), and in both implement respectively IView and IData interfaces, returning the simple class name of each. Then use GenericRecyclerViewAdapter with the AdapterDatabinding and ViewFactory generated classes.


