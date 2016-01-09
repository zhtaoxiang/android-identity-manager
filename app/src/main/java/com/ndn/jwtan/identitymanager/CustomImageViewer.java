package com.ndn.jwtan.identitymanager;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by zhehaowang on 1/8/16.
 */
public class CustomImageViewer extends ImageView {
    public boolean selected;
    public CustomImageViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        selected = false;
    }
}
