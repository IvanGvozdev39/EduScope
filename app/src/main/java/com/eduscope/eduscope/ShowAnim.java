package com.eduscope.eduscope;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ShowAnim extends Animation {
    int targetHeight;
    int initialHeight;
    View view;

    public ShowAnim(View view, int initialHeight, int targetHeight) {
        this.view = view;
        this.initialHeight = initialHeight;
        this.targetHeight = targetHeight;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        //view.getLayoutParams().height = (int) (targetHeight * interpolatedTime);
        if (initialHeight < targetHeight)
            view.getLayoutParams().height = initialHeight + (int) (targetHeight * interpolatedTime);
        else
            view.getLayoutParams().height = initialHeight - (int) (targetHeight * interpolatedTime);
        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth,
                           int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
