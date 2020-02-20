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

package net.mavericklabs.read.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.mavericklabs.read.R;
import net.mavericklabs.read.ReadApplication;
import net.mavericklabs.read.model.Book;
import net.mavericklabs.read.model.ClassroomEvaluation;
import net.mavericklabs.read.model.Inventory;
import net.mavericklabs.read.model.LoginResponse;
import net.mavericklabs.read.model.ReadSession;
import net.mavericklabs.read.model.ReadSessionClassroomWithStudents;
import net.mavericklabs.read.model.ReadStudent;
import net.mavericklabs.read.model.RealmTranslation;
import net.mavericklabs.read.model.StudentBookInventory;
import net.mavericklabs.read.model.StudentEvaluation;
import net.mavericklabs.read.model.StudentLevel;
import net.mavericklabs.read.model.Translation;
import net.mavericklabs.read.realm.RealmHandler;
import net.mavericklabs.read.retrofit.ApiClient;
import net.mavericklabs.read.retrofit.ApiInterface;
import net.mavericklabs.read.retrofit.custom.BookLendingPostObject;
import net.mavericklabs.read.retrofit.custom.BookLendingSubmitRequest;
import net.mavericklabs.read.retrofit.custom.InventoryRequest;
import net.mavericklabs.read.retrofit.custom.StudentEvaluationPost;
import net.mavericklabs.read.retrofit.custom.StudentEvaluationRequest;
import net.mavericklabs.read.util.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.ACCOUNT_SERVICE;
import static net.mavericklabs.read.util.Constants.en_INLocale;
import static net.mavericklabs.read.util.Constants.mr_INLocale;
import static net.mavericklabs.read.util.Constants.session_book_lending;

