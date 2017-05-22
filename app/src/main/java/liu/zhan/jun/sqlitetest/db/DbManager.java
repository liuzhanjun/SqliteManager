package liu.zhan.jun.sqlitetest.db;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;


import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by 刘展俊 on 2017/5/19.
 */

public enum DbManager {
    dbManager {
        @Override
        public DbManager getInstans(Context context) {
            contextWeakReference = new WeakReference<Context>(context);
            return this;
        }
    };

    private DbManager() {
        disposable = new CompositeDisposable();
    }

    public abstract DbManager getInstans(Context context);

    public static final String TAG = "LOGI";
    public WeakReference<Context> contextWeakReference;
    public SQLiteDatabase db;
    public CompositeDisposable disposable;

    /**
     * @param databaseName 数据库的名称 如果只是名称
     *                     数据库在 data/data/packageName/databases下
     *                     如果是绝对地址+数据库名称则在相应的路径下生成数据库
     * @return
     */
    public SQLiteDatabase OpenDb(String databaseName) {
        DatabaseContext context=new DatabaseContext(contextWeakReference.get());
        DbSqliteHelper helper = new DbSqliteHelper(context, databaseName, null, 1);
        db = helper.getWritableDatabase();
        return db;
    }

    /**
     * 创建表
     */
    public void createTable(Class<? extends TableModel> classz) {

        //判断是否已创建该表
        if (DbManager.dbManager.isHasTable(classz.getSimpleName())) {
            return;
        }
        //在子线程中操作
        CreateTable create = new CreateTable(classz, db);
        disposable.add(
                Observable.create(create)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableObserver<Boolean>() {
                            @Override
                            public void onNext(Boolean s) {
                                if (s) {
                                    Log.i(TAG, "onNext: 创建table成功");
                                } else {
                                    Log.i(TAG, "onNext: 创建table失败");
                                }

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.i(TAG, "create table onError: " + e.getMessage());
                            }

                            @Override
                            public void onComplete() {

                            }
                        }));


    }

    /**
     * 判断数据库是否存在该表
     *
     * @param tableName
     * @return
     */
    public boolean isHasTable(String tableName) {
        if (tableName == null) {
            return false;
        }
        String sql = "select count(*) from sqlite_master where type=\"table\"" + " and name=\"" + tableName + "\"";
        Cursor result = db.rawQuery(sql, null);
        boolean has = result.moveToNext();
        if (has) {
            int tb = result.getInt(0);
            return tb == 1 ? true :false;
        }
        return false;
    }


    /**
     * 创建表
     */
    public static class CreateTable implements ObservableOnSubscribe<Boolean> {
        private Class classz;
        private SQLiteDatabase db;

        public CreateTable(Class classz, SQLiteDatabase db) {
            this.classz = classz;
            this.db = db;
        }

        @Override
        public void subscribe(@NonNull ObservableEmitter<Boolean> obe) throws Exception {

            StringBuffer sql = new StringBuffer("create table ");
            String tableName = null;
            try {

                //获得表名
                tableName = classz.getSimpleName();
                sql.append(tableName + " (");
                //获得所有属性
                Field[] fields = classz.getFields();
                for (int i = 0; i < fields.length; i++) {
                    //查看是否有TableField注解
                    boolean tablefile = fields[i].isAnnotationPresent(TableField.class);
//                这个判断表示该属性是否是表结构的字段
                    if (tablefile) {
                        //说明这个属性是字段
                        //获得这个字段的名称 类型 约束
                        boolean fieldType = fields[i].isAnnotationPresent(FieldType.class);
                        if (fieldType) {
                            FieldType mode = fields[i].getAnnotation(FieldType.class);
                            //获得字段名称
                            String fieldName = fields[i].getName();
                            //获得字段类型
                            String value = mode.value();
                            //获得字段约束
                            boolean isConstraint = fields[i].isAnnotationPresent(FieldConstraint.class);
                            if (i > 0) {
                                sql.append(",");
                            }
                            sql.append(fieldName + " " + value);
                            if (isConstraint) {
                                FieldConstraint constraint = fields[i].getAnnotation(FieldConstraint.class);
                                String[] values = constraint.value();
                                for (int j = 0; j < values.length; j++) {
                                    sql.append(" " + values[j]);
                                }


                            }

                        } else {
                            throw new Throwable(fields[i].getName() + "字段类型不明确,请在该字段上加上注解@FieldType");
                        }
                    }

                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                obe.onError(e);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                obe.onError(throwable);
            }


            sql.append(")");
            Log.i(TAG, "createTable: sql=" + sql.toString());

            db.execSQL(sql.toString());

            obe.onNext(isHasTable(tableName));
            obe.onComplete();
        }

        /**
         * 判断数据库是否存在该表
         *
         * @param tableName
         * @return
         */
        public boolean isHasTable(String tableName) {
            if (tableName == null) {
                return false;
            }
            String sql = "select count(*) from sqlite_master where type=\"table\"" + " and name=\"" + tableName + "\"";
            Cursor result = db.rawQuery(sql, null);
            boolean has = result.moveToNext();
            if (has) {
                int tb = result.getInt(0);
                return tb == 1 ? true :false;
            }
            return false;
        }
    }


    /**
     * openhlelper类
     */
    public static class DbSqliteHelper extends SQLiteOpenHelper {

        public DbSqliteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }


    }


    public interface TableModel {

    }


}
