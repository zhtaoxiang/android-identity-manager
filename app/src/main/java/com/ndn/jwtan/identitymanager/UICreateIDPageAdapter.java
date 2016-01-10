package com.ndn.jwtan.identitymanager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.MotionEvent;

public class UICreateIDPageAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    String mHintText;
    boolean mDisableExtra;

    public UICreateIDPageAdapter(FragmentManager fm, int NumOfTabs, String hint, boolean disableExtra) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.mHintText = hint;
        this.mDisableExtra = disableExtra;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                UICreateOpenmHealthIDInfo tab0 = new UICreateOpenmHealthIDInfo();
                return tab0;
            case 1:
                UICreateOpenmHealthIDInputDialog tab1 = new UICreateOpenmHealthIDInputDialog();
                return tab1;
            case 2:
                UICreateOpenmHealthIDPictureSelect tab2 = new UICreateOpenmHealthIDPictureSelect();
                return tab2;
            case 3:
                UICreateOpenmHealthIDRequestSent tab3 = UICreateOpenmHealthIDRequestSent.newInstance(mHintText, mDisableExtra);
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
