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

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class Translation {
    @SerializedName("en_IN")
    private JsonObject english;
    @SerializedName("mr_IN")
    private JsonObject marathi;

    public JsonObject getEnglish() {
        return english;
    }

    public void setEnglish(JsonObject english) {
        this.english = english;
    }

    public JsonObject getMarathi() {
        return marathi;
    }

    public void setMarathi(JsonObject marathi) {
        this.marathi = marathi;
    }
}
