package no.nordicsemi.android.nrftoolbox.WalkthroughMasterActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import no.nordicsemi.android.nrftoolbox.R;

//Adds the Clock and Progress bar to the Walkthrough Activity. The Clock counts down 2 minutes, and
//the progress bar just decreases and supplies additional visual confirmation that something happens
//once the timer ends.
public class TimerFragment extends Fragment {
    private String title;
    private int page;
    private int myInt;
    private int percent;
    private Button timerButton;

    private ProgressBar mProgressBar;
    private TextView clock;
    private TextView prog_percent;
    private TextView stableText;
    CountDownTimer myTimer = new CountDownTimer(10000,1000) {//cDI is 120000

        //Creates the display used by the clock and updates it along with the progress bar every second
        public void onTick(long millisUntilFinished) {
            //  sets the progress bar progress to millisUntilFinished/1000
            myInt = (int) millisUntilFinished/1000;
            Long elapSeconds = 0L;
            Long secondDisp = 0L;
            Long minuteDisp = 0L;
            String Timer = null;
            String sPercent = null;
            elapSeconds = millisUntilFinished/1000;
            secondDisp = elapSeconds % 60;
            minuteDisp = elapSeconds / 60;

            if (minuteDisp < 10L)
                Timer = "0";
            Timer += Long.toString(minuteDisp) + ":";
            if (secondDisp < 10L)
                Timer += "0";
            Timer += Long.toString(secondDisp);
            mProgressBar.setProgress(120 - myInt);
            percent = 100 - Math.round(100*myInt/120);
            sPercent = percent + "%";
            clock.setText(Timer);
            prog_percent.setText(sPercent);
        }
        //Tells the system what to do once the timer is finished
        public void onFinish() {
            timerButton.setVisibility(View.VISIBLE);
            stableText.setText("Device is Now Stable");
        }
    };

    private TimerFragment.OnFragmentInteractionListener mListener;

    public static TimerFragment newInstance(int page, String title) {
        TimerFragment fragmentSecond = new TimerFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentSecond.setArguments(args);
        return fragmentSecond;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.timerProgressBar);
        prog_percent = view.findViewById(R.id.prog_percent);
        clock = view.findViewById(R.id.clock);
        stableText = view.findViewById(R.id.stableText);
        timerButton =  view.findViewById(R.id.timerButton);
        timerButton.setVisibility(View.INVISIBLE);
        timerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //move onto the next fragment(Contact Test) and reset all texts that are not immediately set
                stableText.setText("Device is now Stabilizing...");
                ((WalkthroughMasterActivity) getActivity()).setVpPager(2);
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        myTimer.cancel();
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
