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

/**
 * Created by Amey on 4/10/2019.
 */

public class ReadStudent extends RealmObject implements Serializable {

    private int id;
    private String key;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("middle_name")
    private String middleName;
    @SerializedName("last_name")
    private String lastName;
    private String address;
    private String gender;
    @SerializedName("mother_tongue")
    private String motherTongue;

    @SerializedName("level")
    private String previousLevelKey;
    private RealmList<StudentBookInventory> inventory;
//    private RealmList<StudentBook> books;
//              "birth_date": "2001-03-15",
//              "is_dropout": false,
//              "has_attended_preschool": true,
//              "creation_time": "2019-04-09T05:42:27.705234Z",
//              "last_modification_time": "2019-04-09T09:23:03.728647Z",
//              "is_active": true


    public String getPreviousLevelKey() {
        return previousLevelKey;
    }

    public void setPreviousLevelKey(String previousLevelKey) {
        this.previousLevelKey = previousLevelKey;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMotherTongue() {
        return motherTongue;
    }

    public void setMotherTongue(String motherTongue) {
        this.motherTongue = motherTongue;
    }

//    public List<StudentBook> getBooks() {
//        return books;
//    }

    @Override
    public boolean equals(Object obj) {
        ReadStudent readStudent = (ReadStudent) obj;
        return this.key.equals(readStudent.key)
                && this.inventory.equals(((ReadStudent) obj).inventory);
    }

    public RealmList<StudentBookInventory> getInventory() {
        return inventory;
    }

    public void setInventory(List<StudentBookInventory> list) {
        if(this.inventory != null){
            this.inventory = new RealmList<>();
        }
            this.inventory.clear();

        this.inventory.addAll(list);
    }

//    public void setBooks(List<StudentBook> booksList) {
//        if (this.books != null) {
//            this.books = new RealmList<>();
//
//        }
//        this.books.clear();
//        this.books.addAll(booksList);
//    }
}
