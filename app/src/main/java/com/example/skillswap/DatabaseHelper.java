package com.example.skillswap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DBNAME = "SkillSwap.db";

    public DatabaseHelper(Context context) {
        super(context, DBNAME, null, 6);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table users(email TEXT primary key, name TEXT, password TEXT, avatar_id INTEGER DEFAULT 0, rating_sum REAL DEFAULT 0, rating_count INTEGER DEFAULT 0)");
        db.execSQL("create Table requests(id INTEGER PRIMARY KEY AUTOINCREMENT, post_id INTEGER, sender_email TEXT, receiver_email TEXT, skill_offered TEXT, skill_required TEXT, message TEXT, status TEXT DEFAULT 'Pending', sender_rated INTEGER DEFAULT 0, receiver_rated INTEGER DEFAULT 0)");
        db.execSQL("create Table posts(id INTEGER PRIMARY KEY AUTOINCREMENT, user_email TEXT, user_name TEXT, skill_have TEXT, skill_want TEXT, message TEXT, avatar_id INTEGER, post_status TEXT DEFAULT 'Open')");
        db.execSQL("create Table messages(id INTEGER PRIMARY KEY AUTOINCREMENT, request_id INTEGER, sender_email TEXT, receiver_email TEXT, message_text TEXT, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists users");
        db.execSQL("drop Table if exists requests");
        db.execSQL("drop Table if exists posts");
        db.execSQL("drop Table if exists messages");
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

    // --- 2. PROFILE, AVATAR & RATINGS METHODS ---
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

    public boolean updateFullProfile(String email, String name, String password, int avatarId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        if (password != null && !password.isEmpty()) {
            cv.put("password", password);
        }
        cv.put("avatar_id", avatarId);
        return db.update("users", cv, "email = ?", new String[]{email}) > 0;
    }

    public boolean rateUser(String email, float newRating) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select rating_sum, rating_count from users where email = ?", new String[]{email});
        if (cursor.moveToFirst()) {
            float currentSum = cursor.getFloat(0);
            int currentCount = cursor.getInt(1);

            ContentValues cv = new ContentValues();
            cv.put("rating_sum", currentSum + newRating);
            cv.put("rating_count", currentCount + 1);

            cursor.close();
            return db.update("users", cv, "email = ?", new String[]{email}) > 0;
        }
        cursor.close();
        return false;
    }

    public String getUserRating(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select rating_sum, rating_count from users where email = ?", new String[]{email});
        if (cursor.moveToFirst()) {
            float sum = cursor.getFloat(0);
            int count = cursor.getInt(1);
            cursor.close();

            if (count == 0) return "No Ratings Yet";

            float average = sum / count;
            return String.format("%.1f (%d reviews)", average, count);
        }
        cursor.close();
        return "No Ratings Yet";
    }

    // --- 3. POSTS METHODS ---
    public boolean addPost(String email, String name, String have, String want, String msg, int avatar) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_email", email);
        cv.put("user_name", name);
        cv.put("skill_have", have);
        cv.put("skill_want", want);
        cv.put("message", msg);
        cv.put("avatar_id", avatar);
        cv.put("post_status", "Open");
        return db.insert("posts", null, cv) != -1;
    }

    // THE RESTORED METHOD
    public boolean updatePost(int id, String have, String want, String msg) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("skill_have", have);
        cv.put("skill_want", want);
        cv.put("message", msg);
        return db.update("posts", cv, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean closePost(int postId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("post_status", "Closed");
        return db.update("posts", cv, "id = ?", new String[]{String.valueOf(postId)}) > 0;
    }

    public boolean deletePost(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Cleans up associated requests when a post is deleted
        db.delete("requests", "post_id = ?", new String[]{String.valueOf(id)});
        return db.delete("posts", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public Cursor getAllOpenPosts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("Select * from posts WHERE post_status = 'Open' ORDER BY id DESC", null);
    }

    public Cursor getPostById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("Select * from posts where id = ?", new String[]{String.valueOf(id)});
    }

    // --- 4. REQUEST METHODS ---
    public boolean sendRequest(int postId, String sender, String receiver, String offered, String required, String msg) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("post_id", postId);
        cv.put("sender_email", sender);
        cv.put("receiver_email", receiver);
        cv.put("skill_offered", offered);
        cv.put("skill_required", required);
        cv.put("message", msg);
        cv.put("status", "Pending");
        return db.insert("requests", null, cv) != -1;
    }

    public boolean updateRequestStatus(int id, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);
        return db.update("requests", cv, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean markAsRated(int requestId, boolean isSender) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (isSender) cv.put("sender_rated", 1);
        else cv.put("receiver_rated", 1);
        return db.update("requests", cv, "id = ?", new String[]{String.valueOf(requestId)}) > 0;
    }

    public Cursor getMyRequests(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("Select * from requests where sender_email = ? OR receiver_email = ? ORDER BY id DESC", new String[]{email, email});
    }

    // --- 5. CHAT METHODS ---
    public boolean insertMessage(int requestId, String sender, String receiver, String text) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("request_id", requestId);
        cv.put("sender_email", sender);
        cv.put("receiver_email", receiver);
        cv.put("message_text", text);
        return db.insert("messages", null, cv) != -1;
    }

    public Cursor getChatHistory(int requestId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM messages WHERE request_id = ? ORDER BY id ASC", new String[]{String.valueOf(requestId)});
    }
}