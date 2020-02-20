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

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.mavericklabs.read.R;
import net.mavericklabs.read.adapter.BookLendingAdapter;
import net.mavericklabs.read.model.ClassroomEvaluation;
import net.mavericklabs.read.model.ReadStudent;
import net.mavericklabs.read.model.StudentBook;
import net.mavericklabs.read.model.StudentBookInventory;
import net.mavericklabs.read.model.StudentEvaluation;
import net.mavericklabs.read.realm.RealmHandler;
import net.mavericklabs.read.util.DisplayUtil;
import net.mavericklabs.read.util.Logger;
import net.mavericklabs.read.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

import static net.mavericklabs.read.realm.RealmHandler.getTranslation;

public class BookLendingActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final int SCANNER_INTENT_CODE = 1009;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private String studentKey, sessionKey, classroomKey;
    private StudentEvaluation studentEvaluation;
    private StudentEvaluation copyStudentEvaluation;
    private ReadStudent student;
    private RecyclerView recyclerView;
    private Dialog booksListDialog;
    private Button btnSave;
    private String locale;
    private TextView textLabelStudentName;
    private TextView textBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_lending);
        TextView textName = findViewById(R.id.text_student_name);
        textLabelStudentName = findViewById(R.id.text_label_student_name);
        recyclerView = findViewById(R.id.books_recyclerview);
        textBooks = findViewById(R.id.text_books);
        btnSave = findViewById(R.id.btn_save);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        setTranslations();
        Intent intent = getIntent();
        if (intent != null) {
            studentKey = intent.getStringExtra("student_key");
            sessionKey = intent.getStringExtra("session_key");
            classroomKey = intent.getStringExtra("classroom_id");

            Logger.d("studentKey: " + studentKey + " sessionKey " + sessionKey + " classroomKey " + classroomKey);
            if (studentKey != null && sessionKey != null && classroomKey != null) {

                studentEvaluation = RealmHandler.getEvaluationStudent(classroomKey, studentKey, sessionKey);
                copyStudentEvaluation = RealmHandler.getEvaluationStudent(classroomKey, studentKey, sessionKey);
                if (studentEvaluation != null) {
                    student = studentEvaluation.getStudent();
                    String studentName = student.getFirstName();
                    if (student.getMiddleName() != null){
                        studentName = studentName + " " + student.getMiddleName();
                    }
                    studentName = studentName + " " + student.getLastName();
                    textName.setText(studentName);

                    BookLendingAdapter adapter = new BookLendingAdapter(this, student.getInventory(),locale);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(adapter);
                    recyclerView.setNestedScrollingEnabled(false);
                }
            }
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    private void setTranslations() {
        locale = SharedPreferenceUtil.getLocale(getApplicationContext());
        textLabelStudentName.setText(getTranslation(locale, "LABEL_STUDENT_NAME"));
        textBooks.setText(getTranslation(locale, "LABEL_BOOK"));
        btnSave.setText(getTranslation(locale, "LABEL_SAVE"));
        setTitle(getTranslation(locale, "LABEL_STUDENT_DETAILS"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getTranslation(locale, "SEARCH_BOOK"));
        searchView.setOnQueryTextListener(this);
        searchView.setIconified(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.scan) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, ScannerBarcodeActivity.class);
                    startActivityForResult(intent, SCANNER_INTENT_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, new
                            String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }

            } else {
                Intent intent = new Intent(this, ScannerBarcodeActivity.class);
                startActivityForResult(intent, SCANNER_INTENT_CODE);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean inventoryAlreadyAdded(String serialNumber) {
        List<StudentBookInventory> list = student.getInventory();
        for (StudentBookInventory obj : list) {
            if (serialNumber.equals(obj.getSerialNumber())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        closeKeyboard();
        Logger.d("onQueryTextSubmit " + query);

        List<StudentBookInventory> bookList = RealmHandler.getBooksListBySerialNumber(query);
        if (bookList != null) {
            Logger.d("books size " + bookList.size());
            if (bookList.size() > 0) {
                booksListDialog = new Dialog(this);
                booksListDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                booksListDialog.setContentView(R.layout.layout_search_dialog);
                LinearLayout linearLayout = booksListDialog.findViewById(R.id.linear_layout);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
                layoutParams.width = displayMetrics.widthPixels - (DisplayUtil.dpToPx(48, this));
                linearLayout.setLayoutParams(layoutParams);
                RecyclerView recyclerView = booksListDialog.findViewById(R.id.recyclerview);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
                recyclerView.setLayoutManager(layoutManager);
                BookListAdapter adapter = new BookListAdapter(this, bookList);
                recyclerView.setAdapter(adapter);
                booksListDialog.show();

            } else {
                Toast.makeText(this, getTranslation(locale, "BOOK_NOT_FOUND"), Toast.LENGTH_LONG).show();
            }
        }


        return true;
    }

    private void saveData() {

        ClassroomEvaluation classroom = RealmHandler.getClassRoomEvaluation(classroomKey, sessionKey);
        if (classroom != null) {
            List<StudentEvaluation> list = new ArrayList<>();

            for (StudentEvaluation obj : classroom.getStudents()) {
                if (obj.getStudentKey().equals(studentKey)) {
                    list.add(studentEvaluation);

                } else {
                    list.add(obj);
                }
            }

            Logger.d("Students size " + list.size());
            classroom.setStudents(list);
            Logger.d("Students size " + list.size());
            Realm realm = Realm.getDefaultInstance();

            ClassroomEvaluation classObj = realm.where(ClassroomEvaluation.class).equalTo("classroomKey", classroomKey)
                    .equalTo("sessionKey", sessionKey).findFirst();
            if (classObj != null) {
                RealmList<StudentEvaluation> sList = classObj.getStudents();
                for (StudentEvaluation studentEvaluation : sList) {
                    if (studentEvaluation.getLevel() != null) {
                        realm.beginTransaction();
                        studentEvaluation.getLevel().deleteFromRealm();
                        realm.commitTransaction();
                    }

                    if (studentEvaluation.getStudent() != null) {
                        ReadStudent student = studentEvaluation.getStudent();
                        RealmList<StudentBookInventory> inventory = student.getInventory();
                        if (inventory != null) {
                            for (StudentBookInventory studentBookInventory : inventory) {
                                realm.beginTransaction();
                                studentBookInventory.getBook().deleteFromRealm();
                                realm.commitTransaction();
                            }
                            realm.beginTransaction();
                            inventory.deleteAllFromRealm();
                            realm.commitTransaction();

                        }

                        realm.beginTransaction();
                        student.deleteFromRealm();
                        realm.commitTransaction();
                    }

                }
                realm.beginTransaction();
                sList.deleteAllFromRealm();
                realm.commitTransaction();
                realm.beginTransaction();
                classObj.deleteFromRealm();
                realm.commitTransaction();
            }

            realm.beginTransaction();
            realm.copyToRealm(classroom);
            realm.commitTransaction();
            realm.close();
            setResult(RESULT_OK);
            finish();
        }

    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isSameData())
            super.onBackPressed();
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getTranslation(locale, "DATA_NOT_SAVED"));
            builder.setMessage(getTranslation(locale, "DISCARD_ALL_CHANGES"));
            builder.setCancelable(false);
            builder.setPositiveButton(getTranslation(locale, "YES"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setNegativeButton(getTranslation(locale, "NO"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
            Logger.d("data is changed");
        }
    }

    private boolean isSameData() {
        List<StudentBookInventory> list1 = studentEvaluation.getStudent().getInventory();
        List<StudentBookInventory> list2 = copyStudentEvaluation.getStudent().getInventory();
        if (list1.size() != list2.size())
            return false;

        for (StudentBookInventory obj1 : list1) {
            boolean found = false;
            for (StudentBookInventory obj2 : list2) {
                if (obj1.getSerialNumber().equals(obj2.getSerialNumber()) && obj1.getAction().equals(obj2.getAction())) {
                    found = true;
                    break;
                }
            }
            if (!found)
                return false;

        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(this, ScannerBarcodeActivity.class);
                startActivityForResult(intent, SCANNER_INTENT_CODE);

            } else {
                Toast.makeText(this, getTranslation(locale, "CAMERA_PERMISSION_REQUIRED"), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCANNER_INTENT_CODE && resultCode == RESULT_OK) {
            String code = data.getStringExtra("scanned_code");
            StudentBookInventory studentBookInventory = RealmHandler.getBookBySerialNumber(code);
            if (studentBookInventory != null) {
                Logger.d("Scanned Code : " + code + " book name " + studentBookInventory.getBook().getBookName());
                showLendOrCollectDialog(studentBookInventory);

            } else {
                Toast.makeText(this, getTranslation(locale, "BOOK_NOT_FOUND"), Toast.LENGTH_LONG).show();
            }

        }
    }

    private void showLendOrCollectDialog(final StudentBookInventory item) {
        if (booksListDialog != null) {
            booksListDialog.dismiss();
        }
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_book_lending_dialog);
        LinearLayout linearLayout = dialog.findViewById(R.id.linear_layout);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
        layoutParams.width = displayMetrics.widthPixels - (DisplayUtil.dpToPx(48, this));
        linearLayout.setLayoutParams(layoutParams);

        TextView textBookName = dialog.findViewById(R.id.text_book_name);
        textBookName.setText(item.getBook().getBookName());
        TextView textSerialNumber = dialog.findViewById(R.id.text_serial_number);
        textSerialNumber.setText(item.getSerialNumber());

        Button btnLend = dialog.findViewById(R.id.btn_lend);
        btnLend.setText(getTranslation(locale,"LABEL_LEND"));
        Button btnCollect = dialog.findViewById(R.id.btn_collect);
        btnCollect.setText(getTranslation(locale,"LABEL_COLLECT"));

        btnLend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item.setAction("le");
                addBook(item);
                dialog.dismiss();
            }
        });

        btnCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item.setAction("co");
                addBook(item);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void addBook(StudentBookInventory item) {
        if (!inventoryAlreadyAdded(item.getSerialNumber())) {
            List<StudentBookInventory> inventoryList = student.getInventory();
            inventoryList.add(item);

            student.setInventory(inventoryList);
            BookLendingAdapter adapter = new BookLendingAdapter(this, student.getInventory(),locale);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, getTranslation(locale, "BOOK_ALREADY_ADDED"), Toast.LENGTH_SHORT).show();
        }


    }


    private class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {

        final Context context;
        List<StudentBookInventory> list;

        BookListAdapter(Context context, List<StudentBookInventory> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public BookListAdapter.BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_search_list, parent, false);
            return new BookListAdapter.BookViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BookListAdapter.BookViewHolder holder, final int position) {
            final StudentBookInventory obj = list.get(position);
            StudentBook studentBook = obj.getBook();
            holder.textName.setText(studentBook.getBookName());
            holder.textSerialNumber.setText(obj.getSerialNumber());
            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showLendOrCollectDialog(obj);

                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class BookViewHolder extends RecyclerView.ViewHolder {
            private TextView textName;
            private RelativeLayout relativeLayout;
            private TextView textSerialNumber;

            BookViewHolder(View itemView) {
                super(itemView);
                textName = itemView.findViewById(R.id.text_search_item_name);
                relativeLayout = itemView.findViewById(R.id.relativelayout);
                textSerialNumber = itemView.findViewById(R.id.text_search_item_serial_number);

            }
        }
    }
}
