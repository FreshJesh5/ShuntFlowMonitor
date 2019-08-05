package no.nordicsemi.android.nrftoolbox.WalkthroughMasterActivity;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import no.nordicsemi.android.nrftoolbox.R;

public class WalkthroughMasterActivity extends AppCompatActivity
        implements TimerFragment.OnFragmentInteractionListener,
                   InstructionFragment.OnFragmentInteractionListener {

    FragmentPagerAdapter adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough_master);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

    }

    @Override
    public void onFragmentInteraction(Uri uri){
        //Leave Empty
    }
    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return InstructionFragment.newInstance(0, "Page # 1");
               // case 1: // Fragment # 0 - This will show FirstFragment different title
               //     return InstructionFragment.newInstance(1, "Page # 2");
                case 1: // Fragment # 1 - This will show SecondFragment
                    return TimerFragment.newInstance(1, "Page # 2");
                default:
                    return null;
            }
        }

    }

}
