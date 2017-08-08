package com.ndn.jwtan.identitymanager;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by zhehaowang on 1/8/16.
 */
public class CustomImageViewer extends AppCompatImageView {
    public boolean selected;
    public CustomImageViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        selected = false;
    }
}
