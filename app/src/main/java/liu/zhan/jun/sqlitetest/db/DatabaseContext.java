package liu.zhan.jun.sqlitetest.db;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.LoginFilter;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by 刘展俊 on 2017/5/22.
 */

public class DatabaseContext extends ContextWrapper {
    public static final String TAG="DatabaseContext";
    public DatabaseContext(Context base) {
        super(base);
    }

    /**
     * 获得数据库路径
     * @param name
     * @return
     */
    @Override
    public File getDatabasePath(String name) {
        //判断name
        if (!name.contains("/")){
            return super.getDatabasePath(name);
        }
        //判断sd卡是否存在
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.i(TAG, "getDatabasePath: SD卡不存在，请加载SD卡");
            return null;
        }else{
            File file=new File(name);
            //判断目录是否存在，不存在则创建
            if (!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
                try {
                    //判断文件是否存在
                    if (!file.exists()) {
                        boolean filecrate = file.createNewFile();
                        if (filecrate) {
                            return file;
                        } else {
                            Log.i(TAG, "getDatabasePath: 数据库文件创建失败");
                        }
                    }else{
                        return  file;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

        return super.getDatabasePath(name);
    }


    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name),null);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name),null);
    }
}
