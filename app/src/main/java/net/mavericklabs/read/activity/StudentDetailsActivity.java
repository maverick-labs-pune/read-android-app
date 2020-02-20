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
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.mavericklabs.read.R;
import net.mavericklabs.read.adapter.SelectedBooksList;
import net.mavericklabs.read.adapter.SpinnerAdapter;
import net.mavericklabs.read.model.ClassroomEvaluation;
import net.mavericklabs.read.model.Level;
import net.mavericklabs.read.model.StudentBook;
import net.mavericklabs.read.model.ReadStudent;
import net.mavericklabs.read.model.StudentBookInventory;
import net.mavericklabs.read.model.StudentEvaluation;
import net.mavericklabs.read.model.StudentLevel;
import net.mavericklabs.read.realm.RealmHandler;
import net.mavericklabs.read.util.DisplayUtil;
import net.mavericklabs.read.util.Logger;
import net.mavericklabs.read.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

import static net.mavericklabs.read.realm.RealmHandler.getTranslation;
import static net.mavericklabs.read.util.Constants.en_INLocale;
import static net.mavericklabs.read.util.Constants.mr_INLocale;

public class StudentDetailsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private String studentKey, sessionKey, classroomKey, sessionType;
    private StudentEvaluation studentEvaluation;
    private StudentEvaluation copyStudentEvaluation;
    private TextView textBooks;
    private TextView textErrorLevel;
    private TextView textErrorComment;
    private ReadStudent student;
    private Spinner spinner;
    private CheckBox checkBox;
    private EditText editComments;
    private Button btnSave;
    private StudentLevel selectedLevel;
    private StudentLevel copyLevel;
    private RecyclerView booksRecyclerView;
    private static final int SCANNER_INTENT_CODE = 1009;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private Dialog dialog;
    boolean attendance;
    private CardView cardView;
    private TextView textLabelStudentName;
    private TextView textLabelLevel;
    private String locale;
    private TextView textPreviousLevel;
    private TextView textLabelPreviousLevel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);
        textErrorLevel = findViewById(R.id.text_error_level);
        textErrorComment = findViewById(R.id.text_error_comment);
        TextView textName = findViewById(R.id.text_student_name);
        spinner = findViewById(R.id.spinner);
        checkBox = findViewById(R.id.checkbox_attendance);
        editComments = findViewById(R.id.edit_comment);
        btnSave = findViewById(R.id.btn_save);
        textBooks = findViewById(R.id.text_books);
        textLabelLevel = findViewById(R.id.text_label_level);
        textLabelStudentName = findViewById(R.id.text_label_student_name);
        booksRecyclerView = findViewById(R.id.books_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        booksRecyclerView.setLayoutManager(layoutManager);
        cardView = findViewById(R.id.cardView);
        textPreviousLevel = findViewById(R.id.text_previous_level);
        textLabelPreviousLevel = findViewById(R.id.text_label_previous_level);
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
            sessionType = intent.getStringExtra("session_type");
            Logger.d("studentKey: " + studentKey + " sessionKey " + sessionKey + " classroomKey " + classroomKey);
            if (studentKey != null && sessionKey != null && classroomKey != null) {

                studentEvaluation = RealmHandler.getEvaluationStudent(classroomKey, studentKey, sessionKey);
                copyStudentEvaluation = RealmHandler.getEvaluationStudent(classroomKey, studentKey, sessionKey);
                if (studentEvaluation != null) {

                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (!b) {
                                cardView.setVisibility(View.GONE);
                                attendance = false;
                                editComments.setText("");
                                student.setInventory(new ArrayList<StudentBookInventory>());
                                SelectedBooksList adapter = new SelectedBooksList(StudentDetailsActivity.this, student.getInventory(), textBooks);
                                booksRecyclerView.setAdapter(adapter);
                                String booksCount = getTranslation(locale, "LABEL_BOOK") + " " + student.getInventory().size();
                                textBooks.setText(booksCount);
                                adapter.notifyDataSetChanged();
                                selectedLevel = null;
                                copyLevel = null;
                                setupSpinner();


                            } else {
                                cardView.setVisibility(View.VISIBLE);
                                attendance = true;
                            }
                        }
                    });
                    Logger.d("studentEvaluation not null");
                    String booksCount = getTranslation(locale, "LABEL_BOOK") + " " + studentEvaluation.getStudent().getInventory().size();
                    textBooks.setText(booksCount);

                    if (studentEvaluation.getStudent().getPreviousLevelKey()!=null){
                        textPreviousLevel.setText(RealmHandler.getLevelNameByKey(studentEvaluation.getStudent().getPreviousLevelKey(),locale));
                    }else{
                        //set text for previous level not available
                    }

                    if (studentEvaluation.isAttendance()) {
                        checkBox.setChecked(true);
                        attendance = true;
                        cardView.setVisibility(View.VISIBLE);
                    } else {

                        checkBox.setChecked(false);
                        attendance = false;
                        cardView.setVisibility(View.GONE);
                    }

                    if (studentEvaluation.getComment() != null) {
                        editComments.setText(studentEvaluation.getComment());
                    }

                    if (studentEvaluation.getLevel() != null) {

                        selectedLevel = studentEvaluation.getLevel();
                        copyLevel = studentEvaluation.getLevel();
                    }
                    student = studentEvaluation.getStudent();
                    String studentName = student.getFirstName();
                    if (student.getMiddleName() != null) {
                        studentName = studentName + " " + student.getMiddleName();
                    }
                    studentName = studentName + " " + student.getLastName();
                    textName.setText(studentName);
                    setupSpinner();
                    Logger.d("student name : " + studentEvaluation.getStudent().getFirstName());
                    btnSave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            saveData();
                        }
                    });

                    SelectedBooksList adapter = new SelectedBooksList(this, student.getInventory(), textBooks);
                    booksRecyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } else {
                    Logger.d("studentEvaluation null");
                }

            }
        }
        editComments.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                textErrorComment.setVisibility(View.GONE);
            }
        });
    }

    private void setTranslations() {
        locale = SharedPreferenceUtil.getLocale(getApplicationContext());
        textLabelStudentName.setText(getTranslation(locale, "LABEL_STUDENT_NAME"));
        checkBox.setText(getTranslation(locale, "LABEL_PRESENT"));
        textBooks.setText(getTranslation(locale, "LABEL_BOOK"));
        textLabelLevel.setText(getTranslation(locale, "LABEL_LEVEL"));
        editComments.setHint(getTranslation(locale, "LABEL_COMMENTS"));
        btnSave.setText(getTranslation(locale, "LABEL_SAVE"));
        textErrorComment.setText(getTranslation(locale, "NO_COMMENTS_ERROR"));
        textErrorLevel.setText(getTranslation(locale, "NO_LEVEL_ERROR"));
        setTitle(getTranslation(locale, "LABEL_STUDENT_DETAILS"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (dataHasNotChanged() && hasSameBooks())
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

    private boolean dataHasNotChanged() {

        boolean levelEqual;
        if (selectedLevel == null && copyLevel == null)
            levelEqual = true;
        else if ((selectedLevel != null && copyLevel == null) || (selectedLevel == null && copyLevel != null)) {
            levelEqual = false;
        } else levelEqual = selectedLevel.equals(copyLevel);

        boolean commentEqual;
        String comment = editComments.getText().toString().trim();
        if (copyStudentEvaluation.getComment() == null && comment.equals(""))
            commentEqual = true;
        else
            commentEqual = copyStudentEvaluation.getComment() != null && copyStudentEvaluation.getComment().equals(comment);

        if (levelEqual && copyStudentEvaluation.isAttendance() == attendance && commentEqual) {
            return true;
        } else {
            return false;
        }

    }

    public boolean hasSameBooks() {
        boolean matchingFlag = true;
        List<StudentBookInventory> list1 = studentEvaluation.getStudent().getInventory();
        List<StudentBookInventory> list2 = copyStudentEvaluation.getStudent().getInventory();
        if (list1.size() != list2.size())
            return false;

        for (StudentBookInventory obj : list1) {

            for (StudentBookInventory obj1 : list2) {
                if (!obj.getSerialNumber().equals(obj1.getSerialNumber())) {
                    matchingFlag = false;
                    break;
                }

            }
            if (!matchingFlag)
                return false;
        }
        return true;
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
            if (checkBox.isChecked()) {
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
            } else {
                Toast.makeText(this, getTranslation(locale, "STUDENT_MUST_BE_MARKED_PRESENT_BEFORE_ADDING_BOOK"), Toast.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
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
                addBook(studentBookInventory);
            } else {
                Toast.makeText(this, getTranslation(locale, "BOOK_NOT_FOUND"), Toast.LENGTH_LONG).show();
            }

        }
    }

    private boolean bookAlreadyAdded(String key) {
        List<StudentBookInventory> list = student.getInventory();
        for (StudentBookInventory obj : list) {
            if (key.equals(obj.getBook().getKey())) {
                return true;
            }
        }
        return false;
    }

    private void addBook(StudentBookInventory item) {

        if (dialog != null) {
            dialog.dismiss();
        }
        if (!bookAlreadyAdded(item.getBook().getKey())) {
            List<StudentBookInventory> inventoryList = student.getInventory();
            inventoryList.add(item);
            student.setInventory(inventoryList);
            SelectedBooksList adapter = new SelectedBooksList(StudentDetailsActivity.this, student.getInventory(), textBooks);
            booksRecyclerView.setAdapter(adapter);
            String bookCount = getTranslation(locale, "LABEL_BOOK") + " " + inventoryList.size();
            textBooks.setText(bookCount);
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, getTranslation(locale, "BOOK_ALREADY_ADDED"), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValid() {
        boolean val = true;
        String comment = editComments.getText().toString().trim();
        if (checkBox.isChecked() && ((studentEvaluation.getStudent().getInventory().size() == 0 && comment.equals("")))) {
            textErrorComment.setVisibility(View.VISIBLE);
            val = false;
        }

        if (checkBox.isChecked() && selectedLevel == null) {
            textErrorLevel.setVisibility(View.VISIBLE);
            val = false;
        }

        return val;
    }

    private void saveData() {
        if (isValid()) {
            if (checkBox.isChecked())
                studentEvaluation.setAttendance(true);
            else
                studentEvaluation.setAttendance(false);

            studentEvaluation.setComment(editComments.getText().toString().trim());
            studentEvaluation.setLevel(selectedLevel);
            studentEvaluation.setLocalEvaluated(true);

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
                            if (student.getInventory() != null) {

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
    }

    private void setupSpinner() {

        List<Level> levels = RealmHandler.getLevelBySessionType(sessionType);
        Logger.d("levels: " + levels.size());
        final SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.spinner_text_item, levels);
        adapter.setDropDownViewResource(R.layout.spinner_text_item);
//        adapter.addAll(levels);
//        String selectLevel = "--" + getTranslation(locale, "SELECT_LEVEL") + "--";
        adapter.add(new Level("--" + getTranslation(en_INLocale, "SELECT_LEVEL") + "--", "--" + getTranslation(mr_INLocale, "SELECT_LEVEL") + "--"));
        spinner.setAdapter(adapter);
        if (selectedLevel != null && selectedLevel.getKey() != null) {
            for (int i = 0; i < levels.size(); i++) {
                if (selectedLevel.getKey() != null && (selectedLevel.getKey().equals(levels.get(i).getKey()))) {
                    spinner.setSelection(i);
                    break;
                }
            }
        } else {
            spinner.setSelection(adapter.getCount());
        }


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Level level = (Level) spinner.getSelectedItem();
                if (i != adapter.getCount()) {
                    textErrorLevel.setVisibility(View.GONE);
                    selectedLevel = new StudentLevel();
                    selectedLevel.setKey(level.getKey());
                    selectedLevel.setKey(level.getKey());
                    selectedLevel.setRank(level.getRank());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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
    public boolean onQueryTextSubmit(String query) {
        closeKeyboard();
        Logger.d("onQueryTextSubmit " + query);
        if (checkBox.isChecked()) {
            List<StudentBookInventory> bookList = RealmHandler.getBooksListBySerialNumber(query);
            if (bookList != null) {
                Logger.d("books size " + bookList.size());
                if (bookList.size() > 0) {
                    dialog = new Dialog(this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.layout_search_dialog);
                    LinearLayout linearLayout = dialog.findViewById(R.id.linear_layout);
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
                    layoutParams.width = displayMetrics.widthPixels - (DisplayUtil.dpToPx(48, this));
                    linearLayout.setLayoutParams(layoutParams);
                    RecyclerView recyclerView = dialog.findViewById(R.id.recyclerview);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
                    recyclerView.setLayoutManager(layoutManager);
                    BookListAdapter adapter = new BookListAdapter(this, bookList);
                    recyclerView.setAdapter(adapter);
                    dialog.show();

                } else {
                    Toast.makeText(this, getTranslation(locale, "BOOK_NOT_FOUND"), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(this, getTranslation(locale, "STUDENT_MUST_BE_MARKED_PRESENT_BEFORE_ADDING_BOOK"), Toast.LENGTH_LONG).show();
        }

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Logger.d("onQueryTextChange " + newText);
        return true;
    }

    public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {

        Context context;
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
            StudentBookInventory obj = list.get(position);
            StudentBook studentBook = obj.getBook();
            holder.textName.setText(studentBook.getBookName());
            holder.textSerialNumber.setText(obj.getSerialNumber());
            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addBook(list.get(position));
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
