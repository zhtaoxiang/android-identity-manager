package com.ndn.jwtan.identitymanager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class UICreateNDNIDPageAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public UICreateNDNIDPageAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                UISelectCA tab0 = new UISelectCA();
                return tab0;
            case 1:
                UIProbeInputDialog tab1 = new UIProbeInputDialog();
                return tab1;
            case 2:
                UINamePeriodInputDialog tab2 = new UINamePeriodInputDialog();
                return tab2;
            case 3:
                UIChallengeSelect tab3 = new UIChallengeSelect();
                return tab3;
            case 4:
                UIChallengeInputDialog tab4 = new UIChallengeInputDialog();
                return tab4;
            case 5:
                UICreateOpenmHealthIDPictureSelect tab5 = new UICreateOpenmHealthIDPictureSelect();
                return tab5;
            case 6:
                UICertificateFetched tab6 = new UICertificateFetched();
                return tab6;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
