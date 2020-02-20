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

package net.mavericklabs.read.model;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
/**
 * Created by Amey on 4/15/2019.
 */

public class ClassroomEvaluation extends RealmObject{
    private String classroomKey;

    private String sessionKey;
    private RealmList<StudentEvaluation> students;


    public String getClassroomKey() {
        return classroomKey;
    }

    public void setClassroomKey(String classroomKey) {
        this.classroomKey = classroomKey;
    }

    public void setStudents(RealmList<StudentEvaluation> students) {
        this.students = students;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public RealmList<StudentEvaluation> getStudents() {
        return students;
    }

    public void setStudents(List<StudentEvaluation> list) {
        if(this.students == null){
            this.students = new RealmList<>();

        }
        this.students.clear();
        this.students.addAll(list);
    }
}
