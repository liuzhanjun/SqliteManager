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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
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
    private DbSqliteHelper helper;

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
        helper = new DbSqliteHelper(context, databaseName, null, 1);
        db = helper.getWritableDatabase();
        return db;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public boolean checkOpen(String path){
       OpenDb(path);
        return false;
    }

    /**
     * 只有条件的查询
     * @param t
     * @param whereClause
     * @param whereArgs
     * @param callback
     * @param <T>
     */
    public <T extends TableModel> void query(final T t, String whereClause, String[] whereArgs, final DbCallBack<List<T>> callback) {
        this.query(false,null,t,whereClause,whereArgs,null,null,null,callback);
    }

    /**
     * 无条件查询
     * @param t
     * @param callback
     * @param <T>
     */
    public <T extends TableModel> void query(final T t, final DbCallBack<List<T>> callback) {
        this.query(false,null,t,null,null,null,null,null,callback);
    }

    /**
     * sql查询 例如select count (_id) as sizes from CashPayDbInfo
     */
    public <T extends TableModel> void query(String sql,String [] whereArgs, final DbCallBack<List<T>> callback) {
        this.query(true,sql,null,null,whereArgs,null,null,null,callback);
    }

    /**
     * 查询排序
     * @param t
     * @param orderBy
     * @param callback
     * @param <T>
     */
    public <T extends TableModel> void query(final T t,String orderBy, final DbCallBack<List<T>> callback) {
        this.query(false,null,t,null,null,null,null,orderBy,callback);
    }
    /**
     * 查询
     *
     * @param t
     * @param callback
     * @param <T>
     */
    public <T extends TableModel> void query(boolean isfuction,String fuction,final T t, String whereClause, String[] whereArgs, String groupBy, String having, String orderBy, final DbCallBack<List<T>> callback) {

        QuerySubscrib query = new QuerySubscrib(
                db,
                t,
                mGson,
                whereClause,
                whereArgs,
                groupBy, having, orderBy,fuction,isfuction);

        disposable.add((Disposable) Observable.create(query)
                .subscribeOn(Schedulers.io())
                .map(new Function<Cursor,List<T>>() {
                    @Override
                    public List<T> apply(@NonNull Cursor o) throws Exception {
                        //查询到有多少条数据
                        int count = o.getCount();
                        Log.i(TAG, "apply: 有" + count + "条数据");
                        if (count < 1) {
                            return null;
                        }
                        //每条数据有多少个字段
                        int columnCount = o.getColumnCount();
                        Log.i(TAG, "apply: columnCount===" + columnCount);
                        List<T> results = null;
                        StringBuffer json = new StringBuffer("[");
                        //逐条读取每条数据
                        while (!o.isLast()) {
                            //游标移动到下一行
                            boolean has = o.moveToNext();
                            if (has) {

                                //说明这行有数据

                                json.append("{");
                                for (int i = 0; i < columnCount; i++) {

                                    String columnName = o.getColumnName(i);
                                    int type = o.getType(i);

                                    switch (type) {
                                        case Cursor.FIELD_TYPE_STRING:
                                            String stringValue = o.getString(i);
                                            int arrayTag=checkObj(stringValue);
                                            Log.i("DB", "columnName="+columnName+"value="+stringValue);
                                            if (arrayTag==0){
                                                json.append("\""+columnName + "\":" + stringValue+",");
                                            }else {
                                                json.append("\""+columnName + "\":\"" + stringValue + "\",");
                                            }

                                            break;
                                        case Cursor.FIELD_TYPE_INTEGER:
                                            int intValue = o.getInt(i);
                                            json.append("\""+columnName + "\":" + intValue+",");
                                            break;
                                        case Cursor.FIELD_TYPE_FLOAT:
                                            float floatValue = o.getFloat(i);
//
                                            json.append("\""+columnName + "\":" + floatValue+",");
//
                                            break;
                                    }

                                }
                                if (o.isLast()) {
                                    if (json.lastIndexOf(",")==json.length()-1) {
                                        json.deleteCharAt(json.length()-1);
                                    }
                                    json.append("}]");
                                } else {
                                    if (json.lastIndexOf(",")==json.length()-1) {
                                        json.deleteCharAt(json.length()-1);
                                    }
                                    json.append("},");
                                }


                            }
                        }
                        Log.i("DB", "json ="+json.toString());
                        return mGson.fromJson(json.toString(), callback.getmType());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        callback.before();
                    }
                })
                .subscribeWith(new DisposableObserver<List<T>>() {
                    @Override
                    public void onNext(List<T> o) {
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

    private int checkObj(String stringValue) {
        if (stringValue!=null&&!stringValue.equals("")) {
            String index = stringValue.substring(0, 1);
            Log.i(TAG, "checkObj: index=" + index);
            if (index.equals("{") || index.equals("[")) {
                return 0;
            }
        }else{
            return 1;
        }
        return 1;
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
    public void unscribe() {
        disposable.clear();
    }

    public static class QuerySubscrib<T extends TableModel> implements ObservableOnSubscribe<Cursor> {
        private SQLiteDatabase db;
        private T t;
        private Gson mGson;
        private String whereClause;
        private String[] whereArgs;
        private String groupBy;
        private String having;
        private String orderBy;
        private String fuction;
        private boolean isfuction;


        public QuerySubscrib(SQLiteDatabase db, T t, Gson mGson, String whereClause, String[] whereArgs, String groupBy, String having, String orderBy,String fuction,boolean isfuction) {
            this.db = db;
            this.t = t;
            this.mGson = mGson;
            this.whereClause = whereClause;
            this.whereArgs = whereArgs;
            this.groupBy = groupBy;
            this.having = having;
            this.orderBy = orderBy;
            this.fuction=fuction;
            this.isfuction=isfuction;
        }

        @Override
        public void subscribe(@NonNull ObservableEmitter<Cursor> e) throws Exception {

            Log.i(TAG, "subscribe: id=" + Thread.currentThread().getId());
            Cursor result = null;//排序
            if (!isfuction) {
                try {
                    ArrayList<String> keys = new ArrayList<String>();
                    HashMap<String, String> maps = GsonUtils.jsonToMap(t, mGson);
                    Set<String> keySet = maps.keySet();
                    Iterator<String> iterator = keySet.iterator();
                    Class<?> classz = Class.forName(t.getClass().getName());

                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        Field field = classz.getDeclaredField(key);
                        boolean has = field.isAnnotationPresent(TableField.class);
                        String value = maps.get(key);
                        if (has) {
                            keys.add(key);
                        }
                    }

                    String[] columns = new String[keys.size()];
                    for (int i = 0; i < keys.size(); i++) {
                        columns[i] = keys.get(i);
                    }
                    result = db.query(t.getClass().getSimpleName(),
                            columns,//要查询的字段
                            whereClause,//查询的条件
                            whereArgs,//上面？的值
                            groupBy,//分组
                            having,//分组后的条件
                            orderBy);//排序
                } catch (Exception e1) {
                    e1.printStackTrace();
                    e.onError(e1);
                }
                if (result!=null) {
                    e.onNext(result);
                    e.onComplete();
                }
            }else {
                //函数条件查询
                result = db.rawQuery(fuction,whereArgs);
                if (result!=null) {
                    e.onNext(result);
                    e.onComplete();
                }


            }



        }
    }

    /**
     * 清空数据
     *
     * @param t
     */
    public void deleteAll(Class<? extends TableModel> t, DbCallBack<Integer> callback) {
        delete(t, null, null, callback);
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
     * 更新表
     * @param classz
     * @see TableModel
     * @see TableField
     * @see FieldType
     * @see FieldConstraint
     */
    public <T> void AlterTable(final Class<? extends TableModel> classz, final DbCallBack<Boolean> callback) {
        //判断是否已创建该表
        if (DbManager.dbManager.isHasTable(classz.getSimpleName())) {
            //如果创建了该表才能去更新
            queryTableStruct querystruct=new queryTableStruct(classz,db);
            disposable.add(
                    Observable.create(querystruct)
                            .subscribeOn(Schedulers.io())
                            .flatMap(new Function<ConcurrentSkipListSet<String>, ObservableSource<Boolean>>() {
                                @Override
                                public ObservableSource<Boolean> apply(@NonNull ConcurrentSkipListSet<String> s) throws Exception {
                                    return new AlterTableSub(classz,db,s);
                                }
                            })
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
                                    //

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
                    Field field = classz.getDeclaredField(key);
                    boolean has = field.isAnnotationPresent(TableField.class);
                    String value = maps.get(key);
                    if (has) {
                        Log.i(TAG, "subscribe: key=" + key + "|value=" + value);
                        int start=value.indexOf("\"");
                        int end=value.lastIndexOf("\"");

                        if (start==0&&end==value.length()-1){
                            value=value.substring(start+1,end);
                        }
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
                    Field field = classz.getDeclaredField(key);
                    boolean has = field.isAnnotationPresent(TableField.class);
                    String value = maps.get(key);

                    if (has) {
//
                        int start=value.indexOf("\"");
                        int end=value.lastIndexOf("\"");

                        if (start==0&&end==value.length()-1){
                            value=value.substring(start+1,end);
                        }
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
     * 查询表结构
     */
    public static class queryTableStruct implements ObservableOnSubscribe<ConcurrentSkipListSet<String>>{
        private Class classz;
        private SQLiteDatabase db;
        public queryTableStruct(Class classz, SQLiteDatabase db) {
            this.classz = classz;
            this.db = db;
        }
        @Override
        public void subscribe(@NonNull ObservableEmitter< ConcurrentSkipListSet<String>> e) throws Exception {
            String sql="PRAGMA table_info ("+classz.getSimpleName()+")";
            Cursor cursor=db.rawQuery(sql,null);
            ConcurrentSkipListSet<String> allColumn=new ConcurrentSkipListSet<String>();
            while (!cursor.isLast()){
                boolean has=cursor.moveToNext();
                if (has) {
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    allColumn.add(name);
                }
            }
            e.onNext(allColumn);
            e.onComplete();

        }
    }

    /**
     * 更新表结构
     */
    public static class AlterTableSub implements  ObservableSource<Boolean>{
        private Class classz;
        private SQLiteDatabase db;
        private ConcurrentSkipListSet<String> columnName;//旧表原有的字段

        public AlterTableSub(Class classz, SQLiteDatabase db,ConcurrentSkipListSet<String> columnName) {
            this.classz = classz;
            this.db = db;
            this.columnName=columnName;
        }

        @Override
        public void subscribe(@NonNull Observer<? super Boolean> observer) {
            //先判断新表的字段
            //获得所有属性

            Field[] fields = classz.getDeclaredFields();
            ConcurrentSkipListMap<String,FieldStatus> newColumn= getNewColumn(fields,columnName);
            if (newColumn.size()<=0){
                observer.onError(new Throwable("没有需要添加的字段"));
                return;
            }
            Iterator iterator=newColumn.keySet().iterator();
            while (iterator.hasNext()) {
//                alter table CashPayDbInfo add  alls varchar(25)iterator.next()
                String key= (String) iterator.next();
                FieldStatus value=newColumn.get(key);
                StringBuilder sql=new StringBuilder("alter table "+classz.getSimpleName()+" add COLUMN "+key+" "+value._type);
                if (value.getFieldConstraint()!=null) {
                    for (int i = 0; i < value.getFieldConstraint().length; i++) {
                        sql.append(" " + value.getFieldConstraint()[i]);
                    }
                }

                db.execSQL(sql.toString());
            }
            observer.onNext(true);
            observer.onComplete();

        }

        /**
         * 获得要新添加的字段
         * @param fields
         * @return
         */
        private ConcurrentSkipListMap<String,FieldStatus> getNewColumn(Field[] fields, ConcurrentSkipListSet<String> oldColumn) {
            ConcurrentSkipListMap<String,FieldStatus> allColumn=new ConcurrentSkipListMap<String,FieldStatus>();
            for (int i = 0; i < fields.length; i++) {
                //查看是否有TableField注解
                boolean tablefile = fields[i].isAnnotationPresent(TableField.class);
                //这个判断表示该属性是否是表结构的字段
                if (tablefile) {
                    FieldType mode = fields[i].getAnnotation(FieldType.class);
                    //获得字段名称
                    String fieldName = fields[i].getName();
                    //获得字段类型
                    String value = mode.value();
                    //获得字段约束
                    String[] values=null;
                    boolean isConstraint = fields[i].isAnnotationPresent(FieldConstraint.class);
                    if (isConstraint) {
                        FieldConstraint constraint = fields[i].getAnnotation(FieldConstraint.class);
                        values = constraint.value();
                    }
                    FieldStatus fs=new FieldStatus(value,values);
                    allColumn.put(fieldName,fs);
                }
            }
            Iterator iterator=oldColumn.iterator();
            while (iterator.hasNext()) {
                allColumn.remove(iterator.next());
            }

            return  allColumn;
        }
    }

    public static class FieldStatus{
        public String _type;
        public String[] fieldConstraint;

        public FieldStatus(String _type, String[] fieldConstraint) {
            this._type = _type;
            this.fieldConstraint = fieldConstraint;
        }

        public String get_type() {
            return _type;
        }

        public void set_type(String _type) {
            this._type = _type;
        }

        public String[] getFieldConstraint() {
            return fieldConstraint;
        }

        public void setFieldConstraint(String[] fieldConstraint) {
            this.fieldConstraint = fieldConstraint;
        }

        @Override
        public String toString() {
            return "FieldStatus{" +
                    "_type='" + _type + '\'' +
                    ", fieldConstraint=" + Arrays.toString(fieldConstraint) +
                    '}';
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
                Field[] fields = classz.getDeclaredFields();
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

    public void close(){
        if (db!=null&&db.isOpen()){
            db.close();
        }
    }


    public void updateDBVersion(int version){

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
