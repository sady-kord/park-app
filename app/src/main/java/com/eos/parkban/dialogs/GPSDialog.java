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

public class GPSDialog extends DialogFragment {

    private Activity context;

    LinearLayout confirmLayout, cancelLayout;
    private AlertDialog alertDialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        context = getActivity();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_gps, null);
        builder.setView(view);
        alertDialog = builder.create();

        confirmLayout = view.findViewById(R.id.confirm);
        cancelLayout = view.findViewById(R.id.cancel);

        confirmLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
                alertDialog.dismiss();

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

}
