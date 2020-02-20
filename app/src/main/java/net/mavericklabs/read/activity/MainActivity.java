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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import net.mavericklabs.read.R;
import net.mavericklabs.read.fragment.AboutFragment;
import net.mavericklabs.read.fragment.BooksFragment;
import net.mavericklabs.read.fragment.ChangeLanguageFragment;
import net.mavericklabs.read.fragment.HomeFragment;
import net.mavericklabs.read.fragment.ResetPasswordFragment;
import net.mavericklabs.read.model.Inventory;
import net.mavericklabs.read.model.Level;
import net.mavericklabs.read.model.LoginResponse;
import net.mavericklabs.read.model.ReadSession;
import net.mavericklabs.read.model.StudentBook;
import net.mavericklabs.read.model.StudentBookInventory;
import net.mavericklabs.read.realm.RealmHandler;
import net.mavericklabs.read.retrofit.ApiClient;
import net.mavericklabs.read.sync.SyncAdapter;
import net.mavericklabs.read.util.DisplayUtil;
import net.mavericklabs.read.util.Logger;
import net.mavericklabs.read.util.NetworkConnection;
import net.mavericklabs.read.util.SharedPreferenceUtil;

import java.util.List;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.mavericklabs.read.realm.RealmHandler.getTranslation;
import static net.mavericklabs.read.util.Constants.session_details_request_code;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ChangeLanguageListener, SearchView.OnQueryTextListener {
    private NavigationView navigationView;
    private DrawerLayout drawer;
    boolean doubleBackToExitPressedOnce = false;
    private String locale;
    private TextView labelBookFairyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Realm realm = Realm.getDefaultInstance();
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        locale = SharedPreferenceUtil.getLocale(getApplicationContext());
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView textBookFairyName = headerView.findViewById(R.id.text_header_book_fairy_name);
        labelBookFairyName = headerView.findViewById(R.id.label_book_fairy_name);
        selectDrawerItem(navigationView.getMenu().findItem(R.id.nav_home));

        LoginResponse loginResponse = RealmHandler.getLoginResponse();
        if (loginResponse != null) {
            languageChanged();
            String name = loginResponse.getFirstName() + " " + loginResponse.getLastName();
            textBookFairyName.setText(name);
            if (NetworkConnection.isNetworkAvailable(this)) {
                List<Inventory> list = realm.where(Inventory.class).findAll();
                if (list == null || list.size() == 0) {
                    boolean result = SyncAdapter.requestSync(this, SyncAdapter.SYNC_EVERYTHING);
                    if (!result) {
                        Toast.makeText(this, getTranslation(locale, "SYNC_ALREADY_RUNNING"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, getTranslation(locale, "SYNC_STARTED"), Toast.LENGTH_SHORT).show();
                    }
                }

                List<Level> levelList = realm.where(Level.class).findAll();
                if (levelList == null || levelList.size() == 0) {
                    getLevels();
                }
            } else {
                Toast.makeText(this, getTranslation(locale, "NO_NETWORK"), Toast.LENGTH_SHORT).show();
            }
        }
        realm.close();

    }


    private void getLevels() {
        ApiClient.getApiInterface(getApplicationContext()).getLevels(RealmHandler.getLoginResponse().getNgoName()).enqueue(new Callback<List<Level>>() {
            @Override
            public void onResponse(Call<List<Level>> call, Response<List<Level>> response) {
                if (response.isSuccessful()) {
                    List<Level> levelList = response.body();
                    RealmHandler.updateLevels(levelList);
                }
            }

            @Override
            public void onFailure(Call<List<Level>> call, Throwable t) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                finish();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getTranslation(locale, "LABEL_EXIT_APP"), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        selectDrawerItem(item);
        return true;
    }

    private void selectDrawerItem(MenuItem item) {
        if (item.isChecked() && item.getItemId() != R.id.nav_language) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        String locale = SharedPreferenceUtil.getLocale(getApplicationContext());
        Class fragmentClass = null;
        Fragment fragment = null;
        FragmentTransaction fragmentTransaction;
        String title = "";
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                fragmentClass = HomeFragment.class;
                title = getTranslation(locale, "SESSION_FRAGMENT_TITLE");
                break;
            case R.id.nav_books:
                fragmentClass = BooksFragment.class;
                title = getTranslation(locale, "BOOK_FRAGMENT_TITLE");
                break;
            case R.id.nav_change_password:
                fragmentClass = ResetPasswordFragment.class;
                title = getTranslation(locale, "RESET_PASSWORD_TITLE");
                break;
            case R.id.nav_language:
                fragmentClass = ChangeLanguageFragment.class;
                title = getTranslation(locale, "CHANGE_LANGUAGE_TITLE");
                break;
            case R.id.nav_logout:
                List<ReadSession> sessionList = RealmHandler.getAllEditedReadSessions();

                boolean isSyncActive = SyncAdapter.isSyncActive(getApplicationContext());
                if (isSyncActive) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("LABEL_LOGOUT_ERROR");
                    builder.setMessage("SYNC_ALREADY_RUNNING");
                    builder.setCancelable(false);
                    builder.setNeutralButton("LABEL_OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.show();
                    return;
                }


                if (sessionList != null && sessionList.size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("LABEL_LOGOUT_ERROR");
                    builder.setMessage("LABEL_LOGOUT_SAVE_MESSAGE");
                    builder.setCancelable(false);
                    builder.setNeutralButton("LABEL_OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    builder.show();
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getTranslation(locale, "LABEL_LOGOUT"));
                    builder.setMessage(getTranslation(locale, "DISCARD_ALL_CHANGES"));
                    builder.setCancelable(false);
                    builder.setPositiveButton(getTranslation(locale, "YES"), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logout();
                        }
                    });

                    builder.setNegativeButton(getTranslation(locale, "NO"), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    builder.show();

                }
            case R.id.nav_about:
                fragmentClass = AboutFragment.class;
                title = getTranslation(locale, "ABOUT_TITLE");
                break;
            default:
                break;
        }

        if (fragmentClass != null) {
            item.setChecked(true);
            setTitle(title);
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            boolean fragmentPopped = fragmentManager.popBackStackImmediate(title, 0);
            if (!fragmentPopped) {
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.content, fragment, title);
                fragmentTransaction.addToBackStack(title);
                fragmentTransaction.commit();
            }

            fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    Fragment f = getSupportFragmentManager().findFragmentById(R.id.content);
                    Logger.d(" count onBackStackChanged" + getSupportFragmentManager().getBackStackEntryCount());
                    updateTitleAndDrawer(f);
                }
            });
        }
        drawer.closeDrawer(GravityCompat.START);
    }

    private void logout() {
        RealmHandler.clearRealmDatabase();
        SharedPreferenceUtil.clearSharedPrefs(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void updateTitleAndDrawer(Fragment fragment) {
        String locale = SharedPreferenceUtil.getLocale(getApplicationContext());
        String name = fragment.getClass().getName();
        String title = "";
        int id = R.id.nav_home;
        if (name.equals(HomeFragment.class.getName())) {
            title = getTranslation(locale, "SESSION_FRAGMENT_TITLE");
            id = R.id.nav_home;
        } else if (name.equals(BooksFragment.class.getName())) {
            title = getTranslation(locale, "BOOK_FRAGMENT_TITLE");
            id = R.id.nav_books;
        } else if (name.equals(ResetPasswordFragment.class.getName())) {
            title = getTranslation(locale, "RESET_PASSWORD_TITLE");
            id = R.id.nav_change_password;
        } else if (name.equals(ChangeLanguageFragment.class.getName())) {
            title = getTranslation(locale, "CHANGE_LANGUAGE_TITLE");
            id = R.id.nav_language;
        }
        setTitle(title);
        navigationView.setCheckedItem(id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == session_details_request_code && resultCode == RESULT_OK) {
            if (NetworkConnection.isNetworkAvailable(this)) {
                boolean result = SyncAdapter.requestSync(this, SyncAdapter.SYNC_SESSIONS);
                if (!result) {
                    Toast.makeText(this, getTranslation(locale, "SYNC_ALREADY_RUNNING"), Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, getTranslation(locale, "SYNC_STARTED"), Toast.LENGTH_SHORT).show();

                }
            } else {
                Toast.makeText(this, getTranslation(locale, "NO_NETWORK"), Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void languageChanged() {
        Menu menu = navigationView.getMenu();
        locale = SharedPreferenceUtil.getLocale(getApplicationContext());
        labelBookFairyName.setText(getTranslation(locale,"LABEL_BOOK_FAIRY"));
        MenuItem item = menu.findItem(R.id.nav_home);
        item.setTitle(getTranslation(locale, "SESSION_FRAGMENT_TITLE"));
        Logger.d("changed session title");
        item = menu.findItem(R.id.nav_books);
        item.setTitle(getTranslation(locale, "BOOK_FRAGMENT_TITLE"));
        Logger.d("changed session title");
        item = menu.findItem(R.id.nav_change_password);
        item.setTitle(getTranslation(locale, "RESET_PASSWORD_TITLE"));
        Logger.d("changed session title");
        item = menu.findItem(R.id.nav_language);
        item.setTitle(getTranslation(locale, "CHANGE_LANGUAGE_TITLE"));
        Logger.d("changed session title");
        item = menu.findItem(R.id.nav_logout);
        item.setTitle(getTranslation(locale, "LABEL_LOGOUT"));
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint(getTranslation(locale, "SEARCH_BOOK"));
        searchView.setIconified(false);
        MenuItem scanItem = menu.findItem(R.id.scan);
        scanItem.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Logger.d("onQueryTextSubmit " + query);
        List<StudentBookInventory> bookList = RealmHandler.getBooksListBySerialNumber(query);
        if (bookList != null) {
            Logger.d("books size " + bookList.size());
            if (bookList.size() > 0) {


                Dialog dialog = new Dialog(this);
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


        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {

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
