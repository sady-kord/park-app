package com.eos.parkban.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Messenger;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.eos.parkban.R;
import com.eos.parkban.adapters.DialogAdapter;
import com.eos.parkban.adapters.DialogAdapterTimes;
import com.eos.parkban.helper.ShowToast;
import com.eos.parkban.persistence.models.ParkingSpace;
import com.eos.parkban.persistence.models.ParkingSpaceStatus;
import com.eos.parkban.repositories.ParkbanRepository;
import com.eos.parkban.viewmodels.RecordPlateViewModel;

import java.util.ArrayList;
import java.util.List;

public class CustomDialogFragment<T> extends DialogFragment {

    ListView listView;
    SearchView auto_complete_text;
    RelativeLayout searchLayout;
    TextView part1, part2, part3, part4, titleDialog;
    LinearLayout cameraClick, saveClick;

    private ArrayList<T> uiEntities;
    private ArrayList<T> arrayList;
    private int title;
    private DialogAdapter dialogAdapter;
    private List<String> list;
    DialogOnItemSelectedListener dialogOnItemSelectedListener;
    private String p1, p2, p3, p4;
    private T itemSelected;
    private RecordPlateViewModel plateViewModel;
    private Activity context;
    private ParkbanRepository parkbanRepository;

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        context = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment, null);
        builder.setView(view);

        auto_complete_text = view.findViewById(R.id.auto_complete_text);
        listView = view.findViewById(R.id.list_item);
        searchLayout = view.findViewById(R.id.searchLayout);
        part1 = view.findViewById(R.id.part1);
        part1.setText(p1);
        part2 = view.findViewById(R.id.part2);
        part2.setText(p2);
        part3 = view.findViewById(R.id.part3);
        part3.setText(p3);
        part4 = view.findViewById(R.id.part4);
        part4.setText(p4);

        saveClick = view.findViewById(R.id.saveClick);
        saveClick.setOnClickListener(saveClick());

        cameraClick = view.findViewById(R.id.cameraClick);
        cameraClick.setOnClickListener(cameraClick());

        titleDialog = view.findViewById(R.id.dialog_title);
        titleDialog.setText(title);

        dialogAdapter = new DialogAdapter(uiEntities, getActivity());

        auto_complete_text.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

        auto_complete_text.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                dialogAdapter.filter(searchQuery.toString().trim());
                listView.invalidate();
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (dialogOnItemSelectedListener != null)
                    dialogOnItemSelectedListener.OnItemSelected(uiEntities.get(position));
                itemSelected = uiEntities.get(position);


                //dismiss();
            }
        });

        listView.setAdapter(dialogAdapter);

        return builder.create();
    }

    private View.OnClickListener cameraClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plateViewModel.takeNewPhoto(v.getContext());
                dismiss();
            }
        };
    }

    private View.OnClickListener saveClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemSelected != null) {
                    dismiss();

//                    parkbanRepository.getCarByPlaceId(((ParkingSpace) itemSelected).getId(), new ParkbanRepository.DataBaseBooleanCallBack() {
//                        @Override
//                        public void onSuccess(boolean exit) {
//                            if (exit) {
//                                final ConfirmMessageDialog dialog = new ConfirmMessageDialog();
//                                dialog.setCallBack(new ConfirmMessageDialog.DialogCallBack() {
//                                    @Override
//                                    public void onCallBack(boolean confirm) {
//                                        if (confirm) {
//                                            dialog.dismiss();
//                                            dismiss();
//                                        }
//                                    }
//                                });
//                            } else {
//                               dismiss();
//                            }
//
//                        }
//
//                        @Override
//                        public void onFailed() {
//
//                        }
//                    });

                } else {
                    ShowToast.getInstance().showWarning(v.getContext(),R.string.space_not_selected);
                    return;
                }
            }
        };
    }

    public CustomDialogFragment setItems(final ArrayList<T> uiEntities, int title, String part1,
                                         String part2, String part3, String part4, RecordPlateViewModel plateViewModel, ParkbanRepository parkbanRepository, final DialogOnItemSelectedListener dialogOnItemSelectedListener) {
        this.uiEntities = (ArrayList<T>) uiEntities.clone();
        this.title = title;
        this.dialogOnItemSelectedListener = dialogOnItemSelectedListener;
        this.plateViewModel = plateViewModel;
        this.parkbanRepository = parkbanRepository;
        p1 = part1;
        p2 = part2;
        p3 = part3;
        p4 = part4;

        arrayList = new ArrayList<>();
        arrayList.addAll(uiEntities);

        list = new ArrayList<>();
        for (T u : uiEntities) {
            if (u instanceof ParkingSpace) {
                list.add(((ParkingSpace) u).getName());
            }
        }
        return this;
    }

    public interface DialogOnItemSelectedListener {
        void OnItemSelected(Object selectedItem);
    }
}
