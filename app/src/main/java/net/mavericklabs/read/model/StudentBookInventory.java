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

public class StudentBookInventory extends RealmObject {

    @SerializedName("serial_number")
    private String serialNumber;

    private String key;

    @SerializedName("is_active")
    private boolean active;

    private String action;

    private StudentBook book;

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public StudentBook getBook() {
        return book;
    }

    public void setBook(StudentBook book) {
        this.book = book;
    }

    @Override
    public boolean equals(Object obj) {
        StudentBookInventory inventory = (StudentBookInventory) obj;
        return this.serialNumber.equals(inventory.getSerialNumber())
                && this.book.equals(inventory.getBook());
    }
}
