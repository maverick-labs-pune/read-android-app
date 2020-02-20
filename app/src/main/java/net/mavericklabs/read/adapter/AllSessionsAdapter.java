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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.mavericklabs.read.R;
import net.mavericklabs.read.activity.SessionDetailsActivity;
import net.mavericklabs.read.model.ClassroomDetails;
import net.mavericklabs.read.model.ReadSession;
import net.mavericklabs.read.model.ReadSessionClassroom;
import net.mavericklabs.read.model.School;
import net.mavericklabs.read.util.DateUtil;
import net.mavericklabs.read.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.List;

import static net.mavericklabs.read.realm.RealmHandler.getTranslation;
import static net.mavericklabs.read.util.Constants.session_details_request_code;
import static net.mavericklabs.read.util.Constants.text_normal;

/**
 * Created by Amey on 4/9/2019.
 */

public class AllSessionsAdapter extends RecyclerView.Adapter<AllSessionsAdapter.AllSessionsViewHolder> {

    private Context context;
    private List<ReadSession> list;
    private String locale;

    public AllSessionsAdapter(Context context, List<ReadSession> list,String locale) {
        this.context = context;
        this.list = list;
        this.locale = locale;
    }

    @NonNull
    @Override
    public AllSessionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_upcoming_session, parent, false);
        return new AllSessionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllSessionsViewHolder holder, int position) {

        String strLocale = SharedPreferenceUtil.getStringPreference(context, "locale");
        final ReadSession readSession = list.get(position);
        List<ReadSessionClassroom> readSessionClassrooms = readSession.getReadSessionClassroom();
        ClassroomDetails classroom = readSessionClassrooms.get(0).getClassroom();
        School school = classroom.getSchool();

        holder.textSchoolName.setText(school.getSchoolName());

        holder.textDate.setText(DateUtil.formatDate(readSession.getStartDateTime(), readSession.getEndDateTime()));


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, SessionDetailsActivity.class);
                intent.putExtra("session_key", readSession.getKey());
                ((Activity) context).startActivityForResult(intent, session_details_request_code);
            }
        });
        if (readSession.isEvaluated()) {
            holder.imageCheck.setVisibility(View.VISIBLE);
        } else {
            holder.imageCheck.setVisibility(View.GONE);
        }

        List<String> classrooms = new ArrayList<>();
        for (ReadSessionClassroom obj : readSessionClassrooms) {
            String classroomName = getTranslation(locale,obj.getClassroom().getStandard().getStandardName());

            if (obj.getClassroom().getDivision() != null){
                classroomName = classroomName + " | " + obj.getClassroom().getDivision();

            }
            classrooms.add(classroomName);
        }

        holder.recyclerViewClassroom.setLayoutManager(new LinearLayoutManager(context));
        StringAdapter adapter = new StringAdapter(context, classrooms, text_normal);
        holder.recyclerViewClassroom.setAdapter(adapter);
        holder.textSessionType.setText(getTranslation(locale,readSession.getSessionType()));

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

    class AllSessionsViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView textSchoolName, textDate, textSessionType;
        private ImageView imageCheck;
        private RecyclerView recyclerViewClassroom;

        AllSessionsViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            textSchoolName = itemView.findViewById(R.id.text_school_name);
            textDate = itemView.findViewById(R.id.text_date);
//            textStandard = itemView.findViewById(R.id.text_standard);
            imageCheck = itemView.findViewById(R.id.image_check);
            textSessionType = itemView.findViewById(R.id.text_session_type);
            recyclerViewClassroom = itemView.findViewById(R.id.recycler_view_classroom);
        }
    }
}
