package com.uidroid.uidroid.loader;

import android.widget.ImageView;

import com.uidroid.uidroid.R;

public class ImageRequest {

    private int viewCode;

    private final String source;

    private final ImageView.ScaleType scaleType;
    private final int placeHolder;
    private final int requiredSize;

    public ImageRequest(String source, ImageView.ScaleType scaleType, int placeHolder, int requiredSize) {
        this.source = source;
        this.scaleType = scaleType;
        this.placeHolder = placeHolder;
        this.requiredSize = requiredSize;
    }

    public ImageView.ScaleType getScaleType() {
        return scaleType;
    }

    public String getSource() {
        return source;
    }

    public int getPlaceHolder() {
        return placeHolder;
    }

    public int getRequiredSize() {
        return requiredSize;
    }

    public int getViewCode() {
        return viewCode;
    }

    public void setViewCode(int viewCode) {
        this.viewCode = viewCode;
    }

    public static class Builder {

        private String source;

        public ImageView.ScaleType scaleType = ImageView.ScaleType.CENTER_CROP;
        public int placeHolder = R.drawable.stub;
        public int requiredSize = 300;

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder scaleType(ImageView.ScaleType scaleType) {
            this.scaleType = scaleType;
            return this;
        }

        public Builder placeHolder(int placeHolder) {
            this.placeHolder = placeHolder;
            return this;
        }

        public Builder requiredSize(int requiredSize) {
            this.requiredSize = requiredSize;
            return this;
        }

        public ImageRequest build() {
            return new ImageRequest(source, scaleType, placeHolder, requiredSize);
        }
    }


}
