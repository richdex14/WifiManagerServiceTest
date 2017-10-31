package com.example.dexter.wifimanagerservicetest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by dexter on 10/30/17.
 */

public class PasswordDialogFragment extends DialogFragment
{
    public interface PasswordDialogListener
    {
        void onConnectPressed(DialogFragment dialog);
    }

    public MainActivity mMainActivityRef;
    private EditText mPasswordField;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.password_dialog, null);
        mPasswordField = dialogView.findViewById(R.id.password);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mMainActivityRef.onConnectPressed(PasswordDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PasswordDialogFragment.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    public String getPasswordText()
    {
        return mPasswordField.getText().toString();
    }
}
