/*
 * Copyright (c) 2020. Maverick Labs
 *
 *   This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as,
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.mavericklabs.read.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.mavericklabs.read.model.Level;
import net.mavericklabs.read.util.SharedPreferenceUtil;

import java.util.List;

import static net.mavericklabs.read.util.Constants.en_INLocale;
import static net.mavericklabs.read.util.Constants.mr_INLocale;

public class SpinnerAdapter extends ArrayAdapter<Level> {


    // Your custom values for the spinner (User)
    private List<Level> list;
    private String locale;

    public SpinnerAdapter(Context context, int textViewResourceId,
                          List<Level> list) {
        super(context, textViewResourceId, list);
        this.list = list;
        this.locale = SharedPreferenceUtil.getLocale(context);
    }


    @Override
    public Level getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {

        // TODO Auto-generated method stub
        int count = super.getCount();

        return count > 0 ? count - 1 : count;


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        if (locale.equals(en_INLocale))
            label.setText(list.get(position).getEnglishName());
        else if (locale.equals(mr_INLocale))
            label.setText(list.get(position).getMarathiName());
        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        if (locale.equals(en_INLocale))
            label.setText(list.get(position).getEnglishName());
        else if (locale.equals(mr_INLocale))
            label.setText(list.get(position).getMarathiName());

        return label;
    }


}
