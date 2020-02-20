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
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.mavericklabs.read.R;
import net.mavericklabs.read.model.LoginResponse;
import net.mavericklabs.read.realm.RealmHandler;
import net.mavericklabs.read.retrofit.ApiClient;
import net.mavericklabs.read.util.Logger;
import net.mavericklabs.read.util.NetworkConnection;
import net.mavericklabs.read.util.SharedPreferenceUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.mavericklabs.read.realm.RealmHandler.getTranslation;


public class ResetPasswordFragment extends Fragment {

    private EditText editOldPassword, editNewPassword, editConfirmPassword;
    private TextView textError;
    private Button btn;
    private boolean oldPasswordHasFocus = false;
    private boolean newPasswordHasFocus = false;
    private boolean confirmPasswordHasFocus = false;
    private String locale;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editOldPassword = view.findViewById(R.id.edit_old_password);
        editNewPassword = view.findViewById(R.id.edit_new_password);
        editConfirmPassword = view.findViewById(R.id.edit_new_confirm_password);
        btn = view.findViewById(R.id.btn_confirm);
        textError = view.findViewById(R.id.text_error);
        editOldPassword.setOnFocusChangeListener(onFocusChangeListener);
        editNewPassword.setOnFocusChangeListener(onFocusChangeListener);
        editConfirmPassword.setOnFocusChangeListener(onFocusChangeListener);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                if (isCorrectData()) {
                    resetPassword();
                }
            }
        });
        setTranslations();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.search);
        if (item != null)
            item.setVisible(false);
    }

    private void setTranslations() {
        locale = SharedPreferenceUtil.getLocale(getContext());
        editOldPassword.setHint(getTranslation(locale, "LABEL_OLD_PASSWORD"));
        editNewPassword.setHint(getTranslation(locale, "LABEL_NEW_PASSWORD"));
        editConfirmPassword.setHint(getTranslation(locale, "LABEL_CONFIRM_PASSWORD"));
        btn.setText(getTranslation(locale, "LABEL_SAVE"));
    }

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (v.getId() == R.id.edit_old_password) {
                oldPasswordHasFocus = hasFocus;
            } else if (v.getId() == R.id.edit_new_password) {
                newPasswordHasFocus = hasFocus;
            } else if (v.getId() == R.id.edit_new_confirm_password) {
                confirmPasswordHasFocus = hasFocus;
            }
            if (!oldPasswordHasFocus && !newPasswordHasFocus && !confirmPasswordHasFocus) {
                hideKeyboard(v);
            }
        }
    };

    private void resetPassword() {
        hideKeyboard(getView());
        Context context = getContext();
        if (context == null)
            return;
        if (NetworkConnection.isNetworkAvailable(context)) {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            LoginResponse loginResponse = RealmHandler.getLoginResponse();
            if (loginResponse != null) {
                ApiClient.getApiInterface(getContext()).resetPassword(loginResponse.getUserKey()
                        , editOldPassword.getText().toString()
                        , editNewPassword.getText().toString()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), getTranslation(locale, "CHANGED_PASSWORD"), Toast.LENGTH_SHORT).show();
                            getActivity().onBackPressed();
                        } else {
                            Toast.makeText(getContext(), getTranslation(locale, "OLD_PASSWORD_IS_WRONG"), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Logger.d("onFailure");
                        progressDialog.dismiss();
                    }
                });
            }
        } else {
            Toast.makeText(getContext(), getTranslation(locale, "NO_NETWORK"), Toast.LENGTH_SHORT).show();
        }

    }


    private void hideKeyboard(View view) {
        Context context = getContext();
        if (context != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private boolean isCorrectData() {
        String oldPassword = editOldPassword.getText().toString().trim();
        String newPassword = editNewPassword.getText().toString().trim();
        String newConfirmPassword = editConfirmPassword.getText().toString().trim();
        if (oldPassword.equals("") || newPassword.equals("") || newConfirmPassword.equals("")) {
            textError.setText(getTranslation(locale, "ALL_PASSWORDS_FIELD_ERROR"));
            textError.setVisibility(View.VISIBLE);
            return false;
        } else if (!editNewPassword.getText().toString().equals(editConfirmPassword.getText().toString())) {
            textError.setText(getTranslation(locale, "CONFIRM_PASSWORD_ERROR"));
            textError.setVisibility(View.VISIBLE);
            return false;
        } else
            return true;
    }


}
