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

package net.mavericklabs.read.retrofit;

import net.mavericklabs.read.model.Book;
import net.mavericklabs.read.model.ReadSessionClassroomWithStudents;
import net.mavericklabs.read.model.Inventory;
import net.mavericklabs.read.model.Level;
import net.mavericklabs.read.model.Translation;
import net.mavericklabs.read.retrofit.custom.BookLendingSubmitRequest;
import net.mavericklabs.read.retrofit.custom.LoginRequest;
import net.mavericklabs.read.model.LoginResponse;
import net.mavericklabs.read.model.ReadSession;
import net.mavericklabs.read.model.StudentEvaluation;
import net.mavericklabs.read.retrofit.custom.StudentEvaluationRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Amey on 4/8/2019.
 */

public interface ApiInterface {
    @POST("/mobile_login/")
    Call<LoginResponse> login(@Body LoginRequest obj);

    @POST("/refresh_mobile_token/")
    Call<LoginResponse> refreshToken();

    @GET("/ngos/{ngoKey}/book_fairy_sessions/")
    Call<List<ReadSession>> getAllSessions(@Path("ngoKey") String ngoKey, @Query("fairy") String fairyKey);

    @GET("/ngos/{ngoKey}/mobile_book_fairy_sessions/")
    Call<List<ReadSession>> getSessionsByType(@Path("ngoKey") String ngoKey, @Query("fairy") String fairyKey, @Query("type") String type);

    @GET("/read_sessions/{session_key}/session_classrooms/")
    Call<List<ReadSessionClassroomWithStudents>> getSessionClassroom(@Path("session_key") String sessionKey, @Query("ngo") String ngoKey);

    @GET("/read_sessions/{session_key}/get_student_evaluation/")
    Call<List<StudentEvaluation>> getStudentEvaluationList(@Path("session_key") String sessionKey);

    @GET("/read_sessions/{session_key}/get_home_lending_books/")
    Call<List<StudentEvaluation>> getHomeLendingBooks(@Path("session_key") String sessionKey);

    @GET("/ngos/{ngo_key}/mobile_books/")
    Call<List<Book>> getBooksList(@Path("ngo_key") String ngoKey);

    @GET("/books/{book_key}/mobile_inventory/")
    Call<List<Inventory>> getBookInventory(@Path("book_key") String bookKey);

    @GET("/ngos/{ngo_key}/get_levels/")
    Call<List<Level>> getLevels(@Path("ngo_key") String ngoKey);

    @POST("read_sessions/{session_key}/evaluate_students/")
    Call<List<ReadSessionClassroomWithStudents>> evaluateSession(@Path("session_key") String sessionKey, @Body StudentEvaluationRequest body);

    @POST("read_sessions/{session_key}/submit_evaluations/")
    Call<Void> submitSession(@Path("session_key") String sessionKey, @Body StudentEvaluationRequest body);

    @POST("read_sessions/{session_key}/submit_home_lending_books/")
    Call<Void> submitBookLendingSession(@Path("session_key") String sessionKey, @Body BookLendingSubmitRequest body);

    @GET("/translations/")
    Call<Translation> getTranslation();


    @FormUrlEncoded
    @POST("book_fairy_forgot_password/")
    Call<Void> sendForgotPasswordRequest(@Field("username") String username);

    @FormUrlEncoded
    @POST("/users/{user_key}/reset_password/")
    Call<Void> resetPassword(@Path("user_key") String userKey, @Field("old_password") String oldPassword, @Field("new_password") String newPassword);

    @FormUrlEncoded
    @POST("/read_sessions/{session_key}/add_comment_on_session/")
    Call<Void> addCommentToSession(@Path("session_key") String sessionKey, @Field("comments") String comments);
}
