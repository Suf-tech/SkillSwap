package com.example.skillswap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DBNAME = "SkillSwap.db";

    public DatabaseHelper(Context context) {
        // Version 4 hi rehne dein taake existing data na ure
        super(context, DBNAME, null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table users(email TEXT primary key, name TEXT, password TEXT, avatar_id INTEGER DEFAULT 0)");
        db.execSQL("create Table requests(id INTEGER PRIMARY KEY AUTOINCREMENT, sender_email TEXT, receiver_email TEXT, skill_offered TEXT, skill_required TEXT, message TEXT, status TEXT DEFAULT 'Pending')");
        db.execSQL("create Table posts(id INTEGER PRIMARY KEY AUTOINCREMENT, user_email TEXT, user_name TEXT, skill_have TEXT, skill_want TEXT, message TEXT, avatar_id INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists users");
        db.execSQL("drop Table if exists requests");
        db.execSQL("drop Table if exists posts");
        onCreate(db);
    }

    // --- 1. AUTH & FORGET PASSWORD METHODS ---
    public Boolean insertData(String email, String name, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("email", email);
        cv.put("name", name);
        cv.put("password", password);
        return db.insert("users", null, cv) != -1;
    }

    public Boolean checkEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from users where email = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public Boolean checkEmailPassword(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from users where email = ? and password = ?", new String[]{email, password});
        boolean match = cursor.getCount() > 0;
        cursor.close();
        return match;
    }

    public String getPassword(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select password from users where email = ?", new String[]{email});
        if (cursor.moveToFirst()) {
            String pass = cursor.getString(0);
            cursor.close();
            return pass;
        }
        cursor.close();
        return null;
    }

    public Boolean updatePassword(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password", password);
        return db.update("users", cv, "email = ?", new String[]{email}) > 0;
    }

    // --- 2. PROFILE & AVATAR METHODS ---
    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select name from users where email = ?", new String[]{email});
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            cursor.close();
            return name;
        }
        cursor.close();
        return "User";
    }

    public int getAvatarId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select avatar_id from users where email = ?", new String[]{email});
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        cursor.close();
        return 0;
    }

    public Boolean updateAvatar(String email, int avatarId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("avatar_id", avatarId);
        return db.update("users", cv, "email = ?", new String[]{email}) > 0;
    }

    // --- NEW: FULL PROFILE UPDATE METHOD ---
    public boolean updateFullProfile(String email, String name, String password, int avatarId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        // Agar password box khali nahi hai to hi update karein
        if (password != null && !password.isEmpty()) {
            cv.put("password", password);
        }
        cv.put("avatar_id", avatarId);
        return db.update("users", cv, "email = ?", new String[]{email}) > 0;
    }

    // --- 3. POSTS (ADD, EDIT, DELETE, GET) METHODS ---
    public boolean addPost(String email, String name, String have, String want, String msg, int avatar) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_email", email);
        cv.put("user_name", name);
        cv.put("skill_have", have);
        cv.put("skill_want", want);
        cv.put("message", msg);
        cv.put("avatar_id", avatar);
        return db.insert("posts", null, cv) != -1;
    }

    public boolean updatePost(int id, String have, String want, String msg) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("skill_have", have);
        cv.put("skill_want", want);
        cv.put("message", msg);
        return db.update("posts", cv, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deletePost(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("posts", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public Cursor getAllPosts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("Select * from posts ORDER BY id DESC", null);
    }

    public Cursor getPostById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("Select * from posts where id = ?", new String[]{String.valueOf(id)});
    }

    // --- 4. REQUEST METHODS ---
    public boolean updateRequestStatus(int id, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);
        return db.update("requests", cv, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean sendRequest(String sender, String receiver, String offered, String required, String msg) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("sender_email", sender);
        cv.put("receiver_email", receiver);
        cv.put("skill_offered", offered);
        cv.put("skill_required", required);
        cv.put("message", msg);
        cv.put("status", "Pending");
        return db.insert("requests", null, cv) != -1;
    }

    public Cursor getMyRequests(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("Select * from requests where sender_email = ? OR receiver_email = ? ORDER BY id DESC", new String[]{email, email});
    }
}