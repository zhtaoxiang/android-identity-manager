package com.ndn.jwtan.identitymanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class UICreateOpenmHealthIDRequestSent extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mHintText;
    private boolean mDisableExtra;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UICreateOpenmHealthIDRequestSent.
     */
    // TODO: Rename and change types and number of parameters
    public static UICreateOpenmHealthIDRequestSent newInstance(String param1, boolean param2) {
        UICreateOpenmHealthIDRequestSent fragment = new UICreateOpenmHealthIDRequestSent();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putBoolean(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public UICreateOpenmHealthIDRequestSent() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mHintText = getArguments().getString(ARG_PARAM1);
            mDisableExtra = getArguments().getBoolean(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_uicreate_openm_health_idrequest_sent, container, false);
        TextView tv = (TextView) view.findViewById(R.id.step4Hint);
        tv.setText(mHintText);
        if (mDisableExtra) {
            TextView emailTv = (TextView) view.findViewById(R.id.step4Email);
            TextView idNameTv = (TextView) view.findViewById(R.id.step4IdName);
            ImageView imageView = (ImageView) view.findViewById(R.id.step4ImageView);
            Button returnBtn = (Button) view.findViewById(R.id.returnBtn);
            emailTv.setVisibility(View.GONE);
            idNameTv.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            returnBtn.setEnabled(false);
        }
        return view;
    }
}
