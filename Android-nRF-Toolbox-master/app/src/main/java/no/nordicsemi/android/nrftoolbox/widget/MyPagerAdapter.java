package no.nordicsemi.android.nrftoolbox.widget;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MyPagerAdapter extends FragmentStatePagerAdapter {
    private static int NUM_ITEMS = 3;
    private final List<Fragment> mFragmentList = new ArrayList<>();

    public MyPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }
    public void addFragment(Fragment fragment) {
        mFragmentList.add(fragment);
    }
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

}
