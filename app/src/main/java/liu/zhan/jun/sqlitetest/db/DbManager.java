package liu.zhan.jun.sqlitetest.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


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

    private Gson mGson;

    private DbManager() {
        disposable = new CompositeDisposable();
        mGson = new Gson();
    }

    public abstract DbManager getInstans(Context context);

    public static final String TAG = "LOGI";
    public WeakReference<Context> contextWeakReference;
    private SQLiteDatabase db;
    private CompositeDisposable disposable;

    /**
     * @param databaseName 数据库的名称 如果只是名称
     *                     数据库在 data/data/packageName/databases下
     *                     如果是绝对地址+数据库名称则在相应的路径下生成数据库
     * @return
     */
    public SQLiteDatabase OpenDb(String databaseName) {
        DatabaseContext context = new DatabaseContext(contextWeakReference.get());
        DbSqliteHelper helper = new DbSqliteHelper(context, databaseName, null, 1);
        db = helper.getWritableDatabase();
        return db;
    }

    /**
     * 插入数据
     *
     * @param t
     * @param <T>
     * @return
     */
    public <T extends TableModel> void insert(T t, final DbCallBack<Long> callback) {

        //在子线程中插入数据
        InsertTable insert = new InsertTable(db, t, mGson);
        disposable.add((Disposable) Observable.create(insert)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        callback.before();
                    }
                }).subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long o) {
                        callback.success(o);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.failure(e);
                    }

                    @Override
                    public void onComplete() {
                        callback.finish();
                    }
                }));

    }


    /**
     * 更新数据
     *
     * @param t
     * @param whereClause
     * @param whereArgs
     * @param callback
     * @param <T>
     */
    public <T extends TableModel> void update(T t, String whereClause, String[] whereArgs, final DbCallBack<Integer> callback) {

        UpdateTable update = new UpdateTable(db, t, mGson, whereClause, whereArgs);
        disposable.add((Disposable) Observable.create(update).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        callback.before();
                    }
                }).subscribeWith(new DisposableObserver<Integer>() {
                    @Override
                    public void onNext(Integer o) {
                        callback.success(o);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.failure(e);
                    }

                    @Override
                    public void onComplete() {
                        callback.finish();
                    }
                }));


    }


    /**
     * 取消所有订阅
     */
    public void unscribe(){
        disposable.clear();
    }

    /**
     * 清空数据
     * @param t
     */
    public void deleteAll(Class<? extends TableModel> t,DbCallBack<Integer> callback){
        delete(t,null,null,callback);
    }
    /**
     * 删除数据
     *
     * @param t
     * @param whereClause
     * @param whereArgs
     * @param callback
     */
    public void delete(Class<? extends TableModel> t, String whereClause, String[] whereArgs, final DbCallBack<Integer> callback) {

        DeleteTable delete = new DeleteTable(db, t, whereClause, whereArgs);
        disposable.add((Disposable) Observable.create(delete).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        callback.before();
                    }
                }).subscribeWith(new DisposableObserver<Integer>() {
                    @Override
                    public void onNext(Integer o) {
                        callback.success(o);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.failure(e);
                    }

                    @Override
                    public void onComplete() {
                        callback.finish();
                    }
                }));
    }


    /**
     * 创建数据表
     *
     * @param classz
     * @see TableModel
     * @see TableField
     * @see FieldType
     * @see FieldConstraint
     */
    public <T> void createTable(Class<? extends TableModel> classz, final DbCallBack<Boolean> callback) {

        //判断是否已创建该表
        if (DbManager.dbManager.isHasTable(classz.getSimpleName())) {
            callback.failure(new Throwable("该表已创建"));
            return;
        }
        //在子线程中操作
        CreateTable create = new CreateTable(classz, db);
        disposable.add(
                Observable.create(create)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                callback.before();
                            }
                        })
                        .subscribeWith(new DisposableObserver<Boolean>() {
                            @Override
                            public void onNext(Boolean s) {
                                callback.success(s);

                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.failure(e);
                                Log.i(TAG, "create table onError: " + e.getMessage());
                            }

                            @Override
                            public void onComplete() {
                                callback.finish();
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
     * 更新数据
     *
     * @param <T>
     */
    public static class UpdateTable<T extends TableModel> implements ObservableOnSubscribe<Integer> {
        private SQLiteDatabase db;
        private T t;
        private Gson mGson;
        private String whereClause;
        private String[] whereArgs;

        public UpdateTable(SQLiteDatabase db, T t, Gson mGson, String whereClause, String[] whereArgs) {
            this.db = db;
            this.t = t;
            this.mGson = mGson;
            this.whereClause = whereClause;
            this.whereArgs = whereArgs;
        }

        @Override
        public void subscribe(@NonNull ObservableEmitter<Integer> obe) throws Exception {
            try {
                HashMap<String,String> maps = GsonUtils.jsonToMap(t, mGson);
                Set<String> keySet = maps.keySet();
                Iterator<String> iterator = keySet.iterator();
                Class<?> classz = Class.forName(t.getClass().getName());
                ContentValues values = new ContentValues();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    Field field = classz.getField(key);
                    boolean has = field.isAnnotationPresent(TableField.class);
                    String value = maps.get(key);
                    if (has) {
                        Log.i(TAG, "subscribe: key=" + key + "|value=" + value);
                        values.put(key, value);
                    }
                }

                int row = db.update(t.getClass().getSimpleName(), values, whereClause, whereArgs);
                if (row != -1) {
                    obe.onNext(row);
                    obe.onComplete();
                } else {
                    obe.onError(new Throwable(" Error update row=" + row));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                obe.onError(e);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                obe.onError(e);
            }

        }
    }

    /**
     * 删除数据
     */
    public static class DeleteTable implements ObservableOnSubscribe<Integer> {
        private SQLiteDatabase db;
        private Class<? extends TableModel> t;
        private String whereClause;
        private String[] whereArgs;

        public DeleteTable(SQLiteDatabase db, Class<? extends TableModel> t, String whereClause, String[] whereArgs) {
            this.db = db;
            this.t = t;
            this.whereClause = whereClause;
            this.whereArgs = whereArgs;
        }

        @Override
        public void subscribe(@NonNull ObservableEmitter<Integer> obe) throws Exception {
            int row = db.delete(t.getSimpleName(), whereClause, whereArgs);
            obe.onNext(row);
            obe.onComplete();


        }
    }

    /**
     * 插入数据
     *
     * @param <T>
     */
    public static class InsertTable<T extends TableModel> implements ObservableOnSubscribe<Long> {
        private SQLiteDatabase db;
        private T t;
        private Gson mGson;

        public InsertTable(SQLiteDatabase db, T t, Gson mGson) {
            this.db = db;
            this.t = t;
            this.mGson = mGson;
        }

        @Override
        public void subscribe(@NonNull ObservableEmitter<Long> obe) throws Exception {
            try {
                HashMap<String,String> maps = GsonUtils.jsonToMap(t, mGson);
                Set<String> keySet = maps.keySet();
                Iterator<String> iterator = keySet.iterator();
                Class<?> classz = Class.forName(t.getClass().getName());
                ContentValues values = new ContentValues();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    Field field = classz.getField(key);
                    boolean has = field.isAnnotationPresent(TableField.class);
                    String value = maps.get(key);
                    if (has) {
                        Log.i(TAG, "subscribe: key=" + key + "|value=" + value);
                        values.put(key, value);
                    }
                }

                long row = db.insert(t.getClass().getSimpleName(), null, values);
                if (row != -1) {
                    obe.onNext(row);
                    obe.onComplete();
                } else {
                    obe.onError(new Throwable(" Error insert row=" + row));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                obe.onError(e);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                obe.onError(e);
            }


        }
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
            if (isHasTable(tableName)) {
                obe.onNext(true);
            } else {
                obe.onError(new Throwable("已经存在该表"));
            }

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