package asa.PasswordSafe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.util.Pair;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class DBWarpper extends SQLiteOpenHelper
{
    private final String COL2 = "SITE";
    private final String COL3 = "NAME";
    private final String COL4 = "PASSWORD";
    private final String TABLE = "DATA";

    public DBWarpper(Context context){
        super(context, "PasswordSafeDB111.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE + " (ID INTEGER PRIMARY KEY, SITE TEXT, NAME TEXT, PASSWORD TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int j) {
        db.execSQL("drop table if exists " + TABLE);
        onCreate(db);
    }

    public boolean insertData(String site, String name, String pass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, site);
        contentValues.put(COL3, name);
        contentValues.put(COL4, pass);
        long result = db.insert(TABLE,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getData(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE + " where " + COL2 + " = '" + name + "'",null);
        return res;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE,null);
        return res;
    }

    public TreeMap<String, Pair<String, String>> selectPart(String str){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE + " where SITE like '" + str + "%'", null);
        TreeMap<String, Pair<String, String>> map = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                int res = s1.toLowerCase().compareTo(s2.toLowerCase());
                if(res == 0) {
                    return s1.compareTo(s2);
                }
                return res;
            }
        });

        while(res.moveToNext()) {
            map.put(res.getString(res.getColumnIndex(COL2)), new Pair<>(res.getString(res.getColumnIndex(COL3)), res.getString(res.getColumnIndex(COL4))));
        }

        return map;
    }

    public void deleteData(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE, "SITE = '" + name + "'", null);
    }

    public TreeMap<String, Pair<String, String>> getAllDataMap() {
        TreeMap<String, Pair<String, String>> map = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                int res = s1.toLowerCase().compareTo(s2.toLowerCase());
                if(res == 0) {
                    return s1.compareTo(s2);
                }
                return res;
            }
        });
        Cursor res = getAllData();

        while(res.moveToNext()) {
            map.put(res.getString(res.getColumnIndex(COL2)), new Pair<>(res.getString(res.getColumnIndex(COL3)), res.getString(res.getColumnIndex(COL4))));
        }

        return map;
    }

    public String changePassword(String oldPass, String newPass){

        TreeMap<String, Pair<String, String>> map = getAllDataMap();

        for (Map.Entry<String, Pair<String, String>> entry : map.entrySet()) {
            String site = entry.getKey();
            String name = entry.getValue().first;
            String pass = AES.decrypt(entry.getValue().second, oldPass);

            deleteData(entry.getKey());
            insertData(site, name, AES.encrypt(pass, newPass));
        }

        deleteData("This");
        insertData("This", "None", AES.encrypt(newPass, newPass));

        return newPass;
    }

}
