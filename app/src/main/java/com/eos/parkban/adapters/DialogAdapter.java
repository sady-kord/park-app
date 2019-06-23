package com.eos.parkban.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.eos.parkban.R;
import com.eos.parkban.persistence.models.ParkingSpace;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DialogAdapter<T> extends BaseAdapter {

    private Context _context;
    private List<T> list;
    private ArrayList<T> arrayList;
    private String trim;

    public DialogAdapter(List<T> uiEntities, Context context) {
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
            rowView = View.inflate(_context, R.layout.adapter_dialog, null);

            // configure view holder
            viewHolder = new ViewHolder();
            viewHolder.text = rowView.findViewById(R.id.item);

            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        T u = list.get(position);

        if (u instanceof ParkingSpace)
            viewHolder.text.setText(((ParkingSpace) u).getName());
        else if (u instanceof String)
            viewHolder.text.setText(((String) u));

        return rowView;
    }

    public void filter(String charText) {

        charText = charText.toLowerCase(Locale.getDefault());

        list.clear();
        if (charText.length() == 0) {
            list.addAll(arrayList);

        } else {
            for (T postDetail : arrayList) {
                    if (charText.length() != 0 && ((ParkingSpace) postDetail).getName().trim().toLowerCase(Locale.getDefault()).contains(charText)) {
                        list.add(postDetail);
                    } else if (charText.length() != 0 && ((ParkingSpace) postDetail).getName().trim().toLowerCase(Locale.getDefault()).contains(charText)) {
                        list.add(postDetail);
                    }
            }
        }
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView text;
    }
}
