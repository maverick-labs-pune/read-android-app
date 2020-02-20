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

package net.mavericklabs.read;

import android.accounts.Account;
import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;


import net.mavericklabs.read.realm.RealmMigration;
import net.mavericklabs.read.sync.SyncAdapter;
import net.mavericklabs.read.util.SharedPreferenceUtil;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static net.mavericklabs.read.util.Constants.en_INLocale;

/**
 * Created by Amey on 1/30/2019.
 */

public class ReadApplication extends Application {
    public static final String SYNC_COMPLETED = "sync_stop";
    private static final String NET_MAVERICKLABS_READ_DATASYNC_PROVIDER = "net.mavericklabs.read.datasync.provider";
    public static final String PENDING_SESSIONS = "pending_sessions";
    public static final String UPCOMING_SESSIONS = "upcoming_sessions";
    public static final String EVALUATED_SESSIONS = "evaluated_sessions";
    public static final String BOOKS = "books";
    public static final String TRANSLATIONS = "translations";
    private static final String BASE_URL = "content://" + NET_MAVERICKLABS_READ_DATASYNC_PROVIDER + "/";
    public static final Uri BASE_URI = Uri.parse(BASE_URL);

    @Override
    public void onCreate() {
        super.onCreate();
        initRealm();
        String locale = SharedPreferenceUtil.getLocale(this);
        if (locale == null)
            SharedPreferenceUtil.setStringPreference(this, "locale", en_INLocale);

        SyncAdapter.createSyncAccount(getApplicationContext());
        String AUTHORITY = getResources().getString(R.string.content_authority);
        Account ACCOUNT = SyncAdapter.getAccount(getApplicationContext());
        ContentResolver.setSyncAutomatically(ACCOUNT, AUTHORITY, true);
    }

    private void initRealm() {
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("readapplication.realm")
                .schemaVersion(1)
                .migration(new RealmMigration())
                .build();
        Realm.setDefaultConfiguration(config);
    }


    @Override
    public void onTerminate() {
        Realm.getDefaultInstance().close();
        super.onTerminate();
    }

}
