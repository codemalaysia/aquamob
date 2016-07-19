package com.polluxlab.aquamob.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.polluxlab.aquamob.models.Form;
import com.polluxlab.aquamob.models.SaveData;
import com.polluxlab.aquamob.utils.AppConst;

import java.util.ArrayList;

/**
 * Created by ARGHA K ROY on 5/24/2015.
 */
public class DBHelper extends SQLiteOpenHelper {

    /* For downloaded forms*/
    public static final String COL_ID = "id";
    public static final String COL_FORM_ID = "form_id";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_FORM_DATA = "form_data";
    public static final String COL_FORM_NAME = "form_name";

    /*For saved forms*/
    public static final String COL_SAVED_FORM_DATA = "form_saved_data";
    public static final String COL_SWAP_DATA_ID = "form_swap_data_id";
    public static final String COL_SAVE_TIME = "form_save_time";

    public DBHelper(Context context) {
        super(context, AppConst.DB_NAME, null, AppConst.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + AppConst.DB_TABLE_DOWNLOADED_FORMS + "("
                + COL_ID + " INTEGER PRIMARY KEY,"
                + COL_FORM_ID + " VARCHAR(10),"
                + COL_FORM_NAME + " VARCHAR(10),"
                + COL_FORM_DATA + " TEXT,"
                + COL_USER_ID + " VARCHAR(255) )";

        db.execSQL(query);

        query = "CREATE TABLE " + AppConst.DB_TABLE_SAVED_FORMS + "("
                + COL_ID + " INTEGER PRIMARY KEY,"
                + COL_FORM_ID + " VARCHAR(10),"
                + COL_SAVE_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COL_SAVED_FORM_DATA + " TEXT,"
                + COL_SWAP_DATA_ID + " TEXT,"
                + COL_USER_ID + " VARCHAR(255) )";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AppConst.DB_TABLE_SAVED_FORMS);
        db.execSQL("DROP TABLE IF EXISTS " + AppConst.DB_TABLE_DOWNLOADED_FORMS);
        // Create tables again
        onCreate(db);
    }

    public void saveDownloadedForm(String formId, String userId, String formName, String formData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FORM_ID, formId);
        values.put(COL_USER_ID, userId);
        values.put(COL_FORM_NAME, formName);
        values.put(COL_FORM_DATA, formData);

        db.insert(AppConst.DB_TABLE_DOWNLOADED_FORMS, null, values);
        db.close();
    }

    public ArrayList<Form> getAllForms(String userId) {
        ArrayList<Form> downLoadedForms = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + AppConst.DB_TABLE_DOWNLOADED_FORMS + " WHERE " + COL_USER_ID + " = '" + userId + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Form form = new Form(cursor.getString(0), cursor.getString(1), cursor.getString(2));
                downLoadedForms.add(form);
            } while (cursor.moveToNext());
        }
        db.close();
        return downLoadedForms;
    }

    public String getFormFields(String formId, String userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(AppConst.DB_TABLE_DOWNLOADED_FORMS, new String[]{COL_FORM_DATA}, COL_FORM_ID + "=? AND " + COL_USER_ID + " =?",
                new String[]{formId, userId}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        String formData = cursor.getString(0);
        db.close();
        return formData;
    }

    public void deleteAllDownloadedForms(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(AppConst.DB_TABLE_DOWNLOADED_FORMS, COL_USER_ID + " = ?", new String[]{userId});
        db.close();
    }

    public void saveUserForm(String formId, String userId, String formData, @Nullable String swapFormDataId) {

        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT  * FROM " + AppConst.DB_TABLE_SAVED_FORMS+" WHERE "
                +COL_FORM_ID+"='"+formId+"' AND "+COL_USER_ID+"='"+userId+"' AND "+COL_SWAP_DATA_ID+"='"+swapFormDataId+"'";

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() > 0){
            ContentValues values = new ContentValues();
            values.put(COL_SAVED_FORM_DATA, formData);
            // updating row
            db.update(AppConst.DB_TABLE_SAVED_FORMS, values, COL_FORM_ID+" = ? AND "+COL_USER_ID + " = ? AND "+COL_SWAP_DATA_ID+" = ?", new String[]{formId,userId,swapFormDataId});
        }else{
            ContentValues values = new ContentValues();
            values.put(COL_FORM_ID, formId);
            values.put(COL_USER_ID, userId);
            values.put(COL_SAVED_FORM_DATA, formData);
            values.put(COL_SWAP_DATA_ID, swapFormDataId);

            db.insert(AppConst.DB_TABLE_SAVED_FORMS, null, values);
        }
        cursor.close();
        db.close();
    }

    public void updateUserForm(String dataId, String formData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_SAVED_FORM_DATA, formData);
        // updating row
        db.update(AppConst.DB_TABLE_SAVED_FORMS, values, COL_ID + " = ?",
                new String[]{dataId});
    }

    /**
     * This method return there are any saved values in database or not
     *
     * @return true if there are any saved form otherwise false
     */
    public boolean hasSavedFormsData() {
        String selectQuery = "SELECT  * FROM " + AppConst.DB_TABLE_SAVED_FORMS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        boolean hasSavedValue = false;
        if (cursor.getCount() > 0) hasSavedValue = true;
        cursor.close();
        db.close();
        return hasSavedValue;
    }

    public ArrayList<SaveData> getAllSavedForms(String userId) {
        ArrayList<SaveData> savedForms = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + AppConst.DB_TABLE_SAVED_FORMS + " WHERE " + COL_USER_ID + " = '" + userId + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                savedForms.add(new SaveData(cursor.getString(0), cursor.getString(1), cursor.getString(3)));
            } while (cursor.moveToNext());
        }
        db.close();
        return savedForms;
    }

    public ArrayList<SaveData> getAllSavedForms(String formId, String userId) {
        ArrayList<SaveData> savedForms = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + AppConst.DB_TABLE_SAVED_FORMS + " WHERE " + COL_FORM_ID + " = " + formId + " AND " + COL_USER_ID + " = '" + userId + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                savedForms.add(new SaveData(cursor.getString(0), cursor.getString(1), cursor.getString(3)));
            } while (cursor.moveToNext());
        }
        db.close();
        return savedForms;
    }

    public void deleteSavedFormDataById(String dataId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(AppConst.DB_TABLE_SAVED_FORMS, COL_ID + " = ?",
                new String[]{String.valueOf(dataId)});
        db.close();
    }

    public void deleteSavedFormDataByFormId(String formId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(AppConst.DB_TABLE_SAVED_FORMS, COL_FORM_ID + " = ? AND " + COL_USER_ID + " = ?",
                new String[]{formId, userId});
        db.close();
    }

    public void deleteAllSavedFormData(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(AppConst.DB_TABLE_SAVED_FORMS, COL_USER_ID + " = ?", new String[]{userId});
        db.close();
    }
}