/**
 * Created by Amey on 1/31/2019.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final int SYNC_EVERYTHING = 0;
    public static final int SYNC_BOOKS = 1;
    public static final int SYNC_SESSIONS = 2;
    public static final int SYNC_TRANSLATIONS = 3;

    SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public static boolean requestSync(Context context, int syncType) {
        Logger.d("requestSync syncAdapter");
        String AUTHORITY = context.getResources().getString(R.string.content_authority);
        String ACCOUNT_NAME = context.getResources().getString(R.string.sync_account_name);
        String ACCOUNT_TYPE = context.getResources().getString(R.string.sync_account_type);

        Account ACCOUNT = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
        boolean isSyncOn = ContentResolver.isSyncActive(ACCOUNT, AUTHORITY);
        if (!isSyncOn) {
            Bundle settingsBundle = new Bundle();
            settingsBundle.putInt("syncType", syncType);
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            ContentResolver.requestSync(ACCOUNT, AUTHORITY, settingsBundle);
            return true;
        } else {
            return false;

        }
    }

    public static boolean isSyncActive(Context context) {
        String AUTHORITY = context.getResources().getString(R.string.content_authority);
        String ACCOUNT_NAME = context.getResources().getString(R.string.sync_account_name);
        String ACCOUNT_TYPE = context.getResources().getString(R.string.sync_account_type);
        Account ACCOUNT = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
        return ContentResolver.isSyncActive(ACCOUNT, AUTHORITY);
    }

    public static Account getAccount(Context context) {
        String ACCOUNT_NAME = context.getResources().getString(R.string.sync_account_name);
        String ACCOUNT_TYPE = context.getResources().getString(R.string.sync_account_type);
        return new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
    }

    public static void createSyncAccount(Context context) {
        Account account = getAccount(context);
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        if (accountManager != null && accountManager.addAccountExplicitly(account, null, null)) {
            final long SYNC_FREQUENCY = 60 * 60; // 1 hour (seconds)
            String AUTHORITY = context.getResources().getString(R.string.content_authority);
            ContentResolver.setIsSyncable(account, AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
            ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), SYNC_FREQUENCY);
        }
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        Logger.d("onPerformSync");
        LoginResponse loginResponse = RealmHandler.getLoginResponse();
        if (loginResponse == null) {
            Logger.d("login response null");
            return;
        }
        int syncType = extras.getInt("syncType");
        switch (syncType) {
            case SYNC_BOOKS:
                syncBooks(loginResponse);
                notifySyncStopped();
                break;
            case SYNC_TRANSLATIONS:
                syncTranslations();
                notifySyncStopped();
                break;
            case SYNC_SESSIONS:
                uploadLocallyEvaluatedSessions();
                syncSessions(loginResponse);
                notifySyncStopped();
                break;
            case SYNC_EVERYTHING:
                uploadLocallyEvaluatedSessions();
                syncSessions(loginResponse);
                syncBooks(loginResponse);
                break;
        }

    }


    private void notifySyncStopped() {
        ContentResolver contentResolver = getContext().getContentResolver();
        Uri uri = Uri.withAppendedPath(ReadApplication.BASE_URI, ReadApplication.SYNC_COMPLETED);
        contentResolver.notifyChange(uri, null);
        Logger.d("notifyChange Sync stopped");
    }

    private void syncTranslations() {
        try {
            Response<Translation> response = ApiClient.getApiInterface(getContext()).getTranslation().execute();
            if (response.isSuccessful()) {
                Translation translation = response.body();
                if (translation != null) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    realm.delete(RealmTranslation.class);

                    JsonObject englishJson = translation.getEnglish();
                    Set<Map.Entry<String, JsonElement>> englishEntries = englishJson.entrySet();//will return members of your object

                    for (Map.Entry<String, JsonElement> entry : englishEntries) {
                        RealmTranslation realmTranslation = new RealmTranslation(en_INLocale, entry.getKey(), entry.getValue().getAsString());
                        realm.copyToRealm(realmTranslation);
                    }
                    JsonObject marathiJson = translation.getMarathi();
                    Set<Map.Entry<String, JsonElement>> marathiEntries = marathiJson.entrySet();//will return members of your object

                    for (Map.Entry<String, JsonElement> entry : marathiEntries) {
                        RealmTranslation realmTranslation = new RealmTranslation(mr_INLocale, entry.getKey(), entry.getValue().getAsString());
                        realm.copyToRealm(realmTranslation);
                        Logger.d(String.valueOf(entry.getValue()));
                    }
                    realm.commitTransaction();
                    realm.close();
                }
                ContentResolver contentResolver = getContext().getContentResolver();
                Uri uri = Uri.withAppendedPath(ReadApplication.BASE_URI, ReadApplication.TRANSLATIONS);
                contentResolver.notifyChange(uri, null);
                Logger.d("notifyChange TRANSLATIONS");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void syncBooks(LoginResponse loginResponse) {

        try {
            Response<List<Book>> response = ApiClient.getApiInterface(getContext()).getBooksList(loginResponse.getNgoName()).execute();
            if (response.isSuccessful()) {
                RealmHandler.updateBooks(response.body());
                List<Book> books = response.body();
                if (books != null) {
                    for (int i = 0; i < books.size(); i++) {
                        final Book book = books.get(i);
                        Response<List<Inventory>> inventoryResponse = ApiClient.getApiInterface(getContext()).getBookInventory(book.getKey()).execute();
                        if (inventoryResponse.isSuccessful()) {
                            List<Inventory> list = inventoryResponse.body();
                            if (list != null) {
                                for (Inventory inventory : list) {
                                    inventory.setBookKey(book.getKey());
                                }
                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();
                                realm.copyToRealmOrUpdate(list);
                                realm.commitTransaction();
                                realm.close();
                            }
                        }
                    }
                }
                ContentResolver contentResolver = getContext().getContentResolver();
                Uri uri = Uri.withAppendedPath(ReadApplication.BASE_URI, ReadApplication.BOOKS);
                contentResolver.notifyChange(uri, null);
                Logger.d("notifyChange BOOKS");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void syncSessions(LoginResponse loginResponse) {
        loadPendingSessions(loginResponse);
    }

    private void uploadLocallyEvaluatedSessions() {
        List<ReadSession> sessionList = RealmHandler.getAllEditedReadSessions();
        if (sessionList != null && sessionList.size() > 0) {
            for (ReadSession session : sessionList) {
                List<StudentEvaluation> list = RealmHandler.getEvaluatedStudents(session.getKey());
                if (list != null) {
                    if (session.getSessionType().equals(session_book_lending))
                        submitBookLendingSession(list, session.getKey());
                    else
                        sendReport(list, session.getKey());
                }

            }
        }
    }

    private void sendReport(List<StudentEvaluation> list, final String sessionKey) {


        List<StudentEvaluationPost> req = new ArrayList<>();
        for (StudentEvaluation student : list) {
            List<InventoryRequest> books = new ArrayList<>();
            for (StudentBookInventory inventory : student.getStudent().getInventory()) {
                books.add(new InventoryRequest(inventory.getKey(), inventory.getBook().getKey()));
            }
            StudentLevel level = student.getLevel();
            String levelKey;
            if (level != null && level.getKey() != null) {
                levelKey = level.getKey();
            } else {
                levelKey = "";
            }
            StudentEvaluationPost obj = new StudentEvaluationPost(student.getStudentKey(), levelKey, books, student.getComment(), student.isAttendance(), student.isEvaluated());
            req.add(obj);
        }

        ApiInterface apiInterface = ApiClient.getApiInterface(getContext());
        StudentEvaluationRequest studentEvaluationRequest = new StudentEvaluationRequest(req);
        Gson gson = new Gson();
        String json = gson.toJson(studentEvaluationRequest);

        try {
            Response<Void> response = apiInterface.submitSession(sessionKey, studentEvaluationRequest).execute();
            if (response.isSuccessful()) {
                Realm realm = Realm.getDefaultInstance();
                ReadSession readSession = realm.where(ReadSession.class).equalTo("key", sessionKey).findFirst();
                if (readSession != null) {
                    addComment(readSession.getNotes(),readSession.getKey());
                    realm.beginTransaction();
                    readSession.setStoredOffline(false);
                    realm.commitTransaction();

                    realm.close();

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            Logger.d("onFailure");
        }
    }

    private void addComment(String strNotes,String sessionKey) {

        if (strNotes != null && !strNotes.trim().equals("")) {
            try {
                ApiClient.getApiInterface(getContext()).addCommentToSession(sessionKey, strNotes).execute();
            } catch (IOException e) {
                e.printStackTrace();
                Logger.d("onFailure");
            }
        }

    }

    private void submitBookLendingSession(List<StudentEvaluation> list, final String sessionKey) {
        List<BookLendingPostObject> req = new ArrayList<>();
        for (StudentEvaluation student : list) {
            List<InventoryRequest> books = new ArrayList<>();
            for (StudentBookInventory inventory : student.getStudent().getInventory()) {
                books.add(new InventoryRequest(inventory.getKey(), inventory.getBook().getKey(), inventory.getAction()));
            }
            BookLendingPostObject obj = new BookLendingPostObject(student.getStudentKey(), books, student.isEvaluated());
            req.add(obj);
        }

        ApiInterface apiInterface = ApiClient.getApiInterface(getContext());
        BookLendingSubmitRequest bookLendingSubmitRequest = new BookLendingSubmitRequest(req);

        try {
            Response<Void> response = apiInterface.submitBookLendingSession(sessionKey, bookLendingSubmitRequest).execute();
            if (response.isSuccessful()) {
                Realm realm = Realm.getDefaultInstance();
                ReadSession readSession = realm.where(ReadSession.class).equalTo("key", sessionKey).findFirst();
                if (readSession != null) {
                    addComment(readSession.getNotes(),readSession.getKey());
                    realm.beginTransaction();
                    readSession.setStoredOffline(false);
                    realm.commitTransaction();
                    realm.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadUpcomingSessions(LoginResponse loginResponse, final List<ReadSession> readSessions) {

        try {
            Response<List<ReadSession>> response = ApiClient.getApiInterface(getContext()).
                    getSessionsByType(loginResponse.getNgoName(), loginResponse.getUserKey(), "UPCOMING").execute();
            if (response.isSuccessful()) {
                List<ReadSession> list = response.body();
                if (list != null) {
                    for (ReadSession readSession : list) {
                        if (!RealmHandler.isEditedReadSession(readSession.getKey()))
                            getStudentsList(readSession.getKey(), readSession.getSessionType());
                    }
                }

                Logger.d("upcoming sessions list " + list.size());
                readSessions.addAll(list);
                Logger.d("All sessions list " + readSessions.size());
                RealmHandler.updateReadSessionList(readSessions);

                ContentResolver contentResolver = getContext().getContentResolver();
                Uri uri = Uri.withAppendedPath(ReadApplication.BASE_URI, ReadApplication.UPCOMING_SESSIONS);
                contentResolver.notifyChange(uri, null);
                Logger.d("notifyChange UPCOMING_SESSIONS");

                uri = Uri.withAppendedPath(ReadApplication.BASE_URI, ReadApplication.PENDING_SESSIONS);
                contentResolver.notifyChange(uri, null);
                Logger.d("notifyChange PENDING_SESSIONS");

                uri = Uri.withAppendedPath(ReadApplication.BASE_URI, ReadApplication.EVALUATED_SESSIONS);
                contentResolver.notifyChange(uri, null);
                Logger.d("notifyChange EVALUATED_SESSIONS");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPendingSessions(LoginResponse loginResponse) {
        ApiInterface apiInterface = ApiClient.getApiInterface(getContext());

        try {
            Response<List<ReadSession>> response = apiInterface.getSessionsByType(loginResponse.getNgoName(),
                    loginResponse.getUserKey(), "PENDING").execute();
            if (response.isSuccessful()) {
                List<ReadSession> list = response.body();

//              TODO if readSession received from the server is verified or cancelled or deleted then overwrite the local copy)
//              TODO maintain partial student evaluation

                if (list != null) {
                    Logger.d("Pending Sessions size: " + list.size());
                    for (ReadSession readSession : list) {
                        if (!RealmHandler.isEditedReadSession(readSession.getKey())) {
                            getStudentsList(readSession.getKey(), readSession.getSessionType());
                        }
                    }
                    List<ReadSession> chainList;
                    chainList = new ArrayList<>(list);
                    loadEvaluatedSessions(loginResponse, chainList);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadEvaluatedSessions(final LoginResponse loginResponse, final List<ReadSession> readSessions) {
        ApiInterface apiInterface = ApiClient.getApiInterface(getContext());
        try {
            Response<List<ReadSession>> response = apiInterface.getSessionsByType(loginResponse.getNgoName(), loginResponse.getUserKey(),
                    "EVALUATED_NOT_VERIFIED").execute();
            if (response.isSuccessful()) {
                List<ReadSession> list = response.body();
                if (list != null) {
                    for (ReadSession readSession : list) {
                        if (!RealmHandler.isEditedReadSession(readSession.getKey())) {
                            getStudentsList(readSession.getKey(), readSession.getSessionType());
                        }
                    }
                }
                readSessions.addAll(list);
                List<ReadSession> chainList = new ArrayList<>(readSessions);
                loadUpcomingSessions(loginResponse, chainList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getStudentsList(final String sessionKey, final String sessionType) {

        Logger.d("getStudentsList sessionType " + sessionType);
        ApiInterface apiInterface = ApiClient.getApiInterface(getContext());
        try {
            Response<List<ReadSessionClassroomWithStudents>> response = apiInterface.getSessionClassroom(sessionKey,
                    RealmHandler.getLoginResponse().getNgoName()).execute();
            if (response.isSuccessful()) {
                List<ReadSessionClassroomWithStudents> list = response.body();
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        List<ReadStudent> students;
                        if (response.body() != null) {
                            students = response.body().get(i).getStudents();
                            getEvaluatedStudents(sessionKey, students, response.body().get(i).getClassroom().getKey(), sessionType);
                        }
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getEvaluatedStudents(final String sessionKey, final List<ReadStudent> students, final String classroomKey, String sessionType) {
        ApiInterface apiInterface = ApiClient.getApiInterface(getContext());
        Call<List<StudentEvaluation>> call;

        if (sessionType.equals(session_book_lending)) {
            call = apiInterface.getHomeLendingBooks(sessionKey);
        } else {
            call = apiInterface.getStudentEvaluationList(sessionKey);
        }
        try {
            Response<List<StudentEvaluation>> response = call.execute();
            if (response.isSuccessful()) {

                List<StudentEvaluation> list = response.body();
                List<StudentEvaluation> studentEvaluationList = new ArrayList<>();


                if (list != null) {
                    if (list.size() > 0) {
                        for (int i = 0; i < students.size(); i++) {
                            ReadStudent student = students.get(i);
                            StudentEvaluation evaluation = getEvaluatedStudent(student.getKey(), list);
                            if (evaluation != null) {
                                evaluation.setStudentKey(student.getKey());
                                evaluation.setSessionKey(sessionKey);
                                evaluation.setEvaluated(true);
                                evaluation.setLocalEvaluated(true);
                                studentEvaluationList.add(evaluation);
                            } else {

                                evaluation = new StudentEvaluation();
                                evaluation.setLocalEvaluated(false);
                                evaluation.setStudentKey(student.getKey());
                                evaluation.setStudent(student);
                                evaluation.setSessionKey(sessionKey);
                                evaluation.setAttendance(true);
                                studentEvaluationList.add(evaluation);
                            }
                        }
                    } else {
                        for (int i = 0; i < students.size(); i++) {
                            ReadStudent student = students.get(i);
                            StudentEvaluation obj = new StudentEvaluation();
                            obj.setStudentKey(student.getKey());
                            obj.setLocalEvaluated(false);
                            obj.setStudent(student);
                            obj.setAttendance(true);
                            obj.setSessionKey(sessionKey);
                            studentEvaluationList.add(obj);
                        }
                    }
                }
                ClassroomEvaluation classroom = new ClassroomEvaluation();
                classroom.setClassroomKey(classroomKey);
                classroom.setStudents(studentEvaluationList);
                classroom.setSessionKey(sessionKey);
                RealmHandler.updateEvaluationClassroom(classroom);
                Logger.d("size of evaluation students SessionDetails: " + studentEvaluationList.size());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private StudentEvaluation getEvaluatedStudent(String key, List<StudentEvaluation> list) {
        for (StudentEvaluation studentEvaluation : list) {
            if (studentEvaluation.getStudent().getKey().equals(key)) {
                return studentEvaluation;
            }
        }
        return null;
    }
}
