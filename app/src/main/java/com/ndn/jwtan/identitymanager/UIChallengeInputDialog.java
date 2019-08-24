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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class UIChallengeInputDialog extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    NDNCertModel ndnCertModel;
    TextView[] textViews;
    EditText[] editTexts;
    int n_Parameters;

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

    public UIChallengeInputDialog() {
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
        final View layout = inflater.inflate(R.layout.fragment_ui_challenge_input_dialog, container, false);

        ndnCertModel = ViewModelProviders.of(getActivity(), new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new NDNCertModel();
            }
        }).get(NDNCertModel.class);

        ndnCertModel.requirement.observe(this, new Observer<JSONObject>() {
            @Override
            public void onChanged(@Nullable final JSONObject requirement) {
                n_Parameters = requirement.length();
                textViews = new TextView[n_Parameters];
                editTexts = new EditText[n_Parameters];
                LinearLayout parent = layout.findViewById(R.id.challenge_view);
                if (parent.getChildCount() > 0)
                    parent.removeAllViews();

                int i = 0;
                for (Iterator<String> it = requirement.keys(); it.hasNext(); i++) {
                    String probe = it.next();

                    final TextView rowTextView = new TextView(getActivity().getApplication());
                    final EditText rowEditView = new EditText(getActivity().getApplication());

                    rowTextView.setText(probe);

                    parent.addView(rowTextView, 0);
                    parent.addView(rowEditView, 1);

                    textViews[i] = rowTextView;
                    editTexts[i] = rowEditView;
                }
            }
        });

        layout.findViewById(R.id.buttonContinue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject js = new JSONObject();
                JSONObject requirements = ndnCertModel.requirement.getValue();
                int i = 0;
                if (requirements != null)
                    for (Iterator<String> it = requirements.keys(); it.hasNext(); i++) {
                        if (editTexts[i].getText().toString().equals("")) {
                            Toast.makeText(getActivity().getApplication(), "Please fill the entries", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String inputField = it.next();
                        try {
                            js.put(inputField, editTexts[i].getText().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity().getApplication(), "Error occured while parsing fields", Toast.LENGTH_SHORT).show();
                        }
                    }
                ndnCertModel.inputRequirement = js;
                ((GenerateNDNToken) getActivity()).submitChallenge();
            }
        });
        return layout;
    }
}