package no.nordicsemi.android.nrftoolbox.PracticeBleServiceStuff;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import no.nordicsemi.android.nrftoolbox.R;

public class WonderActivity extends AppCompatActivity implements ConnectFragment.OnFragmentInteractionListener,
        JoshBleFragment.OnFragmentInteractionListener {

    FragmentPagerAdapter myViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wonder);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ViewPager mpPager = (ViewPager) findViewById(R.id.wonderVwPager);
        myViewPager = new WonderActivity.MyPagerAdapter(getSupportFragmentManager());
        mpPager.setAdapter(myViewPager);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //Handles Communication Between Fragments, Leave Empty for now
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
                    return ConnectFragment.newInstance("whatever", "Page # 1");
                case 1: // Fragment # 1 - This will show SecondFragment
                    return JoshBleFragment.newInstance("whatever", "Page # 2");
                default:
                    return null;
            }
        }

    }

}
