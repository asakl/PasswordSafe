package asa.PasswordSafe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.util.Pair;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class DBWarpper extends SQLiteOpenHelper
{
    // create database (C'tor)
    DBWarpper(Context context){
        super(context, Constants.DBName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create the table
        db.execSQL("create table " + Constants.tableName + " (ID INTEGER PRIMARY KEY, "
                + Constants.siteCol + " TEXT, "
                + Constants.nameCol + " TEXT, "
                + Constants.passCol + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int j) {
        // update the table
        db.execSQL("drop table if exists " + Constants.tableName);
        onCreate(db);
    }

    boolean insertData(String site, String name, String pass) {
        // get db
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // put data
        contentValues.put(Constants.siteCol, site);
        contentValues.put(Constants.nameCol, name);
        contentValues.put(Constants.passCol, pass);

        // insert data
        long result = db.insert(Constants.tableName,null ,contentValues);
        return result != -1;
    }

    Cursor getData(String site) {
        // get rows by site
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + Constants.tableName + " where " + Constants.siteCol + " = '" + site + "'",null);
    }

    // get all data
    private Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + Constants.tableName,null);
    }

    // comparator by lowercase index
    private class myCmp implements Comparator<String>{
        @Override
        public int compare(String s1, String s2) {
            int res = s1.toLowerCase().compareTo(s2.toLowerCase());
            if(res == 0) {
                return s1.compareTo(s2);
            }
            return res;
        }
    }


    // get part of the data
    TreeMap<String, Pair<String, String>> selectPart(String str){
        // get db
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + Constants.tableName + " where " + Constants.siteCol + " like '" + str + "%'", null);

        // new try map of sort
        TreeMap<String, Pair<String, String>> map = new TreeMap<>(new myCmp());

        // get all data in map
        while(res.moveToNext()) {
            map.put(res.getString(res.getColumnIndex(Constants.siteCol)),
                    new Pair<>(res.getString(res.getColumnIndex(Constants.nameCol)), res.getString(res.getColumnIndex(Constants.passCol))));
        }

        return map;
    }

    // delete row
    void deleteData(String site) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Constants.tableName, Constants.siteCol + " = '" + site + "'", null);
    }

    // get all data in map
    TreeMap<String, Pair<String, String>> getAllDataMap() {
        return selectPart("");
    }

    // change pass
    void changePassword(String oldPass, String newPass){

        // get all data map
        TreeMap<String, Pair<String, String>> map = getAllDataMap();

        // for all sites - change encryption
        for (Map.Entry<String, Pair<String, String>> entry : map.entrySet()) {
            String site = entry.getKey();
            String name = entry.getValue().first;
            String pass = AES.decrypt(entry.getValue().second, oldPass);

            deleteData(entry.getKey());
            insertData(site, name, AES.encrypt(pass, newPass));
        }

        // change this site password and encryption
        deleteData(Constants.thisApp);
        insertData(Constants.thisApp, Constants.none, AES.encrypt(newPass, newPass));

    }

}
