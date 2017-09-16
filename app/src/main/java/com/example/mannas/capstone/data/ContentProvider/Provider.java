package com.example.mannas.capstone.data.ContentProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Mannas on 8/24/2017.
 */

public class Provider extends ContentProvider{
    DB_Helper mDBHelper;
    static final String LOG_TAG = Provider.class.getName();
    @Override
    public boolean onCreate() {
        mDBHelper = new DB_Helper(getContext());
        return true;
    }

    /**
     *
     * @param uri The URI, using the content:// scheme, for the content to retrieve.
     * @param projection String: A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @param selection String: A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given URI.
     * @param selectionArgs String: You may include ?s in selection, which will be replaced by the values from selectionArgs, in the order that they appear in the selection. The values will be bound as Strings.
     * @param sortOrder String: How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort order, which may be unordered.
     * @return
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase DB = mDBHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (Contract.mUriMatcher.match(uri)) {

            case Contract.TableIdentifier.ID_SubjectResponse:
                cursor = DB.query(Contract.SubjectResponse.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case Contract.TableIdentifier.ID_BookDetailResponse:
                cursor = DB.query(Contract.BookDetailResponse.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case Contract.TableIdentifier.ID_SubjectList:
                cursor = DB.query(Contract.SubjectList.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        switch (Contract.mUriMatcher.match(uri)) {
            case Contract.TableIdentifier.ID_SubjectResponse:
                mDBHelper.getWritableDatabase().insert(Contract.SubjectResponse.TABLE_NAME,null,contentValues);
                break;
            case Contract.TableIdentifier.ID_BookDetailResponse:
                mDBHelper.getWritableDatabase().insert(Contract.BookDetailResponse.TABLE_NAME,null,contentValues);
                break;
            case Contract.TableIdentifier.ID_SubjectList:
                mDBHelper.getWritableDatabase().insert(Contract.SubjectList.TABLE_NAME,null,contentValues);
                break;
        }
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String whereClauses, @Nullable String[] whereArgs) {
        switch (Contract.mUriMatcher.match(uri)) {
            case Contract.TableIdentifier.ID_SubjectResponse:
                return mDBHelper.getWritableDatabase().delete(Contract.SubjectResponse.TABLE_NAME,whereClauses,whereArgs);
            case Contract.TableIdentifier.ID_BookDetailResponse:
                return  mDBHelper.getWritableDatabase().delete(Contract.BookDetailResponse.TABLE_NAME,whereClauses,whereArgs);
            case Contract.TableIdentifier.ID_SubjectList:
                return  mDBHelper.getWritableDatabase().delete(Contract.SubjectList.TABLE_NAME,whereClauses,whereArgs);

        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,@Nullable String whereClauses, @Nullable String[] whereArgs) {
        switch (Contract.mUriMatcher.match(uri)) {
            case Contract.TableIdentifier.ID_SubjectResponse:
                return mDBHelper.getWritableDatabase().update(Contract.SubjectResponse.TABLE_NAME,contentValues,whereClauses,whereArgs);

            case Contract.TableIdentifier.ID_BookDetailResponse:
                return mDBHelper.getWritableDatabase().update(Contract.BookDetailResponse.TABLE_NAME,contentValues,whereClauses,whereArgs);

            case Contract.TableIdentifier.ID_SubjectList:
                return mDBHelper.getWritableDatabase().update(Contract.SubjectList.TABLE_NAME,contentValues,whereClauses,whereArgs);

        }
        return 0;
    }

    static class DB_Helper  extends SQLiteOpenHelper {

        static final String DB_NAME = "movieApp_DB";
        static final int DB_Version = 1;
        static final String LOG_TAG = DB_Helper.class.getName();

        DB_Helper(Context context ) {
            super(context, DB_NAME , null, DB_Version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(  Contract.BookDetailResponse.CreationSQL_DDL);
            db.execSQL(  Contract.SubjectResponse.CreationSQL_DDL);
            db.execSQL(  Contract.SubjectList.CreationSQL_DDL);
            Log.i(LOG_TAG,"A new SQL DataBase is deployed !");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if(oldVersion != newVersion){
                db.execSQL(  Contract.BookDetailResponse.DropSQL_DDL);
                db.execSQL(  Contract.SubjectResponse.DropSQL_DDL);
                db.execSQL(  Contract.SubjectList.DropSQL_DDL);
                Log.i(LOG_TAG,"The Old SQL DataBase is Dropped !");
                onCreate(db);
            }
        }


    }
}
