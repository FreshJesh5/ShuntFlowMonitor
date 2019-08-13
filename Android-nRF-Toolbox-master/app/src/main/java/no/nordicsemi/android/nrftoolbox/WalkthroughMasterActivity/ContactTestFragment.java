package no.nordicsemi.android.nrftoolbox.WalkthroughMasterActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import no.nordicsemi.android.nrftoolbox.R;

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
    public Boolean isRedUpstream=false;
    public int firstRedVal;
    public int firstBlueVal;
    public int lastRedVal;
    public int lastBlueVal;

    private OnFragmentInteractionListener mListener;

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

    public void doContactTest(String isredupstream) {
        //mParam1 should be isredupstream hopefully, or maybe not
        //first test if delta temperature for both sensors is less than 2 degrees
        if (.0035*Math.abs(lastBlueVal-firstBlueVal) > 2 || .0035*Math.abs(lastRedVal-firstRedVal) > 2) {
            onTestFailed();
        }
        //second test to see if the downstream temperature is higher than the upstream temperature
        if (isRedUpstream) {
            if (lastRedVal > lastBlueVal || firstRedVal > firstBlueVal) {
                onTestFailed();
            }
        }
        else {
            if (lastRedVal < lastBlueVal || firstRedVal < firstBlueVal) {
                onTestFailed();
            }
        }
        //Now all the tests our complete, and we can move the user onto the measurement process
    }
    public void onTestFailed() {

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
