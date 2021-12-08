package com.uidroid.uidroid.binder;

import android.view.View;
import android.widget.ImageView;

import com.uidroid.uidroid.DatabindingContext;
import com.uidroid.uidroid.loader.ImageRequest;
import com.uidroid.uidroid.model.ViewConfiguration;

import java.util.Objects;

@SuppressWarnings("unused")
public class ImageViewBinder<V extends ImageView> extends ViewBinder<V> {

    public static final String SIZE = "size";
    public static final String COLOR = "color";
    public static final String URL = "url";
    public static final String RESOURCE_ID = "resourceId";
    public static final String PLACE_HOLDER = "placeHolder";
    public static final String FILE = "file";

    @Override
    public void doBind(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        handleColor(view, configuration);
        handleImage(view, configuration, databindingContext);
    }

    private void handleImage(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        final Integer placeHolder = configuration.getIntegerParam(PLACE_HOLDER);
        final Integer size = configuration.getIntegerParam(SIZE);

        String source = null;

        ImageRequest.Builder request = new ImageRequest.Builder();

        if (configuration.getStringParam(URL) != null) {
            source = configuration.getStringParam(URL);
        }
        if (configuration.getIntegerParam(RESOURCE_ID) != null) {
            source = Objects.requireNonNull(configuration.getIntegerParam(RESOURCE_ID)).toString();
        }
        if (configuration.getStringParam(FILE) != null) {
            source = configuration.getStringParam(FILE);
        }

        if (placeHolder != null) {
            request.placeHolder(placeHolder);
        }
        if (size != null) {
            request.requiredSize(size);
        }

        if (source != null) {
            databindingContext.getImageLoader().load(view, request.source(source).build());
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void handleColor(V view, ViewConfiguration configuration) {
        final Integer color = configuration.getIntegerParam(COLOR);

        if (color != null) {
            view.setColorFilter(color);
        }
    }

    @Override
    public void doUnbind(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        databindingContext.getImageLoader().cancel(configuration.getStringParam(URL));
    }

}
