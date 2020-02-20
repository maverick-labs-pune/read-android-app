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
import androidx.recyclerview.widget.RecyclerView;

import net.mavericklabs.read.R;
import net.mavericklabs.read.model.StudentBook;
import net.mavericklabs.read.model.StudentBookInventory;

import java.util.List;

import static net.mavericklabs.read.realm.RealmHandler.getTranslation;

public class BookLendingAdapter extends RecyclerView.Adapter<BookLendingAdapter.BookLendingViewHolder> {

    private Context context;
    private List<StudentBookInventory> list;
    private String locale;

    public BookLendingAdapter(Context context, List<StudentBookInventory> list,String locale) {
        this.context = context;
        this.list = list;
        this.locale = locale;
    }

    @NonNull
    @Override
    public BookLendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book_lending, parent, false);
        return new BookLendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookLendingViewHolder holder, final int position) {
        final StudentBookInventory inventory = list.get(position);

        StudentBook book = inventory.getBook();
        holder.textName.setText(book.getBookName());
        holder.textSerialNumber.setText(inventory.getSerialNumber());
        if (inventory.getAction().equals("le"))
            holder.textAction.setText(getTranslation(locale,"LABEL_LENT"));
        else if (inventory.getAction().equals("co"))
            holder.textAction.setText(getTranslation(locale,"LABEL_COLLECTED"));
        holder.imageRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.remove(position);
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

    class BookLendingViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageRemove;
        private TextView textName, textSerialNumber, textAction;

        BookLendingViewHolder(View itemView) {
            super(itemView);
            textSerialNumber = itemView.findViewById(R.id.text_serial_number);
            textName = itemView.findViewById(R.id.text_book_name);
            imageRemove = itemView.findViewById(R.id.image_remove);
            textAction = itemView.findViewById(R.id.text_action);
        }
    }
}
