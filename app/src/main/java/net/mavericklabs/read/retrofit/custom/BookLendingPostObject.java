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


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BookLendingPostObject {
    @SerializedName("student")
    private String studentKey;


    @SerializedName("book")
    private List<InventoryRequest> bookKeys;

    private boolean evaluated;

    public BookLendingPostObject(String studentKey, List<InventoryRequest> bookKeys, boolean evaluated) {
        this.studentKey = studentKey;
        this.bookKeys = bookKeys;
        this.evaluated = evaluated;
    }

    public String getStudentKey() {
        return studentKey;
    }

    public void setStudentKey(String studentKey) {
        this.studentKey = studentKey;
    }

    public List<InventoryRequest> getBookKeys() {
        return bookKeys;
    }

    public void setBookKeys(List<InventoryRequest> bookKeys) {
        this.bookKeys = bookKeys;
    }

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }
}
