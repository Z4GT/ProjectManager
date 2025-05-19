package com.example.projectmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.UUID;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "project_manager.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_PROJECTS = "projects";
    private static final String TABLE_ACTIVITIES = "activities";
    private static final String TABLE_RECOVERY_CODES = "recovery_codes";

    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "username TEXT UNIQUE, " +
            "password TEXT)";

    private static final String CREATE_PROJECTS_TABLE = "CREATE TABLE " + TABLE_PROJECTS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT, " +
            "description TEXT, " +
            "start_date TEXT, " +
            "end_date TEXT, " +
            "user_id INTEGER, " +
            "FOREIGN KEY(user_id) REFERENCES users(id))";

    private static final String CREATE_ACTIVITIES_TABLE = "CREATE TABLE " + TABLE_ACTIVITIES + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "project_id INTEGER, " +
            "name TEXT, " +
            "description TEXT, " +
            "start_date TEXT, " +
            "end_date TEXT, " +
            "status TEXT, " +
            "FOREIGN KEY(project_id) REFERENCES projects(id))";

    private static final String CREATE_RECOVERY_CODES_TABLE = "CREATE TABLE " + TABLE_RECOVERY_CODES + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "user_id INTEGER, " +
            "code TEXT, " +
            "expiration_time INTEGER, " +
            "FOREIGN KEY(user_id) REFERENCES users(id))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_PROJECTS_TABLE);
        db.execSQL(CREATE_ACTIVITIES_TABLE);
        db.execSQL(CREATE_RECOVERY_CODES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_RECOVERY_CODES_TABLE);
        }
    }

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public int validateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_USERS +
                " WHERE username = ? AND password = ?", new String[]{username, password});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndex("id"));
        }
        cursor.close();
        db.close();
        return userId;
    }

    public String generateRecoveryCode(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_USERS +
                " WHERE username = ?", new String[]{username});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndex("id"));
        }
        cursor.close();

        if (userId == -1) {
            db.close();
            return null;
        }

        String code = UUID.randomUUID().toString().substring(0, 8);
        long expirationTime = System.currentTimeMillis() + 10 * 60 * 1000;

        db.delete(TABLE_RECOVERY_CODES, "user_id = ?", new String[]{String.valueOf(userId)});

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("code", code);
        values.put("expiration_time", expirationTime);
        long result = db.insert(TABLE_RECOVERY_CODES, null, values);
        db.close();
        return result != -1 ? code : null;
    }

    public int validateRecoveryCode(String code) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_RECOVERY_CODES, "expiration_time < ?",
                new String[]{String.valueOf(System.currentTimeMillis())});

        Cursor cursor = db.rawQuery("SELECT user_id FROM " + TABLE_RECOVERY_CODES +
                " WHERE code = ?", new String[]{code});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndex("user_id"));
        }
        cursor.close();
        db.close();
        return userId;
    }

    public boolean updatePassword(int userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        int result = db.update(TABLE_USERS, values, "id = ?",
                new String[]{String.valueOf(userId)});
        db.delete(TABLE_RECOVERY_CODES, "user_id = ?", new String[]{String.valueOf(userId)});
        db.close();
        return result > 0;
    }

    public boolean addProject(String name, String description, String startDate, String endDate, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("start_date", startDate);
        values.put("end_date", endDate);
        values.put("user_id", userId);
        long result = db.insert(TABLE_PROJECTS, null, values);
        db.close();
        return result != -1;
    }

    public Cursor getProjects(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PROJECTS + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)});
    }

    public boolean deleteProject(int projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ACTIVITIES, "project_id = ?", new String[]{String.valueOf(projectId)});
        int result = db.delete(TABLE_PROJECTS, "id = ?", new String[]{String.valueOf(projectId)});
        db.close();
        return result > 0;
    }

    public boolean addActivity(int projectId, String name, String description, String startDate, String endDate, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("project_id", projectId);
        values.put("name", name);
        values.put("description", description);
        values.put("start_date", startDate);
        values.put("end_date", endDate);
        values.put("status", status);
        long result = db.insert(TABLE_ACTIVITIES, null, values);
        db.close();
        return result != -1;
    }

    public boolean updateActivity(int activityId, String name, String description, String startDate, String endDate, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("start_date", startDate);
        values.put("end_date", endDate);
        values.put("status", status);
        int result = db.update(TABLE_ACTIVITIES, values, "id = ?",
                new String[]{String.valueOf(activityId)});
        db.close();
        return result > 0;
    }

    public Cursor getActivities(int projectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ACTIVITIES + " WHERE project_id = ?",
                new String[]{String.valueOf(projectId)});
    }

    public boolean deleteActivity(int activityId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_ACTIVITIES, "id = ?", new String[]{String.valueOf(activityId)});
        db.close();
        return result > 0;
    }

    public int getProjectProgress(int projectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT status FROM " + TABLE_ACTIVITIES + " WHERE project_id = ?",
                new String[]{String.valueOf(projectId)});
        int total = 0;
        int completed = 0;
        while (cursor.moveToNext()) {
            total++;
            if ("Realizado".equals(cursor.getString(cursor.getColumnIndex("status")))) {
                completed++;
            }
        }
        cursor.close();
        db.close();
        return total == 0 ? 0 : (completed * 100) / total;
    }
}