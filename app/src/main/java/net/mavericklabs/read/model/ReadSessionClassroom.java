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

import io.realm.RealmObject;

/**
 * Created by Amey on 4/9/2019.
 */

public class ReadSessionClassroom extends RealmObject{
//    private ReadSession readSession;
    @SerializedName("read_session")
    private SessionInReadSessionClassroom session;
    private ClassroomDetails classroom;

//    public ReadSession getReadSession() {
//        return readSession;
//    }
//
//    public void setReadSession(ReadSession readSession) {
//        this.readSession = readSession;
//    }


    public SessionInReadSessionClassroom getSession() {
        return session;
    }

    public void setSession(SessionInReadSessionClassroom session) {
        this.session = session;
    }

    public ClassroomDetails getClassroom() {
        return classroom;
    }

    public void setClassroom(ClassroomDetails classroom) {
        this.classroom = classroom;
    }
}
