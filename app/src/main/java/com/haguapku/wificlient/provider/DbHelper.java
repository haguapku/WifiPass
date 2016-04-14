package com.haguapku.wificlient.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by MarkYoung on 15/10/30.
 */
public class DbHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "wifipass.db";
    public static final int DATABASE_VERSION = 3;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        rebuild(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + WiFiDbUtils.TABLE_AP_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + WiFiDbUtils.TABLE_AP_SPEEDREC);
        db.execSQL("DROP TABLE IF EXISTS " + WiFiDbUtils.TABLE_AP_PWD);
        db.execSQL("DROP TABLE IF EXISTS " + WiFiDbUtils.TABLE_AP_LOCAL_PWD);
        rebuild(db);

    }

    private void rebuild(SQLiteDatabase db){

        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + WiFiDbUtils.TABLE_AP_LIST
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WiFiDbUtils.AP_SSID + " TEXT,"
                + WiFiDbUtils.AP_BSSID + " TEXT UNIQUE,"
                + WiFiDbUtils.AP_LATITUDE + " REAL,"
                + WiFiDbUtils.AP_LONGITUDE + " REAL,"
                + WiFiDbUtils.AP_RSSI + " INTEGER,"
                + WiFiDbUtils.AP_SECURITY + " INTEGER,"
                + WiFiDbUtils.AP_CAPABILITIES+ " TEXT,"
                + WiFiDbUtils.AP_LASTLINKTIME + " LONG,"
                + WiFiDbUtils.AP_UPDATETIME + " LONG)");

        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + WiFiDbUtils.TABLE_AP_SPEEDREC
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WiFiDbUtils.AP_SSID + " TEXT,"
                + WiFiDbUtils.AP_BSSID + " TEXT UNIQUE,"
                + WiFiDbUtils.AP_SPEED + " REAL,"
                + WiFiDbUtils.AP_PORTAL + " TEXT,"
                + WiFiDbUtils.AP_TT + " LONG)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + WiFiDbUtils.TABLE_AP_PWD
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WiFiDbUtils.AP_SSID + " TEXT,"
                + WiFiDbUtils.AP_BSSID + " TEXT UNIQUE," // The BSSID + ":" SECURITY
                + WiFiDbUtils.AP_PWD + " TEXT,"
                + WiFiDbUtils.AP_CSUCCESS_NUM + " INTEGER,"
                + WiFiDbUtils.AP_CFAIL_NUM + " INTEGER,"
                + WiFiDbUtils.AP_SPEED + " INTEGER,"
                + WiFiDbUtils.AP_ISAVAILABLE + " INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + WiFiDbUtils.TABLE_AP_LOCAL_PWD
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WiFiDbUtils.AP_SSID + " TEXT,"
                + WiFiDbUtils.AP_BSSID + " TEXT UNIQUE,"  // The BSSID + ":" SECURITY
                + WiFiDbUtils.AP_PWD + " TEXT)");

        db.execSQL("CREATE  INDEX IF NOT EXISTS aplist_index ON "
                + WiFiDbUtils.TABLE_AP_LIST + " ( " + WiFiDbUtils.AP_BSSID
                + " )");
        db.execSQL("CREATE  INDEX IF NOT EXISTS apseed_index ON "
                + WiFiDbUtils.TABLE_AP_SPEEDREC + " ( " + WiFiDbUtils.AP_BSSID
                + " )");
        db.execSQL("CREATE  INDEX IF NOT EXISTS appwd_index ON "
                + WiFiDbUtils.TABLE_AP_PWD + " ( " + WiFiDbUtils.AP_BSSID
                + " )");
        db.execSQL("CREATE  INDEX IF NOT EXISTS appwdlocal_index ON "
                + WiFiDbUtils.TABLE_AP_LOCAL_PWD + " ( " + WiFiDbUtils.AP_BSSID
                + " )");
    }
}
