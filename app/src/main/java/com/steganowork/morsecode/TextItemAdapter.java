package com.steganowork.morsecode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TextItemAdapter extends BaseAdapter {
    private Context context;
    private final String[] key;
    private final String[] value;

    public TextItemAdapter(Context context, String[] key, String[] value) {
        this.context = context;
        this.key = key;
        this.value = value;
    }

    @Override
    public int getCount() {
        return key.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView;

        if (convertView == null) {
            gridView = inflater.inflate(R.layout.text_item, null);
            TextView textKeyView = (TextView) gridView.findViewById(R.id.textKeyView);
            TextView textValueView = (TextView) gridView.findViewById(R.id.textValueView);

            textKeyView.setText(key[position]);
            textValueView.setText(value[position]);

            return gridView;
        }

        gridView = (View) convertView;
        return gridView;
    }
}