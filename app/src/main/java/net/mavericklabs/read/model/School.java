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

public class School extends RealmObject{

    private int id;

    @SerializedName("name")
        private String  schoolName;
    @SerializedName("address")
    private String schoolAddress;
//            "pin_code": 48689,
//            "ward_number": 4,
//            "latitude": null,
//            "longitude": null,
//            "school_number": 3,
    @SerializedName("organization_name")
    private String organizationName;
    @SerializedName("year_of_intervention")
    private String yearOfIntervention;
//            "creation_time": "2019-04-08T06:21:19.833818Z",
//            "last_modification_time": "2019-04-08T09:05:15.356019Z",
    @SerializedName("is_active")
    private boolean isActive;
//


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getSchoolAddress() {
        return schoolAddress;
    }

    public void setSchoolAddress(String schoolAddress) {
        this.schoolAddress = schoolAddress;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getYearOfIntervention() {
        return yearOfIntervention;
    }

    public void setYearOfIntervention(String yearOfIntervention) {
        this.yearOfIntervention = yearOfIntervention;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
