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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import net.mavericklabs.read.R;
import net.mavericklabs.read.adapter.SessionDetailsAdapter;
import net.mavericklabs.read.adapter.StringAdapter;
import net.mavericklabs.read.model.ReadSessionBookFairy;
import net.mavericklabs.read.retrofit.custom.BookLendingPostObject;
import net.mavericklabs.read.retrofit.custom.BookLendingSubmitRequest;
import net.mavericklabs.read.retrofit.custom.InventoryRequest;
import net.mavericklabs.read.model.ReadSession;
import net.mavericklabs.read.model.StudentBookInventory;
import net.mavericklabs.read.model.StudentEvaluation;
import net.mavericklabs.read.retrofit.custom.StudentEvaluationPost;
import net.mavericklabs.read.retrofit.custom.StudentEvaluationRequest;
import net.mavericklabs.read.model.StudentLevel;
import net.mavericklabs.read.realm.RealmHandler;
import net.mavericklabs.read.retrofit.ApiClient;
import net.mavericklabs.read.retrofit.ApiInterface;
import net.mavericklabs.read.util.DateUtil;
import net.mavericklabs.read.util.DisplayUtil;
import net.mavericklabs.read.util.Logger;
import net.mavericklabs.read.util.NetworkConnection;
import net.mavericklabs.read.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.mavericklabs.read.realm.RealmHandler.getTranslation;
import static net.mavericklabs.read.util.Constants.en_INLocale;
import static net.mavericklabs.read.util.Constants.mr_INLocale;
import static net.mavericklabs.read.util.Constants.session_book_lending;
import static net.mavericklabs.read.util.Constants.student_details_request_code;
import static net.mavericklabs.read.util.Constants.text_bold;

public class SessionDetailsActivity extends AppCompatActivity {

    private String sessionKey;
    private ReadSession session;
    private TextView textSendReport;
    private SessionDetailsAdapter adapter;
    private TextView textViewLabelSchool;
    private TextView textViewReadSessionType;
    private TextView textViewBookFairy;
    private TextView textViewEvaluatedByBookFairy;
    private TextView textViewVerifiedBySupervisor;
    private TextView textNotes, textLabelNotes;
    private String locale;
    private ImageView imageEditNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        RecyclerView recyclerViewBookFairies = findViewById(R.id.recycler_view_book_fairy);
        TextView textSchoolName = findViewById(R.id.text_school_name);
        TextView textSessionType = findViewById(R.id.text_session_type);
        textSendReport = findViewById(R.id.text_send_report);
        ImageView imageEvaluated = findViewById(R.id.image_session_evaluated);
        ImageView imageVerified = findViewById(R.id.image_session_verified);
        TextView textDate = findViewById(R.id.text_date);
        textViewLabelSchool = findViewById(R.id.text_label_school);
        textViewReadSessionType = findViewById(R.id.text_label_session_type);
        textViewBookFairy = findViewById(R.id.text_label_book_fairy);
        textViewEvaluatedByBookFairy = findViewById(R.id.text_label_evaluated_by_book_fairy);
        textViewVerifiedBySupervisor = findViewById(R.id.text_label_verified_by_supervisor);
        textNotes = findViewById(R.id.text_notes);
        textLabelNotes = findViewById(R.id.text_label_notes);
        imageEditNotes = findViewById(R.id.image_edit_notes);
        setTranslations();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        sessionKey = getIntent().getStringExtra("session_key");
        if (sessionKey != null) {
            session = RealmHandler.getReadSessionById(sessionKey);
            if (session != null) {

                List<ReadSessionBookFairy> bookFairies = session.getBookFairySet();
                List<String> listStringBookFairies = new ArrayList<>();
                for (ReadSessionBookFairy readSessionBookFairy : bookFairies) {
                    String firstName = readSessionBookFairy.getBookFairy().getFirstName();
                    String lastName = readSessionBookFairy.getBookFairy().getLastName();
                    listStringBookFairies.add(firstName + " " + lastName);
                }

                recyclerViewBookFairies.setLayoutManager(new LinearLayoutManager(this));
                StringAdapter stringAdapter = new StringAdapter(this, listStringBookFairies, text_bold);
                recyclerViewBookFairies.setAdapter(stringAdapter);
                recyclerViewBookFairies.setNestedScrollingEnabled(false);

                textNotes.setText(session.getNotes());

                String locale = SharedPreferenceUtil.getLocale(getApplicationContext());
                textSessionType.setText(getTranslation(locale, session.getSessionType()));
                String school = session.getReadSessionClassroom().get(0).getClassroom().getSchool().getSchoolName();
                textSchoolName.setText(school);
                if (session.isEvaluated())
                    imageEvaluated.setImageDrawable(getDrawable(R.drawable.green_check));
                else
                    imageEvaluated.setImageDrawable(getDrawable(R.drawable.ic_remove));

                if (session.isVerified())
                    imageVerified.setImageDrawable(getDrawable(R.drawable.green_check));
                else
                    imageVerified.setImageDrawable(getDrawable(R.drawable.ic_remove));
                textDate.setText(DateUtil.formatDate(session.getStartDateTime(), session.getEndDateTime()));
                adapter = new SessionDetailsAdapter(this, session.getSessionType(), session.getReadSessionClassroom(), locale);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(adapter);
                recyclerView.setNestedScrollingEnabled(false);
            }

        }

        imageEditNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(SessionDetailsActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.layout_notes_dialog);
                LinearLayout linearLayout = dialog.findViewById(R.id.linear_layout);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
                layoutParams.width = displayMetrics.widthPixels - (DisplayUtil.dpToPx(48, SessionDetailsActivity.this));
                linearLayout.setLayoutParams(layoutParams);
                dialog.show();
                final String locale = SharedPreferenceUtil.getLocale(getApplicationContext());
                final EditText editText = dialog.findViewById(R.id.edit_notes);
                editText.setText(session.getNotes());
                editText.setHint(getTranslation(locale, "LABEL_COMMENTS"));
                Button btn = dialog.findViewById(R.id.btn_ok);
                btn.setText(getTranslationFromXML(locale, "LABEL_OK"));
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String strComment = editText.getText().toString().trim();
                        Realm realm = Realm.getDefaultInstance();
                        ReadSession readSession = realm.where(ReadSession.class).equalTo("key", sessionKey).findFirst();
                        if (readSession != null) {
                            realm.beginTransaction();
                            readSession.setNotes(strComment);
                            realm.commitTransaction();
                            textNotes.setText(strComment);
                            session.setNotes(strComment);
                        }
                        realm.close();
                        dialog.dismiss();


                    }
                });
            }
        });

        textSendReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (session.getSessionType().equals(session_book_lending)) {
                    List<StudentEvaluation> list = RealmHandler.getEvaluatedStudents(session.getKey());
                    if (NetworkConnection.isNetworkAvailable(SessionDetailsActivity.this)) {
                        if (list != null) {
                            submitBookLendingSession(list);
                        }
                    } else {
                        Realm realm = Realm.getDefaultInstance();
                        ReadSession readSession = realm.where(ReadSession.class).equalTo("key", session.getKey()).findFirst();
                        if (readSession != null) {
                            realm.beginTransaction();
                            readSession.setStoredOffline(true);
                            realm.commitTransaction();
                        }
                        realm.close();
                        Toast.makeText(SessionDetailsActivity.this, getTranslation(locale, "NO_NETWORK_DATA_STORED_OFFLINE"), Toast.LENGTH_LONG).show();
                    }

                } else {
                    List<StudentEvaluation> list = RealmHandler.getEvaluatedStudents(session.getKey());
                    if (list != null) {
                        Logger.d("getEvaluatedStudents " + list.size());
                        if (isDataValid(list)) {
                            if (NetworkConnection.isNetworkAvailable(SessionDetailsActivity.this)) {
                                sendReport(list);
                            } else {
                                Realm realm = Realm.getDefaultInstance();
                                ReadSession readSession = realm.where(ReadSession.class).equalTo("key", session.getKey()).findFirst();
                                if (readSession != null) {
                                    realm.beginTransaction();
                                    readSession.setStoredOffline(true);
                                    realm.commitTransaction();
                                }
                                realm.close();
                                Toast.makeText(SessionDetailsActivity.this, getTranslation(locale, "NO_NETWORK_DATA_STORED_OFFLINE"), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(SessionDetailsActivity.this, getTranslation(locale, "ALL_STUDENTS_NOT_EVALUATED_ERROR"), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
    }

    private String getTranslationFromXML(String locale, String key) {
        if (locale == null) {
            locale = en_INLocale;
        }
        switch (locale) {
            case en_INLocale:
                switch (key) {
                    case "LABEL_OK":
                        return getString(R.string.LABEL_OK_EN);
                }
                break;
            case mr_INLocale:
                switch (key) {

                    case "LABEL_OK":
                        return getString(R.string.LABEL_OK_MR);
                }
                break;
        }
        return "";
    }


    private void setTranslations() {
        locale = SharedPreferenceUtil.getLocale(getApplicationContext());
        textViewLabelSchool.setText(getTranslation(locale, "LABEL_SCHOOL"));
        textViewReadSessionType.setText(getTranslation(locale, "FIELD_LABEL_READ_SESSION_TYPE"));
        textViewBookFairy.setText(getTranslation(locale, "LABEL_BOOK_FAIRY"));
        textViewEvaluatedByBookFairy.setText(getTranslation(locale, "LABEL_EVALUATED_BY_BOOK_FAIRY"));
        textViewVerifiedBySupervisor.setText(getTranslation(locale, "LABEL_VERIFIED_BY_SUPERVISOR"));
        textSendReport.setText(getTranslation(locale, "LABEL_SUBMIT"));
        textLabelNotes.setText(getTranslation(locale, "LABEL_COMMENTS"));
        setTitle(getTranslation(locale, "LABEL_SESSION_DETAILS"));
    }

    private void submitBookLendingSession(List<StudentEvaluation> list) {
        List<BookLendingPostObject> req = new ArrayList<>();
        for (StudentEvaluation student : list) {
            List<InventoryRequest> books = new ArrayList<>();
            for (StudentBookInventory inventory : student.getStudent().getInventory()) {
                books.add(new InventoryRequest(inventory.getKey(), inventory.getBook().getKey(), inventory.getAction()));
            }
            BookLendingPostObject obj = new BookLendingPostObject(student.getStudentKey(), books, student.isEvaluated());
            req.add(obj);
        }

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
        ApiInterface apiInterface = ApiClient.getApiInterface(getApplicationContext());
        BookLendingSubmitRequest bookLendingSubmitRequest = new BookLendingSubmitRequest(req);
        apiInterface.submitBookLendingSession(session.getKey(), bookLendingSubmitRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Logger.d("onResponse");
                addComment(progress);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Logger.d("onFailure");
//                TODO Show server error
                Toast.makeText(SessionDetailsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                progress.dismiss();
            }
        });
    }

    private void addComment(final ProgressDialog progress) {
        String notes = session.getNotes();
        if (notes != null && !notes.trim().equals("")) {
            ApiClient.getApiInterface(getApplicationContext()).addCommentToSession(sessionKey, session.getNotes()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    progress.dismiss();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {

                }
            });
        } else {
            progress.dismiss();
            setResult(RESULT_OK);
            finish();
        }

    }

    private void sendReport(List<StudentEvaluation> list) {
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

        ApiInterface apiInterface = ApiClient.getApiInterface(getApplicationContext());
        StudentEvaluationRequest studentEvaluationRequest = new StudentEvaluationRequest(req);
        Gson gson = new Gson();
        String json = gson.toJson(studentEvaluationRequest);

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
        apiInterface.submitSession(session.getKey(), studentEvaluationRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                Logger.d("onResponse");
                addComment(progress);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

                progress.dismiss();
                Logger.d("onFailure");
            }
        });
    }


    private boolean isDataValid(List<StudentEvaluation> list) {
        for (StudentEvaluation student : list) {
            if (!student.isLocalEvaluated())
                return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == student_details_request_code && resultCode == RESULT_OK) {
            session = RealmHandler.getReadSessionById(sessionKey);
            if (session != null) {
                adapter.setList(session.getReadSessionClassroom());
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
