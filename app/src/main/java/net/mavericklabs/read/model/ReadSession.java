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
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Amey on 4/9/2019.
 */

public class ReadSession extends RealmObject implements Serializable {

    private int id;
    //              "academic_year_id": 1,
    @PrimaryKey
    private String key;
    @SerializedName("start_date_time")
    private String startDateTime;

    @SerializedName("end_date_time")
    private String endDateTime;
    @SerializedName("type")
    private String sessionType;
    @SerializedName("is_evaluated")
    private boolean isEvaluated;
    @SerializedName("is_verified")
    private boolean isVerified;
    @SerializedName("is_cancelled")
    private boolean isCancelled;
    @SerializedName("readsessionclassroom_set")
    private RealmList<ReadSessionClassroom> readSessionClassroom;

    @SerializedName("readsessionbookfairy_set")
    private RealmList<ReadSessionBookFairy> bookFairySet;

    private boolean storedOffline;

    @SerializedName("start_time")
    private String startTime;
    @SerializedName("end_time")
    private String endTime;

    private String notes;

    public RealmList<ReadSessionBookFairy> getBookFairySet() {
        return bookFairySet;
    }

    public void setBookFairySet(List<ReadSessionBookFairy> list) {

        if (this.bookFairySet == null)
            this.bookFairySet = new RealmList<>();

        this.bookFairySet.clear();
        this.bookFairySet.addAll(list);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setReadSessionClassroom(RealmList<ReadSessionClassroom> readSessionClassroom) {
        this.readSessionClassroom = readSessionClassroom;
    }

    public boolean isStoredOffline() {
        return storedOffline;
    }

    public void setStoredOffline(boolean storedOffline) {
        this.storedOffline = storedOffline;
    }

    public RealmList<ReadSessionClassroom> getReadSessionClassroom() {
        return readSessionClassroom;
    }

    public void setReadSessionClassroom(List<ReadSessionClassroom> list) {
        if (this.readSessionClassroom == null)
            this.readSessionClassroom = new RealmList<>();

        this.readSessionClassroom.clear();
        this.readSessionClassroom.addAll(list);

    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public boolean isEvaluated() {
        return isEvaluated;
    }

    public void setEvaluated(boolean evaluated) {
        isEvaluated = evaluated;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }


}
