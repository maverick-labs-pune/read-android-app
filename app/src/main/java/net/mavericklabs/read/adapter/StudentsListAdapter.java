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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import net.mavericklabs.read.R;
import net.mavericklabs.read.activity.BookLendingActivity;
import net.mavericklabs.read.activity.StudentDetailsActivity;
import net.mavericklabs.read.model.ReadStudent;
import net.mavericklabs.read.model.StudentEvaluation;
import java.util.List;

import static net.mavericklabs.read.util.Constants.book_lending_request_code;
import static net.mavericklabs.read.util.Constants.session_book_lending;
import static net.mavericklabs.read.util.Constants.student_details_request_code;

/**
 * Created by Amey on 4/12/2019.
 */

public class StudentsListAdapter extends RecyclerView.Adapter<StudentsListAdapter.StudentsViewHolder> {

    private Context context;
    private List<StudentEvaluation> list;
    private String sessionType;
    private String classroomKey;

    public StudentsListAdapter(Context context, String sessionType, List<StudentEvaluation> list, String classroomKey) {
        this.context = context;
        this.sessionType = sessionType;
        this.list = list;
        this.classroomKey = classroomKey;
    }

    @NonNull
    @Override
    public StudentsListAdapter.StudentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_name, parent, false);
        return new StudentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentsListAdapter.StudentsViewHolder holder, int position) {

        final StudentEvaluation studentEvaluation = list.get(position);
        final ReadStudent student = studentEvaluation.getStudent();
        holder.textStudentName.setText(student.getFirstName() + " " + student.getLastName());
        if (studentEvaluation.isLocalEvaluated())
            holder.imageCheck.setVisibility(View.VISIBLE);
        else
            holder.imageCheck.setVisibility(View.GONE);
//        Logger.d("session_key "+studentEvaluation.getSessionKey());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sessionType.equals(session_book_lending)) {
                    Intent intent = new Intent(context, BookLendingActivity.class);
                    intent.putExtra("student_key", student.getKey());
                    intent.putExtra("session_key", studentEvaluation.getSessionKey());
                    intent.putExtra("classroom_id", classroomKey);
                    ((Activity) context).startActivityForResult(intent, book_lending_request_code);
                } else {
                    Intent intent = new Intent(context, StudentDetailsActivity.class);
                    intent.putExtra("student_key", student.getKey());
                    intent.putExtra("session_type", sessionType);
                    intent.putExtra("session_key", studentEvaluation.getSessionKey());
                    intent.putExtra("classroom_id", classroomKey);
                    ((Activity) context).startActivityForResult(intent, student_details_request_code);
                }

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

    class StudentsViewHolder extends RecyclerView.ViewHolder {
        TextView textStudentName;
        CardView cardView;
        ImageView imageCheck;

        StudentsViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            textStudentName = itemView.findViewById(R.id.text_student_name);
            imageCheck = itemView.findViewById(R.id.image_evaluated);
        }
    }
}
