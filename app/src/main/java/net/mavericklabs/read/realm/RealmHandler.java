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

package net.mavericklabs.read.realm;

import net.mavericklabs.read.model.Book;
import net.mavericklabs.read.model.ClassroomEvaluation;
import net.mavericklabs.read.model.Inventory;
import net.mavericklabs.read.model.Level;
import net.mavericklabs.read.model.LoginResponse;
import net.mavericklabs.read.model.ReadSessionBookFairy;
import net.mavericklabs.read.model.RealmTranslation;
import net.mavericklabs.read.model.StudentBook;
import net.mavericklabs.read.model.ReadSession;
import net.mavericklabs.read.model.ReadSessionClassroom;
import net.mavericklabs.read.model.StudentBookInventory;
import net.mavericklabs.read.model.StudentEvaluation;
import net.mavericklabs.read.util.DateUtil;
import net.mavericklabs.read.util.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

import static net.mavericklabs.read.util.Constants.en_INLocale;
import static net.mavericklabs.read.util.Constants.mr_INLocale;
import static net.mavericklabs.read.util.Constants.session_evaluation;

/**
 * Created by Amey on 1/31/2019.
 */

public class RealmHandler {

    public static String getAccessToken() {
        Realm realm = Realm.getDefaultInstance();
        LoginResponse loginResponse = realm.where(LoginResponse.class).findFirst();
        String accessToken = null;
        if (loginResponse != null) {
            LoginResponse obj = realm.copyFromRealm(loginResponse);
            accessToken = obj.getToken();
        }
        realm.close();
        return accessToken;
    }

    public static LoginResponse getLoginResponse() {
        Realm realm = Realm.getDefaultInstance();
        LoginResponse loginResponse = realm.where(LoginResponse.class).findFirst();
        LoginResponse obj = null;
        if (loginResponse != null) {
            obj = realm.copyFromRealm(loginResponse);

        }
        realm.close();
        return obj;
    }

