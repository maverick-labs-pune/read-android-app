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

public class ReadSessionBookFairy extends RealmObject {

    @SerializedName("book_fairy")
    private BookFairy bookFairy;

    public BookFairy getBookFairy() {
        return bookFairy;
    }

    public void setBookFairy(BookFairy bookFairy) {
        this.bookFairy = bookFairy;
    }
}