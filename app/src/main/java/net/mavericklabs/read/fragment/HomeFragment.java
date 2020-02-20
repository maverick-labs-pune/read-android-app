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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.mavericklabs.read.R;
import net.mavericklabs.read.ReadApplication;
import net.mavericklabs.read.adapter.AllSessionsAdapter;
import net.mavericklabs.read.model.LoginResponse;
import net.mavericklabs.read.model.ReadSession;
import net.mavericklabs.read.realm.RealmHandler;
import net.mavericklabs.read.sync.SyncAdapter;
import net.mavericklabs.read.util.Logger;
import net.mavericklabs.read.util.NetworkConnection;
import net.mavericklabs.read.util.SharedPreferenceUtil;

import java.util.List;

import static net.mavericklabs.read.realm.RealmHandler.getTranslation;


public class HomeFragment extends Fragment {

    private RecyclerView upcomingRecyclerView, pendingRecyclerView, evaluatedRecyclerView;
    private LoginResponse loginResponse;
    private ProgressBar progressPending, progressUpcoming, progressEvaluated;
    private TextView textPendingNoSessions, textEvaluatedNoSessions, textUpcomingNoSessions;
    private TextView textLabelPendingSessions;
    private String locale;
    private TextView textLabelEvaluatedSessions;
    private TextView textLabelUpcomingSessions;
    private ContentObserver pendingSessionsObserver;
    private final Object updateLock = new Object();
    private ContentObserver upcomingSessionsObserver;
    private ContentObserver evaluatedSessionsObserver;
    private ContentObserver syncCompletedObserver;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.search);
        if(item!=null)
            item.setVisible(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        upcomingRecyclerView = view.findViewById(R.id.recyclerview_upcoming);
        pendingRecyclerView = view.findViewById(R.id.recyclerview_pending);
        evaluatedRecyclerView = view.findViewById(R.id.recyclerview_evaluated);
        loginResponse = RealmHandler.getLoginResponse();
        progressPending = view.findViewById(R.id.progress_bar_pending);
        progressUpcoming = view.findViewById(R.id.progress_bar_upcoming);
        progressEvaluated = view.findViewById(R.id.progress_bar_evaluated);

        textPendingNoSessions = view.findViewById(R.id.text_pending_no_sessions);
        textUpcomingNoSessions = view.findViewById(R.id.text_upcoming_no_sessions);
        textEvaluatedNoSessions = view.findViewById(R.id.text_evaluated_no_sessions);

        textLabelPendingSessions = view.findViewById(R.id.text_label_pending_sessions);
        textLabelEvaluatedSessions = view.findViewById(R.id.text_label_evaluated_sessions);
        textLabelUpcomingSessions = view.findViewById(R.id.text_label_upcoming_sessions);
        FloatingActionButton floatActionButtonSync = view.findViewById(R.id.floating_action_button_sync);


        upcomingRecyclerView.setNestedScrollingEnabled(false);
        pendingRecyclerView.setNestedScrollingEnabled(false);
        evaluatedRecyclerView.setNestedScrollingEnabled(false);
        floatActionButtonSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
                if (context != null) {

                    if (NetworkConnection.isNetworkAvailable(context)) {
                        boolean result = SyncAdapter.requestSync(context, SyncAdapter.SYNC_SESSIONS);
                        if (!result) {
                            Toast.makeText(context, getTranslation(locale, "SYNC_ALREADY_RUNNING"), Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(context, getTranslation(locale, "SYNC_STARTED"), Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        Toast.makeText(context, getTranslation(locale, "NO_NETWORK"), Toast.LENGTH_SHORT).show();

                    }


                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        setTranslations();
        if (loginResponse != null) {

            displayUpcomingSessions(RealmHandler.getUpcomingSessions());
            displayPendingSessions(RealmHandler.getPendingSessions());
            displayEvaluatedSessions(RealmHandler.getEvaluatedSessions());

            pendingSessionsObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                public void onChange(boolean selfChange) {
                    Logger.d("On change pendingSessionsObserver");
                    synchronized (updateLock) {
                        Logger.d("On change pendingSessionsObserver " + RealmHandler.getPendingSessions());
                        displayPendingSessions(RealmHandler.getPendingSessions());
                    }
                }
            };
            upcomingSessionsObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                public void onChange(boolean selfChange) {
                    Logger.d("On change upcomingSessionsObserver");

                    synchronized (updateLock) {
                        Logger.d("On change upcomingSessionsObserver " + RealmHandler.getUpcomingSessions());
                        displayUpcomingSessions(RealmHandler.getUpcomingSessions());
                    }
                }
            };
            evaluatedSessionsObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                public void onChange(boolean selfChange) {
                    Logger.d("On change evaluatedSessionsObserver");
                    synchronized (updateLock) {
                        Logger.d("On change evaluatedSessionsObserver " + RealmHandler.getEvaluatedSessions());
                        displayEvaluatedSessions(RealmHandler.getEvaluatedSessions());
                    }
                }
            };
            syncCompletedObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                public void onChange(boolean selfChange) {
                    Logger.d("On change syncCompletedObserver");
                    synchronized (updateLock) {
                        Logger.d("On change sync complete " + RealmHandler.getEvaluatedSessions());
                        Toast.makeText(getContext(), getTranslation(locale, "SYNC_COMPLETE"), Toast.LENGTH_SHORT).show();
                    }
                }
            };
            Uri pendingSessionsURI = Uri.withAppendedPath(ReadApplication.BASE_URI,
                    ReadApplication.PENDING_SESSIONS);
            Uri upcomingSessionsURI = Uri.withAppendedPath(ReadApplication.BASE_URI,
                    ReadApplication.UPCOMING_SESSIONS);
            Uri evaluatedSessionsURI = Uri.withAppendedPath(ReadApplication.BASE_URI,
                    ReadApplication.EVALUATED_SESSIONS);
            Uri syncCompletedURI = Uri.withAppendedPath(ReadApplication.BASE_URI,
                    ReadApplication.SYNC_COMPLETED);
            Activity activity = getActivity();
            if (activity != null) {
                ContentResolver contentResolver = activity.getContentResolver();
                contentResolver.registerContentObserver(pendingSessionsURI,
                        true, pendingSessionsObserver);
                contentResolver.registerContentObserver(upcomingSessionsURI,
                        true, upcomingSessionsObserver);
                contentResolver.registerContentObserver(evaluatedSessionsURI,
                        true,evaluatedSessionsObserver);
                 contentResolver.registerContentObserver(syncCompletedURI,
                        true, syncCompletedObserver);
                Logger.d("Registered observers");
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Activity activity = getActivity();
        if (activity != null) {
            ContentResolver contentResolver = activity.getContentResolver();
            if (pendingSessionsObserver != null) {
                contentResolver.unregisterContentObserver(pendingSessionsObserver);
            }
            if (upcomingSessionsObserver != null) {
                contentResolver.unregisterContentObserver(upcomingSessionsObserver);
            }
            if (evaluatedSessionsObserver != null) {
                contentResolver.unregisterContentObserver(evaluatedSessionsObserver);
            }
            if (syncCompletedObserver != null) {
                contentResolver.unregisterContentObserver(syncCompletedObserver);
            }
        }
    }

    private void setTranslations() {
        locale = SharedPreferenceUtil.getLocale(getContext());
        textLabelPendingSessions.setText(getTranslation(locale, "LABEL_PENDING_SESSIONS"));
        textLabelEvaluatedSessions.setText(getTranslation(locale, "LABEL_EVALUATED_SESSIONS"));
        textLabelUpcomingSessions.setText(getTranslation(locale, "LABEL_UPCOMING_SESSIONS"));
        textPendingNoSessions.setText(getTranslation(locale, "LABEL_NO_SESSIONS"));
        textEvaluatedNoSessions.setText(getTranslation(locale, "LABEL_NO_SESSIONS"));
        textUpcomingNoSessions.setText(getTranslation(locale, "LABEL_NO_SESSIONS"));
    }

    private void displayUpcomingSessions(List<ReadSession> readSessions) {
        if(readSessions!=null){
            Logger.d("sessions size displayUpcomingSessions " + readSessions.size());
            AllSessionsAdapter adapter = new AllSessionsAdapter(getContext(), readSessions, locale);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
            upcomingRecyclerView.setLayoutManager(layoutManager);
            upcomingRecyclerView.setAdapter(adapter);
            upcomingRecyclerView.setVisibility(View.VISIBLE);
            progressUpcoming.setVisibility(View.GONE);
            if (readSessions.size() == 0)
                textUpcomingNoSessions.setVisibility(View.VISIBLE);
            else
                textUpcomingNoSessions.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }

    }

    private void displayPendingSessions(List<ReadSession> readSessions) {
        if(readSessions!=null){
            Logger.d("sessions size displayPendingSessions " + readSessions.size());
            AllSessionsAdapter adapter = new AllSessionsAdapter(getContext(), readSessions, locale);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
            pendingRecyclerView.setLayoutManager(layoutManager);
            pendingRecyclerView.setAdapter(adapter);
            pendingRecyclerView.setVisibility(View.VISIBLE);
            progressPending.setVisibility(View.GONE);
            if (readSessions.size() == 0)
                textPendingNoSessions.setVisibility(View.VISIBLE);
            else
                textPendingNoSessions.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }

    }

    private void displayEvaluatedSessions(List<ReadSession> readSessions) {
        if(readSessions!=null){
            Logger.d("sessions size displayEvaluatedSessions " + readSessions.size());

            AllSessionsAdapter adapter = new AllSessionsAdapter(getContext(), readSessions, locale);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
            evaluatedRecyclerView.setLayoutManager(layoutManager);
            evaluatedRecyclerView.setAdapter(adapter);
            evaluatedRecyclerView.setVisibility(View.VISIBLE);
            progressEvaluated.setVisibility(View.GONE);
            if (readSessions.size() == 0)
                textEvaluatedNoSessions.setVisibility(View.VISIBLE);
            else
                textEvaluatedNoSessions.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }

    }


}
