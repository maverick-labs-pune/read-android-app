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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import net.mavericklabs.read.R;
import net.mavericklabs.read.model.StudentBook;
import net.mavericklabs.read.model.StudentBookInventory;

import java.util.List;

/**
 * Created by Amey on 1/28/2019.
 */

public class SelectedBooksList extends RecyclerView.Adapter<SelectedBooksList.BooksListViewHolder> {

    private Context context;
    private List<StudentBookInventory> list;
    private TextView textBooks;

    public SelectedBooksList(Context context, List<StudentBookInventory> list, TextView textBooks) {
        this.context = context;
        this.list = list;
        this.textBooks = textBooks;
    }

    @NonNull
    @Override
    public BooksListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BooksListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BooksListViewHolder holder, final int position) {
        final StudentBookInventory inventory = list.get(position);

        StudentBook book = inventory.getBook();
        holder.textName.setText(book.getBookName());
        holder.textSerialNumber.setText(inventory.getSerialNumber());
        holder.imageRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.remove(position);
                textBooks.setText("Books " + list.size());
                notifyDataSetChanged();
            }
        });

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

    class BooksListViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView imageRemove;
        private TextView textName, textSerialNumber;

        BooksListViewHolder(View itemView) {
            super(itemView);
            textSerialNumber = itemView.findViewById(R.id.text_serial_number);
            textName = itemView.findViewById(R.id.text_book_name);
            cardView = itemView.findViewById(R.id.cardView);
            imageRemove = itemView.findViewById(R.id.image_remove);
        }
    }
}
