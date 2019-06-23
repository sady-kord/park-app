package com.eos.parkban.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import com.eos.parkban.R;
import com.eos.parkban.helper.FontHelper;

@SuppressLint("AppCompatCustomView")
public class PersianEditText extends EditText {
    Context context;

    public PersianEditText(Context context) {
        super(context);
        setInitialize(context);
    }

    public PersianEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setInitialize(context);
    }

    public PersianEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setInitialize(context);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text != null) {
            text = FontHelper.toPersianNumber(text.toString());
        }
        super.setText(text, type);
    }

    private void setInitialize(Context context) {
        if (!isInEditMode())
            setTypeface(FontHelper.getInstance(context).getPersianTextTypeface());
        setHintTextColor(getResources().getColor(R.color.colorPrimary));
        setCompoundDrawablePadding(10);
    }

    public void IsOnlyEnglishDigit(Boolean isEnglishDigit) {
        if (!isEnglishDigit && !isInEditMode())
            setTypeface(FontHelper.getInstance(context).getPersianTextTypeface());
    }

}

