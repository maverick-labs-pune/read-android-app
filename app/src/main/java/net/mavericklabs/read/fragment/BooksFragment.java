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

package net.mavericklabs.read.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.mavericklabs.read.R;
import net.mavericklabs.read.ReadApplication;
import net.mavericklabs.read.realm.RealmHandler;
import net.mavericklabs.read.sync.SyncAdapter;
import net.mavericklabs.read.util.Logger;
import net.mavericklabs.read.util.NetworkConnection;
import net.mavericklabs.read.util.SharedPreferenceUtil;

import static net.mavericklabs.read.realm.RealmHandler.getTranslation;


public class BooksFragment extends Fragment {


    private Button btnSyncBooks;
    private String locale;
    private ContentObserver syncCompletedObserver;
    private final Object updateLock = new Object();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_books, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnSyncBooks = view.findViewById(R.id.btn_sync_books);
        btnSyncBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (context == null) {
                    return;
                }
                if (NetworkConnection.isNetworkAvailable(context)) {
                    boolean result = SyncAdapter.requestSync(context, SyncAdapter.SYNC_BOOKS);
                    if (!result) {
                        Toast.makeText(context, getTranslation(locale, "SYNC_ALREADY_RUNNING"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, getTranslation(locale, "SYNC_STARTED"), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, getTranslation(locale, "NO_NETWORK"), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.search);
        if(item!=null)
            item.setVisible(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTranslations();
    }

    private void setTranslations() {
        locale = SharedPreferenceUtil.getLocale(getContext());
        btnSyncBooks.setText(getTranslation(locale, "LABEL_SYNC_BOOKS"));
    }

    @Override
    public void onStart() {

        super.onStart();
        Activity activity = getActivity();
        if (activity != null) {
            ContentResolver contentResolver = activity.getContentResolver();
            if (syncCompletedObserver != null) {
                contentResolver.unregisterContentObserver(syncCompletedObserver);
            }
            syncCompletedObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                public void onChange(boolean selfChange) {
                    Logger.d("On change syncCompletedObserver");
                    synchronized (updateLock) {
                        Logger.d("On change sync complete " + RealmHandler.getEvaluatedSessions());
                        Toast.makeText(getContext(), getTranslation(locale, "SYNC_COMPLETE"), Toast.LENGTH_SHORT).show();
                    }
                }
            };
            Uri syncCompletedURI = Uri.withAppendedPath(ReadApplication.BASE_URI,
                    ReadApplication.SYNC_COMPLETED);

            contentResolver.registerContentObserver(syncCompletedURI,
                    true, syncCompletedObserver);
            Logger.d("Registered observers");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Activity activity = getActivity();
        if (activity != null) {
            ContentResolver contentResolver = activity.getContentResolver();
            if (syncCompletedObserver != null) {
                contentResolver.unregisterContentObserver(syncCompletedObserver);
            }
        }
    }

}
