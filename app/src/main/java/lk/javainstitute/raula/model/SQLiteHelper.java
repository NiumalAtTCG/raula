package lk.javainstitute.raula.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SalonPackages.db";
    private static final int DATABASE_VERSION = 2; // Incremented version to update DB

    // Table: Packages
    private static final String TABLE_PACKAGES = "packages";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_PRICE = "price";

    // Table: Appointments
    private static final String TABLE_APPOINTMENTS = "appointments";
    private static final String COLUMN_APPOINTMENT_ID = "id";
    private static final String COLUMN_APPOINTMENT_DATE = "appointment_date";
    private static final String COLUMN_TIME_SLOT = "time_slot";
    private static final String COLUMN_STATUS = "appointment_Status";

    // Create Packages Table
    private static final String CREATE_TABLE_PACKAGES = "CREATE TABLE " + TABLE_PACKAGES + " (" +
            COLUMN_ID + " TEXT PRIMARY KEY, " +
            COLUMN_NAME + " TEXT, " +
            COLUMN_DESCRIPTION + " TEXT, " +
            COLUMN_PRICE + " REAL)";

    // Create Appointments Table
    private static final String CREATE_TABLE_APPOINTMENTS = "CREATE TABLE " + TABLE_APPOINTMENTS + " (" +
            COLUMN_APPOINTMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_APPOINTMENT_DATE + " TEXT, " +
            COLUMN_TIME_SLOT + " TEXT, " +
            COLUMN_STATUS + " TEXT)";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PACKAGES);
        db.execSQL(CREATE_TABLE_APPOINTMENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) { // Only drop if upgrading from version 1
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
            db.execSQL(CREATE_TABLE_APPOINTMENTS);
        }
    }

    // 🔹 Insert a Package into SQLite
    public boolean insertPackage(String id, String name, String description, double price) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PACKAGES + " WHERE id=?", new String[]{id});
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return false;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_PRICE, price);

        long result = db.insert(TABLE_PACKAGES, null, values);
        db.close();

        return result != -1;
    }

    // 🔹 Insert an Appointment into SQLite
    public boolean insertAppointment(String appointmentDate, String timeSlot, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_APPOINTMENT_DATE, appointmentDate);
        values.put(COLUMN_TIME_SLOT, timeSlot);
        values.put(COLUMN_STATUS, status);

        long result = db.insert(TABLE_APPOINTMENTS, null, values);
        db.close();

        return result != -1;
    }

    // 🔹 Get All Appointments from SQLite
    public Cursor getAllAppointments() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_APPOINTMENTS, null, null, null, null, null, COLUMN_APPOINTMENT_DATE + " ASC");
    }

    // 🔹 Delete All Appointments
    public void clearAppointments() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_APPOINTMENTS);
        db.close();
    }
}
