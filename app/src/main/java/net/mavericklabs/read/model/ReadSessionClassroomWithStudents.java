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

import java.util.List;

/**
 * Created by Amey on 4/10/2019.
 */

public class ReadSessionClassroomWithStudents {
    private int id;
    private List<ReadStudent> students;
    @SerializedName("classroom_id")
    private int classroomId;

    private ClassroomDetails classroom;

    public ClassroomDetails getClassroom() {
        return classroom;
    }

    public void setClassroom(ClassroomDetails classroom) {
        this.classroom = classroom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<ReadStudent> getStudents() {
        return students;
    }

    public void setStudents(List<ReadStudent> students) {
        this.students = students;
    }

    public int getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(int classroomId) {
        this.classroomId = classroomId;
    }
}
