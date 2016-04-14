package com.haguapku.wificlient.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by MarkYoung on 15/10/30.
 */
public class WiFiProvider extends ContentProvider{

    public static UriMatcher uriMatcher;
    public DbHelper dbHelper;

    private static final String MIME_DIR_PREFIX = "vnd.android.cursor.dir";

    private static final String MIME_ITEM_AP_LIST = "vnd.wifi.aplist";
    private static final String MIME_ITEM_AP_SPEED = "vnd.wifi.apspeed";
    private static final String MIME_ITEM_AP_PWD = "vnd.wifi.appwd";
    private static final String MINI_ITEM_AP_PWD_LOCAL = "vnd.wifi.appwdlocal";

    private static final int AP_LIST = 1;
    private static final int AP_SPEED = 2;
    private static final int AP_PWD = 3;
    private static final int AP_PWD_LOCAL = 4;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(WiFiDbUtils.AUTHORITY,"aplist",AP_LIST);
        uriMatcher.addURI(WiFiDbUtils.AUTHORITY, "apspeed", AP_SPEED);
        uriMatcher.addURI(WiFiDbUtils.AUTHORITY, "appwd", AP_PWD);
        uriMatcher.addURI(WiFiDbUtils.AUTHORITY, "appwdlocal", AP_PWD_LOCAL);
    }

    private static final String WHERE_BSSID = WiFiDbUtils.AP_BSSID + "=?";

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            switch (uriMatcher.match(uri)) {
                case AP_LIST:
                    cursor = db.query(WiFiDbUtils.TABLE_AP_LIST, projection,
                            selection, selectionArgs, null, null, sortOrder);
                    break;
                case AP_SPEED:
                    cursor = db.query(WiFiDbUtils.TABLE_AP_SPEEDREC, projection,
                            selection, selectionArgs, null, null, sortOrder);
                    break;
                case AP_PWD:
                    cursor = db.query(WiFiDbUtils.TABLE_AP_PWD, projection, selection,
                            selectionArgs, null, null, sortOrder);
                    break;
                case AP_PWD_LOCAL:
                    cursor = db.query(WiFiDbUtils.TABLE_AP_LOCAL_PWD, projection,
                            selection, selectionArgs, null, null, sortOrder);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI" + uri);
            }

            if (cursor != null) {
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
            }
        }catch (SQLiteDiskIOException e){
            e.printStackTrace();
        }catch (SQLiteException e){
            e.printStackTrace();
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        switch (uriMatcher.match(uri)){
            case AP_LIST:
                return MIME_DIR_PREFIX + "/" + MIME_ITEM_AP_LIST;
            case AP_SPEED:
                return MIME_DIR_PREFIX + "/" + MIME_ITEM_AP_SPEED;
            case AP_PWD:
                return MIME_DIR_PREFIX + "/" + MIME_ITEM_AP_PWD;
            case AP_PWD_LOCAL:
                return MIME_DIR_PREFIX + "/" + MINI_ITEM_AP_PWD_LOCAL;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    private long exist(SQLiteDatabase database, String tableName, String bssid){

        Cursor cursor = database.query(tableName,new String[]{"_id"},WHERE_BSSID,toArray(bssid),null,null,null);
        if(cursor != null){
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getLong(0);
                }
            }finally {
                cursor.close();
            }
            return -1;
        }

        return 0;
    }

    private String[] toArray(String... args){
        return args;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if(!values.containsKey(WiFiDbUtils.AP_BSSID)){
            throw new IllegalArgumentException("Have no bssid" + uri);
        }

        String tableName = null;

        switch (uriMatcher.match(uri)) {
            case AP_LIST:
                values.put(WiFiDbUtils.AP_UPDATETIME, System.currentTimeMillis());
                tableName = WiFiDbUtils.TABLE_AP_LIST;
                break;
            case AP_SPEED:
                tableName = WiFiDbUtils.TABLE_AP_SPEEDREC;
                break;
            case AP_PWD:
                tableName = WiFiDbUtils.TABLE_AP_PWD;
                break;
            case AP_PWD_LOCAL:
                tableName = WiFiDbUtils.TABLE_AP_LOCAL_PWD;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Uri uri1 = null;

        String bssid = values.getAsString(WiFiDbUtils.AP_BSSID);
        long rowId = exist(db, tableName, bssid);

        if(rowId != -1){
            db.update(tableName,values,WHERE_BSSID,toArray(bssid));
        }else {
            rowId = db.insert(tableName,null,values);
        }

        if(rowId > 0){
            uri = ContentUris.withAppendedId(uri,rowId);
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int count = 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        switch (uriMatcher.match(uri)) {
            case AP_LIST:
                count = db.delete(WiFiDbUtils.TABLE_AP_LIST, selection,
                        selectionArgs);
                break;
            case AP_SPEED:
                count = db.delete(WiFiDbUtils.TABLE_AP_SPEEDREC, selection,
                        selectionArgs);
                break;
            case AP_PWD:
                count = db.delete(WiFiDbUtils.TABLE_AP_PWD, selection,
                        selectionArgs);
                break;
            case AP_PWD_LOCAL:
                count = db.delete(WiFiDbUtils.TABLE_AP_LOCAL_PWD, selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        return count;

    }

    @Nullable
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int count = 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)){
            case AP_LIST:
                values.put(WiFiDbUtils.AP_UPDATETIME,System.currentTimeMillis());
                count = db.update(WiFiDbUtils.TABLE_AP_LIST,values,selection,selectionArgs);
                break;
            case AP_SPEED:
                count = db.update(WiFiDbUtils.TABLE_AP_SPEEDREC, values, selection,
                        selectionArgs);
                break;
            case AP_PWD:
                count = db.update(WiFiDbUtils.TABLE_AP_PWD, values, selection,
                        selectionArgs);
                break;
            case AP_PWD_LOCAL:
                count = db.update(WiFiDbUtils.TABLE_AP_LOCAL_PWD, values,
                        selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        return count;
    }
}
