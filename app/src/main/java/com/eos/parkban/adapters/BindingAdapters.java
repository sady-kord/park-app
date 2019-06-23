package com.eos.parkban.adapters;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class BindingAdapters {

    @BindingAdapter("image_src")
    public static void setImageBitmap(ImageView imageView, Bitmap imageBitmap) {
        imageView.setImageBitmap(imageBitmap);
    }

    @BindingAdapter("data_adapter")
    public static void setRecyclerViewBindingAdapter(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    @BindingAdapter("layout_visibility")
    public static void setVisibility(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }


}
