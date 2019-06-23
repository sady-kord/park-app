package com.eos.parkban.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.eos.parkban.R;
import com.eos.parkban.adapters.DialogAdapterTimes;
import com.eos.parkban.persistence.models.CarPlateHistory;

import java.util.ArrayList;

public class CarTimesDialog<T> extends DialogFragment {

    private ArrayList<T> uiEntities;
    private ArrayList<T> arrayList;

    ListView listView;
    private DialogAdapterTimes dialogAdapter;
    private ArrayList<Object> list;

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_car_items_times, null);
        builder.setView(view);

        listView = view.findViewById(R.id.list_item);
        dialogAdapter = new DialogAdapterTimes(list, getActivity());
        listView.setAdapter(dialogAdapter);

        return builder.create();
    }

    public CarTimesDialog setItem (final ArrayList<T> uiEntities){
        this.uiEntities = (ArrayList<T>) uiEntities.clone();

        arrayList = new ArrayList<>();
        arrayList.addAll(uiEntities);

        list = new ArrayList<>();
        for (T u : uiEntities) {
            if (u instanceof CarPlateHistory){
                list.add(u);
            }
        }
        return this;
    }
}
