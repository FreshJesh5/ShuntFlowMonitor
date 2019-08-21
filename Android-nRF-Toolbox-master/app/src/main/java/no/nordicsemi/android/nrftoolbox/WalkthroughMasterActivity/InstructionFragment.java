package no.nordicsemi.android.nrftoolbox.WalkthroughMasterActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import no.nordicsemi.android.nrftoolbox.MainActivity;
import no.nordicsemi.android.nrftoolbox.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InstructionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InstructionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class InstructionFragment extends Fragment {
    private String title;
    private int page;
    TextView yellow_orientation;

    private InstructionFragment.OnFragmentInteractionListener mListener;
    public InstructionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param page Parameter 1.
     * @param title Parameter 2.
     * @return A new instance of fragment InstructionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InstructionFragment newInstance(int page, String title) {
        InstructionFragment fragmentFirst = new InstructionFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
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
        View view = inflater.inflate(R.layout.fragment_instruction, container, false);
        view.findViewById(R.id.move_on_from_instruction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //when the instruction button is pressed, it should move onto the next fragment(timer)
                //and ALSO start the timer
                //model.doAction();
                ((WalkthroughMasterActivity)getActivity()).setVpPager(1);
                ((WalkthroughMasterActivity)getActivity()).startTheTimerOne(); //the 1 means start the 1st timer(or the one in timerFragment)
            }
        });
        final Spinner myspin_one = view.findViewById(R.id.instruction_spinner_one);
        ArrayAdapter<CharSequence> adapter_one = ArrayAdapter.createFromResource(getActivity(), R.array.instruction_upstream_downstream, android.R.layout.simple_spinner_item);
        adapter_one.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        myspin_one.setAdapter(adapter_one);


        final Spinner myspin_two = view.findViewById(R.id.instruction_spinner_two);
        ArrayAdapter<CharSequence> adapter_two = ArrayAdapter.createFromResource(getActivity(), R.array.instruction_upstream_downstream, android.R.layout.simple_spinner_item);
        adapter_two.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        myspin_two.setAdapter(adapter_two);

        myspin_one.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    ((WalkthroughMasterActivity) getActivity()).setYellow_upstream_flag(false);
                    myspin_two.setSelection(1);
                }
                else if (position == 1) {
                    ((WalkthroughMasterActivity) getActivity()).setYellow_upstream_flag(true);
                    myspin_two.setSelection(0);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        myspin_two.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    ((WalkthroughMasterActivity) getActivity()).setYellow_upstream_flag(false);
                    myspin_one.setSelection(1);
                }
                else if (position == 1) {
                    ((WalkthroughMasterActivity) getActivity()).setYellow_upstream_flag(true);
                    myspin_one.setSelection(0);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void ononClickWalkthroughButton(Uri uri) {
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
