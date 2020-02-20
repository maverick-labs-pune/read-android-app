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

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created by Amey on 4/10/2019.
 */

public class StudentEvaluation extends RealmObject implements Serializable {

    private String studentKey;
    private String sessionKey;
    private ReadStudent student;
    private boolean attendance;
    @SerializedName("comments")
    private String comment;
    private boolean evaluated;
    private StudentLevel level;
    private boolean localEvaluated;

//    private ReadSession session;

//    public StudentEvaluation(){
//
//    }
//
//    public StudentEvaluation(ReadStudent student) {
//        this.student = student;
//        this.attendance = false;
//        this.comment = "";
//        this.level = null;
//        this.session = null;
//    }


    public boolean isLocalEvaluated() {
        return localEvaluated;
    }

    public void setLocalEvaluated(boolean localEvaluated) {
        this.localEvaluated = localEvaluated;
    }

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }

    public String getStudentKey() {
        return studentKey;
    }

    public void setStudentKey(String studentKey) {
        this.studentKey = studentKey;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

//    public ReadSession getSession() {
//        return session;
//    }
//
//    public void setSession(ReadSession session) {
//        this.session = session;
//    }

    public StudentLevel getLevel() {
        return level;
    }

    public void setLevel(StudentLevel level) {
        this.level = level;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ReadStudent getStudent() {
        return student;
    }

    public void setStudent(ReadStudent student) {
        this.student = student;
    }

    public boolean isAttendance() {
        return attendance;
    }

    public void setAttendance(boolean attendance) {
        this.attendance = attendance;
    }

    @Override
    public boolean equals(Object o) {
        StudentEvaluation obj = (StudentEvaluation) o;
        boolean levelEqual = false;

        if (this.level != null && obj.level != null) {
            levelEqual = this.level.equals(obj.getLevel());
        } else {
            if (this.level != null && obj.level == null)
                levelEqual = false;
            if (this.level == null && obj.level != null)
                levelEqual = false;
        }

        if (this.level == null && obj.getLevel() == null)
            levelEqual = true;

        return this.studentKey.equals(obj.getStudentKey())
                && this.attendance == (obj.isAttendance())
                && this.evaluated == (obj.isEvaluated())
                && this.localEvaluated == obj.isLocalEvaluated()
                && levelEqual
                && this.student.equals(obj.student)
                && this.comment.equals(obj.getComment());


    }
}
