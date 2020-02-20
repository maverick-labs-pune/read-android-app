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

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.mavericklabs.read.R;
import net.mavericklabs.read.model.LoginResponse;
import net.mavericklabs.read.model.RealmTranslation;
import net.mavericklabs.read.model.Translation;
import net.mavericklabs.read.retrofit.ApiClient;
import net.mavericklabs.read.retrofit.ApiInterface;
import net.mavericklabs.read.retrofit.custom.LoginRequest;
import net.mavericklabs.read.util.Constants;
import net.mavericklabs.read.util.DisplayUtil;
import net.mavericklabs.read.util.Logger;
import net.mavericklabs.read.util.NetworkConnection;
import net.mavericklabs.read.util.SharedPreferenceUtil;

import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.mavericklabs.read.util.Constants.en_INLocale;
import static net.mavericklabs.read.util.Constants.mr_INLocale;

public class LoginActivity extends AppCompatActivity {

    private EditText editUsername, editPassword;
    private Context context = LoginActivity.this;
    private TextView textForgotPassword;
    private TextView btnLogin;
    private TextView btnChangeLanguage;
    private boolean userNameHasFocus = false;
    private boolean passwordHasFocus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);
        textForgotPassword = findViewById(R.id.text_forgot_password);
        btnChangeLanguage = findViewById(R.id.btn_change_language);
        setTranslations();

        editUsername.setOnFocusChangeListener(onFocusChangeListener);
        editPassword.setOnFocusChangeListener(onFocusChangeListener);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        btnChangeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String locale = SharedPreferenceUtil.getLocale(getApplicationContext());
                if (locale == null) {
                    locale = en_INLocale;
                }
                if (locale.equals(en_INLocale)) {
                    SharedPreferenceUtil.setStringPreference(getApplicationContext(), "locale", mr_INLocale);
                } else if (locale.equals(mr_INLocale)) {
                    SharedPreferenceUtil.setStringPreference(getApplicationContext(), "locale", en_INLocale);
                }
                setTranslations();
            }
        });

        editPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent != null && keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                } else if (i == EditorInfo.IME_ACTION_GO || keyEvent == null || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    login();
                }
                return true;
            }
        });

        textForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.layout_forgot_password_dialog);
                LinearLayout linearLayout = dialog.findViewById(R.id.linear_layout);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
                layoutParams.width = displayMetrics.widthPixels - (DisplayUtil.dpToPx(48, context));
                linearLayout.setLayoutParams(layoutParams);
                dialog.show();
                final String locale = SharedPreferenceUtil.getLocale(getApplicationContext());
                final EditText editText = dialog.findViewById(R.id.edit_forgot_username);
                final TextView textViewLabel = dialog.findViewById(R.id.text_label_email);
                editText.setHint(getTranslationFromXML(locale, "LABEL_USER_USERNAME"));
                textViewLabel.setText(getTranslationFromXML(locale, "LABEL_FORGOT_PASSWORD_EMAIL"));
                Button btn = dialog.findViewById(R.id.btn_ok);
                btn.setText(getTranslationFromXML(locale, "LABEL_OK"));
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String userName = editText.getText().toString().trim();
                        if (!userName.equals("")) {
                            dialog.dismiss();
                            if(NetworkConnection.isNetworkAvailable(getApplicationContext())){
                                sendForgotPasswordRequest(userName);
                            }else{
                                Toast.makeText(getApplicationContext(),
                                        getTranslationFromXML(locale,"NO_NETWORK"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }

    private void setTranslations() {
        String locale = SharedPreferenceUtil.getLocale(getApplicationContext());
        editUsername.setHint(getTranslationFromXML(locale, "LABEL_USER_USERNAME"));
        editPassword.setHint(getTranslationFromXML(locale, "LABEL_USER_PASSWORD"));
        textForgotPassword.setText(getTranslationFromXML(locale, "LABEL_FORGOT_PASSWORD"));
        btnLogin.setText(getTranslationFromXML(locale, "LABEL_LOGIN"));
        btnChangeLanguage.setText(getTranslationFromXML(locale, "LABEL_CHANGE_LANGUAGE"));
        setTitle(getTranslationFromXML(locale, "LABEL_APP_TITLE"));
    }

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (v.getId() == R.id.edit_username) {
                userNameHasFocus = hasFocus;
            } else if (v.getId() == R.id.edit_password) {
                passwordHasFocus = hasFocus;
            }
            if (!userNameHasFocus && !passwordHasFocus) {
                hideKeyboard(v);
            }
        }
    };

    private String getTranslationFromXML(String locale, String key) {
        if (locale == null) {
            locale = en_INLocale;
        }
        switch (locale) {
            case en_INLocale:
                switch (key) {
                    case "LABEL_USER_USERNAME":
                        return getString(R.string.LABEL_USER_USERNAME_EN);
                    case "LABEL_USER_PASSWORD":
                        return getString(R.string.LABEL_USER_PASSWORD_EN);
                    case "LABEL_APP_TITLE":
                        return getString(R.string.LABEL_APP_TITLE_EN);
                    case "LABEL_FORGOT_PASSWORD":
                        return getString(R.string.LABEL_FORGOT_PASSWORD_EN);
                    case "LABEL_LOGIN":
                        return getString(R.string.LABEL_LOGIN_EN);
                    case "LABEL_CHANGE_LANGUAGE":
                        return getString(R.string.LABEL_CHANGE_LANGUAGE_EN);
                    case "LABEL_FORGOT_PASSWORD_EMAIL":
                        return getString(R.string.LABEL_FORGOT_PASSWORD_EMAIL_EN);
                    case "LABEL_OK":
                        return getString(R.string.LABEL_OK_EN);
                    case "LABEL_ERROR":
                        return getString(R.string.LABEL_ERROR_EN);
                    case "LABEL_EMAIL_SENT":
                        return getString(R.string.LABEL_EMAIL_SENT_EN);
                    case "NO_NETWORK":
                        return getString(R.string.NO_NETWORK_EN);
                }
                break;
            case mr_INLocale:
                switch (key) {
                    case "LABEL_USER_USERNAME":
                        return getString(R.string.LABEL_USER_USERNAME_MR);
                    case "LABEL_USER_PASSWORD":
                        return getString(R.string.LABEL_USER_PASSWORD_MR);
                    case "LABEL_APP_TITLE":
                        return getString(R.string.LABEL_APP_TITLE_MR);
                    case "LABEL_FORGOT_PASSWORD":
                        return getString(R.string.LABEL_FORGOT_PASSWORD_MR);
                    case "LABEL_LOGIN":
                        return getString(R.string.LABEL_LOGIN_MR);
                    case "LABEL_CHANGE_LANGUAGE":
                        return getString(R.string.LABEL_CHANGE_LANGUAGE_MR);
                    case "LABEL_FORGOT_PASSWORD_EMAIL":
                        return getString(R.string.LABEL_FORGOT_PASSWORD_EMAIL_MR);
                    case "LABEL_OK":
                        return getString(R.string.LABEL_OK_MR);
                    case "LABEL_ERROR":
                        return getString(R.string.LABEL_ERROR_MR);
                    case "LABEL_EMAIL_SENT":
                        return getString(R.string.LABEL_EMAIL_SENT_MR);
                    case "NO_NETWORK":
                        return getString(R.string.NO_NETWORK_MR);
                }
                break;
        }
        return "";
    }

    private void sendForgotPasswordRequest(String userName) {
        ApiClient.getApiInterface(getApplicationContext()).sendForgotPasswordRequest(userName).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                String locale = SharedPreferenceUtil.getLocale(getApplicationContext());
                if (response.isSuccessful()) {
                    Toast.makeText(context, getTranslationFromXML(locale, "LABEL_EMAIL_SENT"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, getTranslationFromXML(locale, "LABEL_ERROR"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String locale = SharedPreferenceUtil.getLocale(getApplicationContext());
                Toast.makeText(context, getTranslationFromXML(locale, "LABEL_ERROR"), Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void login() {
        final String locale = SharedPreferenceUtil.getLocale(getApplicationContext());
        if (!NetworkConnection.isNetworkAvailable(getApplicationContext())){
            Toast.makeText(getApplicationContext(),
                    getTranslationFromXML(locale,"NO_NETWORK"),Toast.LENGTH_SHORT).show();
            return;
        }
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
        hideKeyboard(getWindow().getCurrentFocus());
        ApiInterface apiInterface = ApiClient.getApiInterface(getApplicationContext());
        LoginRequest obj = new LoginRequest();
        obj.setUsername(editUsername.getText().toString());
        obj.setPassword(editPassword.getText().toString());
        apiInterface.login(obj).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse res = response.body();
                    if (res != null && res.getGroupName().equals(Constants.group_name)) {
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        realm.copyToRealm(res);
                        realm.commitTransaction();
                        realm.close();
                        getTranslations();
                    }
                } else {
                    try {
                        JSONObject jObjError = null;
                        if (response.errorBody() != null) {
                            jObjError = new JSONObject(response.errorBody().string());
                        }
                        if (jObjError != null) {
                            Toast.makeText(LoginActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    Logger.d("Login Failed " + response.message());
                }
                progress.dismiss();
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                progress.dismiss();
                Toast.makeText(getApplicationContext(),
                        getTranslationFromXML(locale,"LABEL_ERROR"),Toast.LENGTH_SHORT).show();
            }

        });
    }


    private void getTranslations() {

        ApiInterface apiInterface = ApiClient.getApiInterface(getApplicationContext());
        apiInterface.getTranslation().enqueue(new Callback<Translation>() {
            @Override
            public void onResponse(Call<Translation> call, Response<Translation> response) {

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
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    final String locale = SharedPreferenceUtil.getLocale(getApplicationContext());
                    Toast.makeText(getApplicationContext(),
                            getTranslationFromXML(locale,"LABEL_ERROR"),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Translation> call, Throwable t) {
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                realm.delete(LoginResponse.class);
                realm.commitTransaction();
                final String locale = SharedPreferenceUtil.getLocale(getApplicationContext());
                Toast.makeText(getApplicationContext(),
                        getTranslationFromXML(locale,"LABEL_ERROR"),Toast.LENGTH_SHORT).show();

            }
        });
    }
}