    public static void clearRealmDatabase() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
        realm.close();
    }

    public static ReadSession getReadSessionById(String key) {
        Realm realm = Realm.getDefaultInstance();
        ReadSession session = realm.where(ReadSession.class)
                .equalTo("key", key).findFirst();
        ReadSession obj = null;
        if (session != null) {
            obj = realm.copyFromRealm(session);

        }
        realm.close();
        return obj;
    }

    public static List<ReadSession> getAllEditedReadSessions() {
        Realm realm = Realm.getDefaultInstance();
        List<ReadSession> sessions = realm.where(ReadSession.class)
                .equalTo("storedOffline", true).findAll();
        List<ReadSession> list = new ArrayList<>();
        if (sessions != null) {
            list = realm.copyFromRealm(sessions);

        }
        realm.close();
        return list;
    }

    public static List<Level> getAllLevels() {
        Realm realm = Realm.getDefaultInstance();
        List<Level> level = realm.where(Level.class).findAll();
        List<Level> list = new ArrayList<>();
        if (level != null) {
            list = realm.copyFromRealm(level);

        }
        realm.close();
        return list;
    }

    public static boolean isEditedReadSession(String key) {
        Realm realm = Realm.getDefaultInstance();
        ReadSession session = realm.where(ReadSession.class)
                .equalTo("key", key).findFirst();

        if (session != null) {
            if (session.isStoredOffline()) {
                realm.close();
                return true;
            }
        }
        realm.close();
        return false;
    }

    private static List<ReadSession> getReadSessionList() {
        Realm realm = Realm.getDefaultInstance();
        List<ReadSession> list = realm.where(ReadSession.class).findAll();
        List<ReadSession> sessions = new ArrayList<>();
        if (list != null) {
            sessions = realm.copyFromRealm(list);
        }
        realm.close();
        return sessions;
    }


    public static void updateReadSessionList(List<ReadSession> requestList) {

        Logger.d("updateReadSessionList " + requestList.size());
        Realm realm = Realm.getDefaultInstance();

//        List<ReadSession> allSessions = getReadSessionList();
//        if (allSessions.size() != 0) {
//            for (ReadSession session : allSessions) {
//                for (ReadSession obj : requestList) {
//                    if (session.getKey().equals(obj.getKey())) {
//                        listToSave.add(obj);
//                        break;
//                    }
//                }
//            }
//        } else {
//            listToSave.clear();
//            listToSave.addAll(requestList);
//        }
        List<ReadSession> listToSave = new ArrayList<>(requestList);

        List<ReadSession> editedSessions = RealmHandler.getAllEditedReadSessions();
        if (editedSessions.size() > 0) {
            List<ReadSession> localList = new ArrayList<>();
            for (ReadSession session : listToSave) {
                if (RealmHandler.isEditedReadSession(session.getKey())) {
                    localList.add(RealmHandler.getReadSessionById(session.getKey()));
                } else {
                    localList.add(session);
                }
            }
            listToSave.clear();
            listToSave.addAll(localList);
        }


        RealmResults<ReadSession> allSessions = realm.where(ReadSession.class).findAll();
        Logger.d("allSessions " + allSessions.size());
        if (allSessions.size() > 0) {
            for (ReadSession session : allSessions) {


                for (ReadSessionBookFairy readSessionBookFairy : session.getBookFairySet()) {
                    realm.beginTransaction();
                    if (readSessionBookFairy.getBookFairy() != null) {
                        readSessionBookFairy.getBookFairy().deleteFromRealm();

                    }
                    realm.commitTransaction();

                }
                realm.beginTransaction();
                session.getBookFairySet().deleteAllFromRealm();
                realm.commitTransaction();

                for (ReadSessionClassroom readSessionClassroom : session.getReadSessionClassroom()) {
                    realm.beginTransaction();
                    if (readSessionClassroom.getClassroom() != null) {

                        if (readSessionClassroom.getClassroom().getSchool() != null)
                            readSessionClassroom.getClassroom().getSchool().deleteFromRealm();

                        if (readSessionClassroom.getClassroom().getStandard() != null)
                            readSessionClassroom.getClassroom().getStandard().deleteFromRealm();

                        readSessionClassroom.getClassroom().deleteFromRealm();
                    }


                    if (readSessionClassroom.getSession() != null)
                        readSessionClassroom.getSession().deleteFromRealm();

                    realm.commitTransaction();

                }

                realm.beginTransaction();
                session.getReadSessionClassroom().deleteAllFromRealm();
                realm.commitTransaction();
            }

            realm.beginTransaction();
            allSessions.deleteAllFromRealm();
            realm.commitTransaction();
        }

        Logger.d("listToSave " + listToSave.size());
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(listToSave);
        realm.commitTransaction();
        realm.close();
    }

    public static List<ReadSession> getPendingSessions() {
        Realm realm = Realm.getDefaultInstance();
        List<ReadSession> list = realm.where(ReadSession.class).findAll();
        if (list != null) {
            List<ReadSession> sessions = realm.copyFromRealm(list);
            List<ReadSession> readSessionList = new ArrayList<>();
            for (ReadSession readSession : sessions) {
                Date sessionDate = DateUtil.getDateFromString(readSession.getStartDateTime());
                Date currentDate = Calendar.getInstance().getTime();
                if (sessionDate.before(currentDate) && !readSession.isEvaluated()) {
                    readSessionList.add(readSession);
                }
            }
            realm.close();
            return readSessionList;
        }
        realm.close();
        return null;
    }

    public static List<ReadSession> getEvaluatedSessions() {
        Realm realm = Realm.getDefaultInstance();
        List<ReadSession> list = realm.where(ReadSession.class).findAll();
        if (list != null) {
            List<ReadSession> sessions = realm.copyFromRealm(list);
            List<ReadSession> readSessionList = new ArrayList<>();
            for (ReadSession readSession : sessions) {
                if (readSession.isEvaluated() && !readSession.isVerified()) {
                    readSessionList.add(readSession);
                }
            }
            realm.close();
            return readSessionList;
        }
        realm.close();
        return null;
    }

    public static List<ReadSession> getUpcomingSessions() {
        Realm realm = Realm.getDefaultInstance();
        List<ReadSession> list = realm.where(ReadSession.class).findAll();
        if (list != null) {
            List<ReadSession> sessions = realm.copyFromRealm(list);
            List<ReadSession> readSessionList = new ArrayList<>();
            for (ReadSession readSession : sessions) {
                Date sessionDate = DateUtil.getDateFromString(readSession.getStartDateTime());
                Date currentDate = Calendar.getInstance().getTime();
                if (sessionDate != null && !sessionDate.before(currentDate)) {
                    readSessionList.add(readSession);
                }
            }
            realm.close();
            return readSessionList;
        }
        realm.close();
        return null;
    }

    public static void updateBooks(List<Book> body) {

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(Book.class).findAll().deleteAllFromRealm();
        realm.copyToRealmOrUpdate(body);
        realm.commitTransaction();
        realm.close();
    }

    public static void updateLevels(List<Level> body) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(Level.class).findAll().deleteAllFromRealm();
        realm.copyToRealmOrUpdate(body);
        realm.commitTransaction();
        realm.close();
    }

    public static ClassroomEvaluation getClassRoomEvaluation(String classroomKey, String
            sessionKey) {
        Realm realm = Realm.getDefaultInstance();
        ClassroomEvaluation classroom = realm.where(ClassroomEvaluation.class)
                .equalTo("classroomKey", classroomKey)
                .equalTo("sessionKey", sessionKey).findFirst();
        if (classroom != null) {
            ClassroomEvaluation obj = realm.copyFromRealm(classroom);
            realm.close();
            return obj;
        }
        realm.close();
        return null;
    }

    public static List<StudentEvaluation> getEvaluatedStudents(String sessionKey) {
        Realm realm = Realm.getDefaultInstance();
        List<StudentEvaluation> studentEvaluations = realm.where(StudentEvaluation.class)
                .equalTo("sessionKey", sessionKey).findAll();
        if (studentEvaluations != null) {
            List<StudentEvaluation> list = realm.copyFromRealm(studentEvaluations);
            realm.close();
            return list;
        }
        realm.close();
        return null;
    }

    public static List<StudentEvaluation> getStudentsOfClassroom(String classroomKey, String
            sessionKey) {
        Realm realm = Realm.getDefaultInstance();
        List<StudentEvaluation> students = new ArrayList<>();
        ClassroomEvaluation classroom = realm.where(ClassroomEvaluation.class)
                .equalTo("classroomKey", classroomKey)
                .equalTo("sessionKey", sessionKey).findFirst();
        if (classroom != null && classroom.getStudents() != null) {
            students = realm.copyFromRealm(classroom.getStudents());
        }
        realm.close();
        return students;
    }

    public static StudentEvaluation getEvaluationStudent(String classroomKey, String
            studentKey, String sessionKey) {

        Realm realm = Realm.getDefaultInstance();
        ClassroomEvaluation classroom = realm.where(ClassroomEvaluation.class)
                .equalTo("classroomKey", classroomKey).
                        equalTo("sessionKey", sessionKey).findFirst();
        if (classroom != null) {
            List<StudentEvaluation> list = classroom.getStudents();
            if (list != null) {
                List<StudentEvaluation> students = realm.copyFromRealm(list);
                for (StudentEvaluation student : students) {
                    if (student.getSessionKey().equals(sessionKey) &&
                            student.getStudent().getKey().equals(studentKey)) {
                        realm.close();
                        return student;
                    }
                }
            }

        }
        realm.close();
        return null;
    }

    public static void updateEvaluationClassroom(ClassroomEvaluation classroom) {
        Realm realm = Realm.getDefaultInstance();
        ClassroomEvaluation classRoomEvaluation = realm.where(ClassroomEvaluation.class)
                .equalTo("classroomKey", classroom.getClassroomKey())
                .equalTo("sessionKey", classroom.getSessionKey()).findFirst();
        if (classRoomEvaluation != null) {
            RealmList<StudentEvaluation> students = classRoomEvaluation.getStudents();
            for (StudentEvaluation studentEvaluation : students) {
                if (studentEvaluation.getLevel() != null) {
                    realm.beginTransaction();
                    studentEvaluation.getLevel().deleteFromRealm();
                    realm.commitTransaction();
                }
                if (studentEvaluation.getStudent() != null) {
                    RealmList<StudentBookInventory> inventory = studentEvaluation.getStudent().getInventory();
                    if (inventory != null) {
                        for (StudentBookInventory obj : inventory) {
                            realm.beginTransaction();
                            obj.getBook().deleteFromRealm();
                            realm.commitTransaction();
                        }
                        realm.beginTransaction();
                        inventory.deleteAllFromRealm();
                        realm.commitTransaction();
                    }
                    realm.beginTransaction();
                    studentEvaluation.getStudent().deleteFromRealm();
                    realm.commitTransaction();
                }
            }
            realm.beginTransaction();
            students.deleteAllFromRealm();
            realm.commitTransaction();
            realm.beginTransaction();
            classRoomEvaluation.deleteFromRealm();
            realm.commitTransaction();
        }
        realm.beginTransaction();
        Logger.d("updateClassRoom students size " + classroom.getStudents().size());
        realm.copyToRealm(classroom);
        realm.commitTransaction();
        realm.close();
    }


    public static StudentBookInventory getBookBySerialNumber(String code) {
        Realm realm = Realm.getDefaultInstance();
        Inventory inventory = realm.where(Inventory.class)
                .equalTo("serialNumber", code).findFirst();
        if (inventory != null) {
            Book book = realm.where(Book.class)
                    .equalTo("key", inventory.getBookKey()).findFirst();
            if (book != null) {
                Book obj = realm.copyFromRealm(book);
                StudentBookInventory studentBookInventory = new StudentBookInventory();
                StudentBook studentBook = new StudentBook();
                studentBook.setBookName(obj.getBookName());
                studentBook.setKey(obj.getKey());
                studentBook.setType(obj.getType());
                studentBookInventory.setBook(studentBook);
                Inventory inventory1 = realm.copyFromRealm(inventory);
                studentBookInventory.setKey(inventory1.getKey());
                studentBookInventory.setSerialNumber(inventory1.getSerialNumber());
                studentBookInventory.setActive(inventory1.isActive());
                realm.close();
                return studentBookInventory;
            } else {
                realm.close();
                return null;
            }

        } else {
            realm.close();
            return null;
        }

    }

    public static List<StudentBookInventory> getBooksListBySerialNumber(String serialNumber) {
        Realm realm = Realm.getDefaultInstance();
        List<Inventory> list = realm.where(Inventory.class)
                .beginsWith("serialNumber", serialNumber).findAll();
        if (list != null) {
            List<Inventory> inventories = realm.copyFromRealm(list);
            List<StudentBookInventory> studentBookInventories = new ArrayList<>();
            for (Inventory inventory : inventories) {
                Book book = realm.where(Book.class)
                        .equalTo("key", inventory.getBookKey()).findFirst();
                if (book != null) {
                    Book obj = realm.copyFromRealm(book);
                    StudentBook studentBook = new StudentBook();
                    studentBook.setBookName(obj.getBookName());
                    studentBook.setKey(obj.getKey());
                    studentBook.setType(obj.getType());
                    StudentBookInventory studentBookInventory = new StudentBookInventory();
                    studentBookInventory.setSerialNumber(inventory.getSerialNumber());
                    studentBookInventory.setActive(inventory.isActive());
                    studentBookInventory.setKey(inventory.getKey());
                    studentBookInventory.setBook(studentBook);
                    studentBookInventories.add(studentBookInventory);
                }
            }
            realm.close();

            return studentBookInventories;

        } else {
            realm.close();
            return null;
        }
    }

    public static String getTranslation(String locale, String key) {
        Realm realm = Realm.getDefaultInstance();
        RealmTranslation realmTranslation = realm.where(RealmTranslation.class)
                .equalTo("locale", locale)
                .equalTo("key", key).findFirst();
        String value = key;
        if (realmTranslation != null) {
            value = realmTranslation.getValue();
        }
        realm.close();
        Logger.d(value);
        return value;

    }


    public static String getLevelNameByKey(String key, String locale) {
        Realm realm = Realm.getDefaultInstance();
        Level level = realm.where(Level.class).equalTo("key", key).findFirst();
        if (level != null) {
            Level level1 = realm.copyFromRealm(level);
            realm.close();
            String strName = "";
            if (locale.equals(en_INLocale))
                strName = level1.getEnglishName();
            else if (locale.equals(mr_INLocale))
                strName = level1.getMarathiName();

            return strName;
        }
        realm.close();
        return null;
    }



    public static List<Level> getLevelBySessionType(String sessionType) {
        List<Level> list;
        Realm realm = Realm.getDefaultInstance();
        if (sessionType.equals(session_evaluation)) {
            list = realm.where(Level.class).equalTo("isEvaluation", true).findAll();
        } else {
            list = realm.where(Level.class).equalTo("isRegular", true).findAll();
        }

        if (list != null) {
            List<Level> levels = realm.copyFromRealm(list);
            realm.close();
            return levels;
        }
        realm.close();
        return null;
    }
}
