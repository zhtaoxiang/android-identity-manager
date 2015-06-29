package com.ndn.jwtan.identitymanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class MessageDialogFragment extends DialogFragment {

    public MessageDialogFragment(int stringId) {
        mStringId = stringId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(mStringId)
                .setTitle(R.string.dialog_title)
                .setNeutralButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Confirm!
                        getActivity().finish();
                    }
                });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        return dialog;
    }

    private int mStringId;
}
