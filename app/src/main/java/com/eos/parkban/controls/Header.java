package com.eos.parkban.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.eos.parkban.R;

public class Header extends RelativeLayout {

    PersianTextView title;

    public Header(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.control_header , this, true);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Header, 0, 0);

        title = v.findViewById(R.id.header_title);
        title.setText(typedArray.getString(R.styleable.Header_titleHeader));
    }

    public void setTitle(String text){
        title.setText(text);
    }

    public void setTitle(int text){
        title.setText(text);
    }
}
