package com.eos.parkban;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.eos.parkban.persistence.models.ParkingSpace;

import java.util.List;

public class BaseActivity extends AppCompatActivity {

    public static long CurrentUserId;
    public static List<ParkingSpace> parkingSpaceList;

    private Dialog progressDialog;

    public void showProgress(final boolean show) {
        try {
            if (progressDialog == null) {
                progressDialog = new Dialog(this, android.R.style.Theme_Black);
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.control_progress_dialog, null);

                progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                progressDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
                progressDialog.setContentView(view);
            }
            if (show)
                progressDialog.show();
            else

                progressDialog.dismiss();

        }catch (Exception e){
            Log.i("progress ex" , e.getMessage());
        }
    }
}
