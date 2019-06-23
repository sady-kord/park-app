package com.eos.parkban.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;

import com.eos.parkban.R;

public class ConfirmMessageDialog extends DialogFragment {

    LinearLayout confirmLayout, cancelLayout;
    private Activity context;
    private Dialog alertDialog;
    private DialogCallBack callBack;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        context = getActivity();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_confirm_message, null);
        builder.setView(view);
        alertDialog = builder.create();

        confirmLayout = view.findViewById(R.id.confirm);
        cancelLayout = view.findViewById(R.id.cancel);

        confirmLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onCallBack(true);
            }
        });

        cancelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
            }
        });

        return alertDialog;
    }

    public void setCallBack(DialogCallBack callBack) {
        this.callBack = callBack;
    }

    public interface DialogCallBack {
        void onCallBack(boolean confirm);
    }
}
