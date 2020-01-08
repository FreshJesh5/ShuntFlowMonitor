package no.nordicsemi.android.nrftoolbox.WalkthroughMasterActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import no.nordicsemi.android.nrftoolbox.R;

import static java.lang.Math.abs;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContactTestFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContactTestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactTestFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //Values to be used for the contact test. The first Red Val and Blue Val are the first values
    //advertised, and the last two are the final two values advertised in the two minute window.
    public int firstYellowVal;
    public int firstGreenVal;
    public int lastYellowVal;
    public int lastGreenVal;
    private View graph_data_button;
    private ProgressBar mProgressBar2;
    private TextView prog_percent2;
    private int percent2;
    private TextView mview;
    private TextView mview2;
    private OnFragmentInteractionListener mListener;
    private Button connectButton;

    public ContactTestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactTestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactTestFragment newInstance(String param1, String param2) {
        ContactTestFragment fragment = new ContactTestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contact_test, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);
        connectButton = view.findViewById(R.id.whywontyouwork);
        ((WalkthroughMasterActivity)getActivity()).findFragmentConnectButton(connectButton);
        graph_data_button = view.findViewById(R.id.move_on_from_contact_button);
        graph_data_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //move onto the next fragment(Graph_Data) and reset all texts that are not immediately set
                ((WalkthroughMasterActivity) getActivity()).setVpPager(3);
                graph_data_button.setVisibility(View.INVISIBLE);
                //((WalkthroughMasterActivity)getActivity()).setGraph_data_flag(true);

            }
        });

        mProgressBar2 = (ProgressBar) view.findViewById(R.id.timerProgressBar2);
        prog_percent2 = view.findViewById(R.id.prog_percent2);
         mview = view.findViewById(R.id.mygreenval);
         mview2 = view.findViewById(R.id.myyellowval);
    }

    public void storeFirstAdValues(int[] x,int[] y, int[] z, int[] a) {
        //assign firstGreenVal and firstYellowVal the first advertised values ***DEPENDS ON UPSTREAM/DOWNSTREAM
        firstGreenVal = x[1];
        firstYellowVal = z[1];
        connectButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                connectButton.setVisibility(View.INVISIBLE);
            }
        },200);        //Hide connect Button once the user connects
        mview.post(new Runnable() {
            @Override
            public void run() {
                mview.setText("ok");
            }
        });
        mview2.post(new Runnable() {
            @Override
            public void run() {
                mview2.setText("ok");
            }
        });//sets the text from waiting to ok so user knows the value is received

    }

    public void doContactTest(Boolean yellow_upstream_flag) {
        String mstring;
        //first test to see if the downstream temperature is higher than the upstream temperature
        if (yellow_upstream_flag) {
            if (lastYellowVal > lastGreenVal || firstYellowVal > firstGreenVal) {
                mstring = "Upstream temperature is higher than Downstream temperature.";
                onTestFailed(mstring);
                return;
            }
        }
        else {
            if (lastYellowVal < lastGreenVal || firstYellowVal < firstGreenVal) {
                mstring = "Upstream temperature is higher than Downstream temperature.";
                onTestFailed(mstring);
                return;
            }
        }
        //second test if delta temperature for both sensors is less than 2 degrees
        if (.0035* abs(lastGreenVal - firstGreenVal) > 2 || .0035* abs(lastYellowVal - firstYellowVal) > 2) {
            mstring = "Rise in temperature is more than 2 degrees.";
            onTestFailed(mstring);
            return;
        }
        else {
            //Now all the tests our complete, and we can move the user onto the measurement process
            onTestSuccess();
        }
    }

    public void onTestSuccess() {
       // TextView mview = getView().findViewById(R.id.no_fail);
       // mview.setText("NO FAIL");
       // Handler handler = new Handler();
       // handler.postDelayed(new Runnable() {
       //     public void run() {
                //wait 2 seconds and go onto the next step
                graph_data_button.setVisibility(View.VISIBLE);
    }

    //Disconnect from the device here
    public void onTestFailed(String mstring) {
        mProgressBar2.setProgress(0);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Contact Test Failed");
        builder.setMessage(mstring + " Taking you back to main menu.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ((WalkthroughMasterActivity)getActivity()).disconnectDevice();
                ((WalkthroughMasterActivity)getActivity()).finish();
            }
        });
        builder.show();

    }

    public CountDownTimer myTimer = new CountDownTimer(10000,1000) {//cDI is 120000

        private TextView tv;
         int[] adv_vals = new int[4];

         public void onStart() {

         }
        //Creates the display used by the clock and updates it along with the progress bar every second
        public void onTick(long millisUntilFinished) {
            //  sets the progress bar progress to millisUntilFinished/1000
                int myInt = (int) millisUntilFinished/1000;
                String sPercent2 = null;
                mProgressBar2.setProgress(120 - myInt);
                percent2 = 100 - Math.round(100*myInt/120);
                sPercent2 = String.format("%d%%", percent2);
                prog_percent2.setText(sPercent2);
        }
        //Tells the system what to do once the timer is finished
        public void onFinish() {
            //public void storeLastAdValues(int[] x,int[] y, int[] z, int[] a) {
                //assign firstGreenVal and firstYellowVal the first advertised values ***DEPENDS ON UPSTREAM/DOWNSTREAM
            WalkthroughMasterActivity myact = ((WalkthroughMasterActivity)getActivity());
                //these values were used when we wanted to have the adc values shown in the contact test table
                //lastGreenVal = myact.currx;
                //lastYellowVal = myact.currz;
                Boolean upflag = myact.getYellow_upstream_flag();
                //final Double greendec = .0035*abs(lastGreenVal-firstGreenVal);
                //final Double yellowdec = .0035*abs(lastYellowVal-firstYellowVal);
                mview = getView().findViewById(R.id.my_sec_greenval);
                mview.post(new Runnable() {
                    @Override
                    public void run() {
                        mview.setText("ok");   //bottom value in table
                    }
                });
                mview2 = getView().findViewById(R.id.my_sec_yellow_val);
                mview2.post(new Runnable() {
                    @Override
                    public void run() {
                        mview2.setText("ok");  //top value in table
                    }
                });
            doContactTest(upflag);
            //}
        }

    };

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
        //Fixes bug where timer still exists after back button is pressed, causing the app to crash
        ((WalkthroughMasterActivity)getActivity()).disconnectDevice();
        //((WalkthroughMasterActivity)getActivity()).onDeviceDisconnected();
        //myTimer.cancel();
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

   