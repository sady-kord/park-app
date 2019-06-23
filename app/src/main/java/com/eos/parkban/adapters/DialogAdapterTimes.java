package com.eos.parkban.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.eos.parkban.R;
import com.eos.parkban.persistence.models.Car;
import com.eos.parkban.persistence.models.CarItems;
import com.eos.parkban.persistence.models.CarPlateHistory;
import com.eos.parkban.persistence.models.ParkingSpace;
import com.eos.parkban.persistence.models.SendStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DialogAdapterTimes<T> extends BaseAdapter {

    private Context _context;
    private List<T> list;
    private ArrayList<T> arrayList;
    private String trim;

    public DialogAdapterTimes(List<T> uiEntities, Context context) {
        this._context = context;
        this.list = uiEntities;
        arrayList = new ArrayList<T>();
        arrayList.addAll(list);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder viewHolder;

        if (rowView == null) {
            rowView = View.inflate(_context, R.layout.adapter_items_time, null);

            // configure view holder
            viewHolder = new ViewHolder();
            viewHolder.text = rowView.findViewById(R.id.item);
            viewHolder.state = rowView.findViewById(R.id.state);

            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        T u = list.get(position);

        if (u instanceof CarPlateHistory) {
            viewHolder.text.setText(((CarPlateHistory) u).getTime());
            if (((CarPlateHistory) u).getStatus() == SendStatus.PENDING.ordinal())
                viewHolder.state.setText("رکورد جدید");
            if (((CarPlateHistory) u).getStatus() == SendStatus.SENT.ordinal()) {
                viewHolder.state.setText("ارسال موفق");
                viewHolder.state.setTextColor(_context.getResources().getColor(R.color.green));
            }
            if (((CarPlateHistory) u).getStatus() == SendStatus.FAILED.ordinal()) {
                viewHolder.state.setText("ارسال ناموفق");
                viewHolder.state.setTextColor(_context.getResources().getColor(R.color.red));
            }
        }else if (u instanceof ParkingSpace)
            viewHolder.text.setText(((ParkingSpace) u).getName());


        return rowView;
    }

    class ViewHolder {
        TextView text, state;
    }
}
