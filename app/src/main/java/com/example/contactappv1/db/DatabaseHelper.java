package com.example.contactappv1.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.contactappv1.model.Contact;
import com.example.contactappv1.model.RecentCall;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite DatabaseHelper for the Contact App.
 *
 * Tables:
 *  1. contacts   — stores all contact entries (name, phone, online, favorite)
 *  2. call_logs  — stores call history (contact_id FK, call_type, timestamp)
 *
 * Supported operations:
 *  Contacts : insert, update, delete, getAll, search by name/phone,
 *             getOnline, getFavorites, toggleFavorite, toggleOnline
 *  Call Logs: insertCall, getAllCalls, getCallsByType (missed/incoming/outgoing),
 *             getCallsByContact, getMissedCallCount, deleteCall, clearAllCalls
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    //Database metadata
    private static final String DB_NAME    = "contacts.db";
    private static final int    DB_VERSION = 1;

    //Table: contacts
    public static final String TABLE_CONTACTS      = "contacts";
    public static final String COL_CONTACT_ID      = "id";
    public static final String COL_CONTACT_NAME    = "name";
    public static final String COL_CONTACT_PHONE   = "phone";
    public static final String COL_CONTACT_ONLINE  = "online";   // 1 = online, 0 = offline
    public static final String COL_CONTACT_FAVORITE= "favorite"; // 1 = favorite

    //Table: call_logs
    public static final String TABLE_CALL_LOGS     = "call_logs";
    public static final String COL_CALL_ID         = "id";
    public static final String COL_CALL_CONTACT_ID = "contact_id"; // FK → contacts.id (nullable for unknown)
    public static final String COL_CALL_NAME       = "name";       // denormalized for deleted contacts
    public static final String COL_CALL_PHONE      = "phone";
    public static final String COL_CALL_TYPE       = "call_type";  // 0=incoming, 1=outgoing, 2=missed
    public static final String COL_CALL_TIMESTAMP  = "timestamp";  // epoch millis
    public static final String COL_CALL_DURATION   = "duration";   // seconds (0 for missed)
    public static final String COL_CALL_LABEL      = "label";      // "Mobile", "Home", "Work"

    //CREATE statements
    private static final String CREATE_CONTACTS =
            "CREATE TABLE " + TABLE_CONTACTS + " (" +
            COL_CONTACT_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_CONTACT_NAME     + " TEXT NOT NULL, " +
            COL_CONTACT_PHONE    + " TEXT NOT NULL, " +
            COL_CONTACT_ONLINE   + " INTEGER DEFAULT 0, " +
            COL_CONTACT_FAVORITE + " INTEGER DEFAULT 0" +
            ");";

    private static final String CREATE_CALL_LOGS =
            "CREATE TABLE " + TABLE_CALL_LOGS + " (" +
            COL_CALL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_CALL_CONTACT_ID + " INTEGER, " +
            COL_CALL_NAME       + " TEXT NOT NULL, " +
            COL_CALL_PHONE      + " TEXT NOT NULL, " +
            COL_CALL_TYPE       + " INTEGER NOT NULL, " +
            COL_CALL_TIMESTAMP  + " INTEGER NOT NULL, " +
            COL_CALL_DURATION   + " INTEGER DEFAULT 0, " +
            COL_CALL_LABEL      + " TEXT DEFAULT 'Mobile', " +
            "FOREIGN KEY (" + COL_CALL_CONTACT_ID + ") REFERENCES " +
                    TABLE_CONTACTS + "(" + COL_CONTACT_ID + ") ON DELETE SET NULL" +
            ");";

    //Singleton
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //Lifecycle

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CONTACTS);
        db.execSQL(CREATE_CALL_LOGS);
        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALL_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    //contacts — Write operations

    /** Insert a new contact. Returns the new row ID, or -1 on failure. */
    public long insertContact(String name, String phone, boolean online, boolean favorite) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CONTACT_NAME,     name);
        cv.put(COL_CONTACT_PHONE,    phone);
        cv.put(COL_CONTACT_ONLINE,   online   ? 1 : 0);
        cv.put(COL_CONTACT_FAVORITE, favorite ? 1 : 0);
        return db.insert(TABLE_CONTACTS, null, cv);
    }

    /** Update all fields of an existing contact. */
    public int updateContact(long id, String name, String phone, boolean online, boolean favorite) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CONTACT_NAME,     name);
        cv.put(COL_CONTACT_PHONE,    phone);
        cv.put(COL_CONTACT_ONLINE,   online   ? 1 : 0);
        cv.put(COL_CONTACT_FAVORITE, favorite ? 1 : 0);
        return db.update(TABLE_CONTACTS, cv,
                COL_CONTACT_ID + "=?", new String[]{String.valueOf(id)});
    }

    /** Delete a contact by ID. */
    public int deleteContact(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_CONTACTS,
                COL_CONTACT_ID + "=?", new String[]{String.valueOf(id)});
    }

    /** Toggle the favorite flag for a contact. Returns the new value. */
    public boolean toggleFavorite(long id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(TABLE_CONTACTS,
                new String[]{COL_CONTACT_FAVORITE},
                COL_CONTACT_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        boolean newVal = false;
        if (c.moveToFirst()) {
            newVal = c.getInt(0) == 0; // flip
        }
        c.close();
        ContentValues cv = new ContentValues();
        cv.put(COL_CONTACT_FAVORITE, newVal ? 1 : 0);
        db.update(TABLE_CONTACTS, cv,
                COL_CONTACT_ID + "=?", new String[]{String.valueOf(id)});
        return newVal;
    }

    /** Toggle the online flag for a contact. Returns the new value. */
    public boolean toggleOnline(long id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(TABLE_CONTACTS,
                new String[]{COL_CONTACT_ONLINE},
                COL_CONTACT_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        boolean newVal = false;
        if (c.moveToFirst()) {
            newVal = c.getInt(0) == 0;
        }
        c.close();
        ContentValues cv = new ContentValues();
        cv.put(COL_CONTACT_ONLINE, newVal ? 1 : 0);
        db.update(TABLE_CONTACTS, cv,
                COL_CONTACT_ID + "=?", new String[]{String.valueOf(id)});
        return newVal;
    }

    //contacts: Read operations
    /** Return all contacts sorted A-Z by name. */
    public List<Contact> getAllContacts() {
        return queryContacts(null, null, COL_CONTACT_NAME + " ASC");
    }

    /** Return contacts whose name OR phone contains the query string (case-insensitive). */
    public List<Contact> searchContacts(String query) {
        String like = "%" + query + "%";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + TABLE_CONTACTS +
                " WHERE LOWER(" + COL_CONTACT_NAME + ") LIKE LOWER(?)" +
                "    OR " + COL_CONTACT_PHONE + " LIKE ?" +
                " ORDER BY " + COL_CONTACT_NAME + " ASC",
                new String[]{like, like});
        return cursorToContacts(c);
    }

    /** Return only contacts that are currently online. */
    public List<Contact> getOnlineContacts() {
        return queryContacts(COL_CONTACT_ONLINE + "=1", null, COL_CONTACT_NAME + " ASC");
    }

    /** Return only contacts marked as favorites. */
    public List<Contact> getFavoriteContacts() {
        return queryContacts(COL_CONTACT_FAVORITE + "=1", null, COL_CONTACT_NAME + " ASC");
    }

    /** Return a single contact by ID, or null if not found. */
    public Contact getContactById(long id) {
        List<Contact> list = queryContacts(
                COL_CONTACT_ID + "=?", new String[]{String.valueOf(id)}, null);
        return list.isEmpty() ? null : list.get(0);
    }

    /** Return a contact matching the given phone number, or null. */
    public Contact getContactByPhone(String phone) {
        List<Contact> list = queryContacts(
                COL_CONTACT_PHONE + "=?", new String[]{phone}, null);
        return list.isEmpty() ? null : list.get(0);
    }

    /** Return total number of contacts in the database. */
    public int getContactCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CONTACTS, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    /** Return number of online contacts. */
    public int getOnlineContactCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_CONTACTS + " WHERE " + COL_CONTACT_ONLINE + "=1",
                null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    //call logs — Write operations

    /**
     * Insert a call log entry.
     *
     * @param contactId  ID of the linked contact (-1 if unknown/deleted)
     * @param name       Display name
     * @param phone      Phone number
     * @param callType   RecentCall.TYPE_INCOMING / TYPE_OUTGOING / TYPE_MISSED
     * @param timestamp  Epoch millis of the call
     * @param duration   Duration in seconds (0 for missed calls)
     * @param label      "Mobile", "Home", "Work", etc.
     * @return new row ID, or -1 on failure
     */
    public long insertCall(long contactId, String name, String phone,
                           int callType, long timestamp, int duration, String label) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (contactId >= 0) cv.put(COL_CALL_CONTACT_ID, contactId);
        cv.put(COL_CALL_NAME,      name);
        cv.put(COL_CALL_PHONE,     phone);
        cv.put(COL_CALL_TYPE,      callType);
        cv.put(COL_CALL_TIMESTAMP, timestamp);
        cv.put(COL_CALL_DURATION,  duration);
        cv.put(COL_CALL_LABEL,     label);
        return db.insert(TABLE_CALL_LOGS, null, cv);
    }

    /** Delete a single call log entry by ID. */
    public int deleteCall(long callId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_CALL_LOGS,
                COL_CALL_ID + "=?", new String[]{String.valueOf(callId)});
    }

    /** Delete all call logs for a specific contact. */
    public int deleteCallsByContact(long contactId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_CALL_LOGS,
                COL_CALL_CONTACT_ID + "=?", new String[]{String.valueOf(contactId)});
    }

    /** Remove all call log entries. */
    public void clearAllCalls() {
        getWritableDatabase().delete(TABLE_CALL_LOGS, null, null);
    }

    //call logs— Read operations

    /** Return all call logs ordered by most-recent first. */
    public List<RecentCall> getAllCalls() {
        return queryCalls(null, null);
    }

    /** Return call logs filtered by type (incoming / outgoing / missed). */
    public List<RecentCall> getCallsByType(int callType) {
        return queryCalls(COL_CALL_TYPE + "=?", new String[]{String.valueOf(callType)});
    }

    /** Return call logs for a specific contact (by contact_id FK). */
    public List<RecentCall> getCallsByContact(long contactId) {
        return queryCalls(COL_CALL_CONTACT_ID + "=?", new String[]{String.valueOf(contactId)});
    }

    /** Return only missed calls. */
    public List<RecentCall> getMissedCalls() {
        return getCallsByType(RecentCall.TYPE_MISSED);
    }

    /** Return only incoming calls. */
    public List<RecentCall> getIncomingCalls() {
        return getCallsByType(RecentCall.TYPE_INCOMING);
    }

    /** Return only outgoing calls. */
    public List<RecentCall> getOutgoingCalls() {
        return getCallsByType(RecentCall.TYPE_OUTGOING);
    }

    /** Return the count of missed calls. */
    public int getMissedCallCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_CALL_LOGS +
                " WHERE " + COL_CALL_TYPE + "=" + RecentCall.TYPE_MISSED, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    /**
     * Return the N most recent calls for a contact (by phone number).
     * Useful for showing call history in a detail view.
     */
    public List<RecentCall> getRecentCallsByPhone(String phone, int limit) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + TABLE_CALL_LOGS +
                " WHERE " + COL_CALL_PHONE + "=?" +
                " ORDER BY " + COL_CALL_TIMESTAMP + " DESC" +
                " LIMIT " + limit,
                new String[]{phone});
        return cursorToRecentCalls(c);
    }

    /**
     * Return calls within a timestamp range (epoch millis).
     * Useful for filtering "today", "this week", etc.
     */
    public List<RecentCall> getCallsInRange(long fromMillis, long toMillis) {
        return queryCalls(
                COL_CALL_TIMESTAMP + " BETWEEN ? AND ?",
                new String[]{String.valueOf(fromMillis), String.valueOf(toMillis)});
    }

    //advanced queries
    /**
     * Return contacts who have at least one missed call.
     * Uses a JOIN between contacts and call_logs.
     */
    public List<Contact> getContactsWithMissedCalls() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT c.* FROM " + TABLE_CONTACTS + " c " +
                "INNER JOIN " + TABLE_CALL_LOGS + " cl ON c." + COL_CONTACT_ID +
                "    = cl." + COL_CALL_CONTACT_ID +
                " WHERE cl." + COL_CALL_TYPE + " = " + RecentCall.TYPE_MISSED +
                " ORDER BY c." + COL_CONTACT_NAME + " ASC",
                null);
        return cursorToContacts(c);
    }

    /**
     * Return a raw Cursor for a full-text search across both tables.
     * Matches contacts whose name/phone appears in call logs.
     */
    public Cursor searchCallLogs(String query) {
        String like = "%" + query + "%";
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_CALL_LOGS +
                " WHERE LOWER(" + COL_CALL_NAME  + ") LIKE LOWER(?)" +
                "    OR " + COL_CALL_PHONE + " LIKE ?" +
                " ORDER BY " + COL_CALL_TIMESTAMP + " DESC",
                new String[]{like, like});
    }

    //helpers — Private
    private List<Contact> queryContacts(String selection, String[] selectionArgs, String orderBy) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_CONTACTS, null, selection, selectionArgs,
                null, null, orderBy);
        return cursorToContacts(c);
    }

    private List<Contact> cursorToContacts(Cursor c) {
        List<Contact> list = new ArrayList<>();
        if (c == null) return list;
        try {
            int idxId       = c.getColumnIndexOrThrow(COL_CONTACT_ID);
            int idxName     = c.getColumnIndexOrThrow(COL_CONTACT_NAME);
            int idxPhone    = c.getColumnIndexOrThrow(COL_CONTACT_PHONE);
            int idxOnline   = c.getColumnIndexOrThrow(COL_CONTACT_ONLINE);
            int idxFavorite = c.getColumnIndexOrThrow(COL_CONTACT_FAVORITE);
            while (c.moveToNext()) {
                list.add(new Contact(
                        c.getLong(idxId),
                        c.getString(idxName),
                        c.getString(idxPhone),
                        c.getInt(idxOnline)   == 1,
                        c.getInt(idxFavorite) == 1
                ));
            }
        } finally {
            c.close();
        }
        return list;
    }

    private List<RecentCall> queryCalls(String selection, String[] selectionArgs) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_CALL_LOGS, null, selection, selectionArgs,
                null, null, COL_CALL_TIMESTAMP + " DESC");
        return cursorToRecentCalls(c);
    }

    private List<RecentCall> cursorToRecentCalls(Cursor c) {
        List<RecentCall> list = new ArrayList<>();
        if (c == null) return list;
        try {
            int idxId        = c.getColumnIndexOrThrow(COL_CALL_ID);
            int idxName      = c.getColumnIndexOrThrow(COL_CALL_NAME);
            int idxPhone     = c.getColumnIndexOrThrow(COL_CALL_PHONE);
            int idxType      = c.getColumnIndexOrThrow(COL_CALL_TYPE);
            int idxTimestamp = c.getColumnIndexOrThrow(COL_CALL_TIMESTAMP);
            int idxDuration  = c.getColumnIndexOrThrow(COL_CALL_DURATION);
            int idxLabel     = c.getColumnIndexOrThrow(COL_CALL_LABEL);
            while (c.moveToNext()) {
                list.add(new RecentCall(
                        c.getLong(idxId),
                        c.getString(idxName),
                        c.getString(idxPhone),
                        c.getInt(idxType),
                        c.getLong(idxTimestamp),
                        c.getInt(idxDuration),
                        c.getString(idxLabel)
                ));
            }
        } finally {
            c.close();
        }
        return list;
    }

    //seed data — Mirrors original mock data

    private void seedData(SQLiteDatabase db) {
        // Insert contacts — same set as the original mock data
        long[] ids = new long[23];
        ids[0]  = insertContact(db, "Ahmad Karimi",      "415-203-9821", true,  true);
        ids[1]  = insertContact(db, "Alex Lipshutz",     "323-858-4856", true,  true);
        ids[2]  = insertContact(db, "Alexander Torff",   "559-401-9243", false, false);
        ids[3]  = insertContact(db, "Ann Bator",         "212-200-4402", false, false);
        ids[4]  = insertContact(db, "Bennett Lipshutz",  "760-483-3097", true,  false);
        ids[5]  = insertContact(db, "Braxton Culhane",   "555-019-9238", false, false);
        ids[6]  = insertContact(db, "Britney Siphron",   "707-874-5941", true,  true);
        ids[7]  = insertContact(db, "Cristofer Ekstrom", "891-234-5507", true,  false);
        ids[8]  = insertContact(db, "Diana Vaccaro",     "302-112-6671", false, true);
        ids[9]  = insertContact(db, "Donovan Bergson",   "619-774-3390", false, false);
        ids[10] = insertContact(db, "Emerson Dokidis",   "480-903-2245", true,  false);
        ids[11] = insertContact(db, "Giana Kenter",      "530-887-4402", false, false);
        ids[12] = insertContact(db, "Jaxson Calzoni",    "213-445-7781", true,  false);
        ids[13] = insertContact(db, "Jordan Siphron",    "734-221-8849", false, false);
        ids[14] = insertContact(db, "Kierra Geidt",      "603-598-3301", true,  false);
        ids[15] = insertContact(db, "Kadin Lipshutz",    "857-334-9012", false, false);
        ids[16] = insertContact(db, "Livia Bator",       "503-762-1188", false, false);
        ids[17] = insertContact(db, "Marcus Workman",    "312-004-8823", true,  true);
        ids[18] = insertContact(db, "Maren Mango",       "719-006-5431", false, false);
        ids[19] = insertContact(db, "Reagan Levin",      "206-334-0921", false, false);
        ids[20] = insertContact(db, "Sienna Saris",      "917-442-6670", true,  true);
        ids[21] = insertContact(db, "Terry Culhane",     "702-885-1234", true,  false);
        ids[22] = insertContact(db, "Tiana Bergson",     "415-667-3310", false, false);

        // Seed call log entries (timestamps are offsets from "now" for realism)
        long now = System.currentTimeMillis();
        long min  = 60_000L;
        long hour = 3_600_000L;
        long day  = 86_400_000L;

        insertCall(db, ids[3],  "Ann Bator",       "212-200-4402", RecentCall.TYPE_INCOMING, now - 10 * min,    600, "Mobile");
        insertCall(db, ids[0],  "Ahmad Karimi",    "415-203-9821", RecentCall.TYPE_MISSED,   now - 24 * min,      0, "Mobile");
        insertCall(db, ids[13], "Jordan Siphron",  "734-221-8849", RecentCall.TYPE_OUTGOING, now - hour,         180, "Work");
        insertCall(db, ids[17], "Marcus Workman",  "312-004-8823", RecentCall.TYPE_INCOMING, now - 2 * hour,     420, "Mobile");
        insertCall(db, ids[20], "Sienna Saris",    "917-442-6670", RecentCall.TYPE_MISSED,   now - day,            0, "Mobile");
        insertCall(db, ids[19], "Reagan Levin",    "206-334-0921", RecentCall.TYPE_OUTGOING, now - day,           90, "Mobile");
        insertCall(db, ids[16], "Livia Bator",     "503-762-1188", RecentCall.TYPE_INCOMING, now - 2 * day,      300, "Home");
        insertCall(db, ids[10], "Emerson Dokidis", "480-903-2245", RecentCall.TYPE_MISSED,   now - 4 * day,        0, "Mobile");
        insertCall(db, ids[8],  "Diana Vaccaro",   "302-112-6671", RecentCall.TYPE_OUTGOING, now - 7 * day,      120, "Mobile");
        insertCall(db, ids[6],  "Britney Siphron", "707-874-5941", RecentCall.TYPE_INCOMING, now - 8 * day,      540, "Mobile");
        insertCall(db, ids[21], "Terry Culhane",   "702-885-1234", RecentCall.TYPE_OUTGOING, now - 14 * day,     200, "Mobile");
    }

    /** Internal helper used only during seeding (takes an already-open db). */
    private long insertContact(SQLiteDatabase db, String name, String phone,
                                boolean online, boolean favorite) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CONTACT_NAME,     name);
        cv.put(COL_CONTACT_PHONE,    phone);
        cv.put(COL_CONTACT_ONLINE,   online   ? 1 : 0);
        cv.put(COL_CONTACT_FAVORITE, favorite ? 1 : 0);
        return db.insert(TABLE_CONTACTS, null, cv);
    }

    private long insertCall(SQLiteDatabase db, long contactId, String name, String phone,
                             int type, long timestamp, int duration, String label) {
        ContentValues cv = new ContentValues();
        if (contactId >= 0) cv.put(COL_CALL_CONTACT_ID, contactId);
        cv.put(COL_CALL_NAME,      name);
        cv.put(COL_CALL_PHONE,     phone);
        cv.put(COL_CALL_TYPE,      type);
        cv.put(COL_CALL_TIMESTAMP, timestamp);
        cv.put(COL_CALL_DURATION,  duration);
        cv.put(COL_CALL_LABEL,     label);
        return db.insert(TABLE_CALL_LOGS, null, cv);
    }
}
