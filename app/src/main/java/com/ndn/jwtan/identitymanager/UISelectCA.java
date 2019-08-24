package com.ndn.jwtan.identitymanager;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class UISelectCA extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    AutoCompleteTextView acTextView;
    ArrayList<ClientCaItem> items;
    NDNCertModel ndnCertModel;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UICreateOpenmHealthIDInputDialog.
     */
    // TODO: Rename and change types and number of parameters
    public static UICreateOpenmHealthIDInputDialog newInstance(String param1, String param2) {
        UICreateOpenmHealthIDInputDialog fragment = new UICreateOpenmHealthIDInputDialog();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public UISelectCA() {
        // Required empty public constructor
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
        final View layout = inflater.inflate(R.layout.fragment_uiselect_ca, container, false);

        ndnCertModel = ViewModelProviders.of(getActivity(), new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new NDNCertModel();
            }
        }).get(NDNCertModel.class);

        items = ndnCertModel.caItems;

        ArrayAdapter<ClientCaItem> adapter = new ArrayAdapter<ClientCaItem>(getActivity().getApplication(), android.R.layout.select_dialog_singlechoice, items);
        //Find TextView control
        acTextView = (AutoCompleteTextView) layout.findViewById(R.id.ca_name);
        acTextView.setThreshold(2);
        acTextView.setAdapter(adapter);

        ndnCertModel.selectedCaItem.observe(this, new Observer<ClientCaItem>() {
            @Override
            public void onChanged(@Nullable ClientCaItem clientCaItem) {
                if (clientCaItem != null){
                    ((TextView) layout.findViewById(R.id.info)).setText(clientCaItem.m_caInfo);
                    ((TextView) layout.findViewById(R.id.info)).setVisibility(View.VISIBLE);
                    ((Button) layout.findViewById(R.id.buttonContinue)).setEnabled(true);
                }else {
                    ((TextView) layout.findViewById(R.id.info)).setText("");
                    ((TextView) layout.findViewById(R.id.info)).setVisibility(View.GONE);
                    ((Button) layout.findViewById(R.id.buttonContinue)).setEnabled(false);
                }
            }
        });
        return layout;
    }
}
