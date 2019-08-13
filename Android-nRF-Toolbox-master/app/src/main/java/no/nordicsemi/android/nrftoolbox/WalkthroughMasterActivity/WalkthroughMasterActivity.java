package no.nordicsemi.android.nrftoolbox.WalkthroughMasterActivity;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.nrftoolbox.R;

public class WalkthroughMasterActivity extends AppCompatActivity
        implements TimerFragment.OnFragmentInteractionListener,
                   InstructionFragment.OnFragmentInteractionListener {

    private MyPagerAdapter pagerAdapter;
    private ViewPager vpPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough_master);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupViewPager();
    }
    public void setupViewPager() {
        vpPager = (ViewPager) findViewById(R.id.vpPager);
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(InstructionFragment.newInstance(0,"page #1"));
        pagerAdapter.addFragment(TimerFragment.newInstance(1,"Page #2"));
        vpPager.setAdapter(pagerAdapter);
    }

    //gets called when the next button is pressed
    public void setVpPager(int i) {
        vpPager.setCurrentItem(i);
    }

    public void startTheTimer() {
        TimerFragment timerFragment = (TimerFragment)pagerAdapter.getItem(1);
        timerFragment.myTimer.start();
    }
    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri){
        //Leave Empty
    }


    public static class MyPagerAdapter extends FragmentStatePagerAdapter {
        private static int NUM_ITEMS = 2;
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


}
