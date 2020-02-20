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

package net.mavericklabs.read.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import net.mavericklabs.read.model.LoginResponse;
import net.mavericklabs.read.realm.RealmHandler;
import net.mavericklabs.read.retrofit.ApiClient;
import net.mavericklabs.read.retrofit.ApiInterface;
import net.mavericklabs.read.util.DateUtil;
import net.mavericklabs.read.util.Logger;

import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainEmptyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent activityIntent;

        Realm realm = Realm.getDefaultInstance();
        Logger.d("realmPath: " + realm.getPath());
        realm.close();

        // go straight to main if a token is stored
        if (RealmHandler.getAccessToken() != null) {
            if (isTokenExpiring()) {
                refreshToken();
            }

            Logger.d("Logged in " + RealmHandler.getAccessToken());
            activityIntent = new Intent(this, MainActivity.class);
        } else {
            activityIntent = new Intent(this, LoginActivity.class);
        }

        startActivity(activityIntent);
        finish();
    }

    private boolean isTokenExpiring() {
        LoginResponse loginResponse = RealmHandler.getLoginResponse();
        Date d = DateUtil.getDateFromString(loginResponse.getExpiryDate());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.add(Calendar.DAY_OF_YEAR, -5);

        String expiryDate = DateUtil.dateToString(calendar.getTime(), "yyyy-MM-dd");
        String currentDate = DateUtil.dateToString(Calendar.getInstance().getTime(), "yyyy-MM-dd");
        if (expiryDate.equals(currentDate)) {
            Logger.d("Refresh required");
            return true;
        } else {
            return false;
        }
    }

    void refreshToken() {

        ApiInterface apiInterface = ApiClient.getApiInterface(getApplicationContext());
        apiInterface.refreshToken().enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                if (response.isSuccessful()) {
                    LoginResponse res = response.body();
                    if (res != null) {
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        LoginResponse loginResponse = RealmHandler.getLoginResponse();
                        loginResponse.setExpiryDate(res.getExpiryDate());
                        loginResponse.setToken(res.getToken());
                        realm.delete(LoginResponse.class);
                        realm.copyToRealm(loginResponse);
                        realm.commitTransaction();
                        realm.close();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {

            }
        });
    }
}
