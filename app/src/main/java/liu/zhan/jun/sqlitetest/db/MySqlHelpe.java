package liu.zhan.jun.sqlitetest.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static liu.zhan.jun.sqlitetest.MainActivity.TAG;


/**
 * Created by 刘展俊 on 2017/5/19.
 */

public class MySqlHelpe extends SQLiteOpenHelper {
    /**
     *
     * @param context 上下文
     * @param name 数据库的名字
     * @param factory 数据工厂
     * @param version 当前版本
     */
    public MySqlHelpe(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MySqlHelpe(Context context){
        super(context,Constant.DATABASENAME,null,Constant.VERSION);
    }

    /**
     * 只有在创建数据库的时候调用一次
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate: ====================");
        String createStudent="create table "+Constant.TABLENAME
                +"(_id Integer primary key,"
                +Constant.name+" varchar(10),age Integer)";
        db.execSQL(createStudent);
    }

    /**
     * 数据库版本更新的时候调用
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade: =========================");


    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.i(TAG, "onOpen: =====================");
    }
}
