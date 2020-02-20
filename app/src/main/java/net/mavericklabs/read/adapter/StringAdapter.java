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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.mavericklabs.read.R;

import java.util.List;

import static net.mavericklabs.read.util.Constants.text_bold;
import static net.mavericklabs.read.util.Constants.text_normal;

public class StringAdapter extends RecyclerView.Adapter<StringAdapter.StringViewHolder> {

    private Context context;
    private List<String> list;
    private String textStyle;

    public StringAdapter(Context context, List<String> list, String textStyle) {
        this.context = context;
        this.list = list;
        this.textStyle = textStyle;
    }

    @NonNull
    @Override
    public StringAdapter.StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        switch (textStyle) {
            case text_normal:
                view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
                break;
            case text_bold:
                view = LayoutInflater.from(context).inflate(R.layout.item_bold_list, parent, false);
                break;
        }

        return new StringViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StringAdapter.StringViewHolder holder, int position) {


        holder.textView.setText(list.get(position));


    }

    @Override
    public long getItemId(int position) {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return list.size();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class StringViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        StringViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
        }
    }
}
