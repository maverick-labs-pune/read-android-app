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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.mavericklabs.read.R;
import net.mavericklabs.read.model.ClassroomDetails;
import net.mavericklabs.read.model.ReadSessionClassroom;
import net.mavericklabs.read.model.School;
import net.mavericklabs.read.model.SessionInReadSessionClassroom;
import net.mavericklabs.read.model.Standard;
import net.mavericklabs.read.model.StudentEvaluation;
import net.mavericklabs.read.realm.RealmHandler;
import net.mavericklabs.read.util.Logger;

import java.util.List;

import static net.mavericklabs.read.realm.RealmHandler.getTranslation;

/**
 * Created by Amey on 4/11/2019.
 */

public class SessionDetailsAdapter extends RecyclerView.Adapter<SessionDetailsAdapter.SessionDetailsViewHolder> {
    private Context context;
    private List<ReadSessionClassroom> list;
    private String locale;
    private String sessionType;

    public SessionDetailsAdapter(Context context, String sessionType, List<ReadSessionClassroom> list, String locale) {
        this.context = context;
        this.sessionType = sessionType;
        this.list = list;
        this.locale = locale;
    }

    public void setList(List<ReadSessionClassroom> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public SessionDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_session_details, parent, false);
        return new SessionDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SessionDetailsViewHolder holder, int position) {
        Logger.d("Classrooms size : " + list.size());
        ReadSessionClassroom obj = list.get(position);
        ClassroomDetails classroom = obj.getClassroom();
        SessionInReadSessionClassroom session = obj.getSession();
        School school = classroom.getSchool();
        Standard standard = classroom.getStandard();
        holder.textStandard.setText(getTranslation(locale, standard.getStandardName()));
        holder.textLabelStudents.setText(getTranslation(locale, "LABEL_STUDENT"));
        holder.textLabelStudents1.setText(getTranslation(locale, "LABEL_STUDENT"));

        if (classroom.getDivision() != null)
            holder.textDivision.setText(classroom.getDivision());

        holder.arrowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.studentsRecyclerView.getVisibility() == View.VISIBLE) {
                    holder.studentsRecyclerView.setVisibility(View.GONE);
                    holder.arrowImage.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.arrow_down));
                } else {
                    holder.studentsRecyclerView.setVisibility(View.VISIBLE);
                    holder.arrowImage.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.arrow_up));
                }
            }
        });
//        getStudentsList(session.getKey(), holder, position);
        List<StudentEvaluation> students = RealmHandler.getStudentsOfClassroom(obj.getClassroom().getKey(), obj.getSession().getKey());
        holder.textStudentsCount.setText(String.valueOf(students.size()));
        StudentsListAdapter adapter = new StudentsListAdapter(context,sessionType, students, obj.getClassroom().getKey());
        holder.studentsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.studentsRecyclerView.setAdapter(adapter);

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class SessionDetailsViewHolder extends RecyclerView.ViewHolder {

        private final TextView textLabelStudents;
        private final TextView textLabelStudents1;
        private TextView textDate, textSchoolName, textStudentsCount, textStandard, textDivision, textSendReport;
        private RecyclerView studentsRecyclerView;
        private ImageView arrowImage;

        public SessionDetailsViewHolder(View itemView) {
            super(itemView);
//            textDate = itemView.findViewById(R.id.text_date);
//            textSchoolName = itemView.findViewById(R.id.text_school_name);
            textStandard = itemView.findViewById(R.id.text_standard);
            textDivision = itemView.findViewById(R.id.text_division);
            textStudentsCount = itemView.findViewById(R.id.text_students_count);
            textLabelStudents = itemView.findViewById(R.id.text_label_students);
            textLabelStudents1 = itemView.findViewById(R.id.text_label_students_1);
            textSendReport = itemView.findViewById(R.id.text_send_report);
            studentsRecyclerView = itemView.findViewById(R.id.students_recyclerview);
            arrowImage = itemView.findViewById(R.id.arrow_image);

        }
    }
}
