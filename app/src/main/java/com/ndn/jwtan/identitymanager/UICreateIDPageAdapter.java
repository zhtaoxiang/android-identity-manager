package com.ndn.jwtan.identitymanager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class UICreateIDPageAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public UICreateIDPageAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                UICreateOpenmHealthIDInfo tab1 = new UICreateOpenmHealthIDInfo();
                return tab1;
            case 1:
                UICreateOpenmHealthIDInputDialog tab2 = new UICreateOpenmHealthIDInputDialog();
                return tab2;
            case 2:
                UICreateOpenmHealthIDPictureSelect tab3 = new UICreateOpenmHealthIDPictureSelect();
                return tab3;
            case 3:
                UICreateOpenmHealthIDRequestSent tab4 = new UICreateOpenmHealthIDRequestSent();
                return tab4;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
