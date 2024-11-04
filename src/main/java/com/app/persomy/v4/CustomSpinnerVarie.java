package com.app.persomy.v4;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.List;

class CustomSpinnerVarie extends ArrayAdapter<String> {
    private final List<String> objects;
    private final LayoutInflater inflater;

    public CustomSpinnerVarie(Activity context, List<String> objects) {
        super(context, R.layout.spinner_custom_varie, objects);
        this.objects = objects;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = inflater.inflate(R.layout.spinner_custom_varie, parent, false);
        }

        TextView label = row.findViewById(R.id.descrizioneSpesa);
        label.setText(objects.get(position));
        label.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));

        return row;
    }
}