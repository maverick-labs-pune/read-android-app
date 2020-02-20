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

package net.mavericklabs.read.retrofit.custom;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Amey on 4/18/2019.
 */

public class StudentEvaluationPost {
    @SerializedName("student")
    private String studentKey;

    @Nullable
    @SerializedName("level")
    private String levelName;

    @SerializedName("book")
    private List<InventoryRequest> bookKeys;

    private String comments;
    private boolean attendance;
    private boolean evaluated;

    public StudentEvaluationPost(String studentKey, String levelName, List<InventoryRequest> bookKeys, String comments, boolean attendance, boolean evaluated) {
        this.studentKey = studentKey;
        this.levelName = levelName;
        this.bookKeys = bookKeys;
        this.comments = comments;
        this.attendance = attendance;
        this.evaluated = evaluated;
    }

    public String getStudentKey() {
        return studentKey;
    }

    public void setStudentKey(String studentKey) {
        this.studentKey = studentKey;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public List<InventoryRequest> getBookKeys() {
        return bookKeys;
    }

    public void setBookKeys(List<InventoryRequest> bookKeys) {
        this.bookKeys = bookKeys;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isAttendance() {
        return attendance;
    }

    public void setAttendance(boolean attendance) {
        this.attendance = attendance;
    }

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }
}
