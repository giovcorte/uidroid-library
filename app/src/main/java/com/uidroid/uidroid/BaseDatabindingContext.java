package com.uidroid.uidroid;

import com.uidroid.annotation.UI;
import com.uidroid.uidroid.factory.IViewBinderFactory;
import com.uidroid.uidroid.factory.IViewCompositeFactory;
import com.uidroid.uidroid.factory.IViewConfigurationFactory;
import com.uidroid.uidroid.factory.IViewFactory;
import com.uidroid.uidroid.handler.IClickHandler;
import com.uidroid.uidroid.loader.IImageLoader;

@UI
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
    public BaseDatabindingContext(IImageLoader imageLoader, IClickHandler clickHandler, IViewBinderFactory viewBinderFactory, IViewConfigurationFactory viewConfigurationFactory, IViewCompositeFactory viewCompositeFactory, IViewFactory viewFactory) {
        super(imageLoader, clickHandler, viewBinderFactory, viewConfigurationFactory, viewCompositeFactory, viewFactory);
    }

}
