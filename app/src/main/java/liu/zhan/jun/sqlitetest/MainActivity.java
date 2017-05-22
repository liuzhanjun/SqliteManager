package liu.zhan.jun.sqlitetest;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import liu.zhan.jun.sqlitetest.db.Constant;
import liu.zhan.jun.sqlitetest.db.DbCallBack;
import liu.zhan.jun.sqlitetest.db.DbManager;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "LOGI";
    private TextView content;
    private SQLiteDatabase db;
    private CompositeDisposable disposable;
    private String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        content = (TextView) findViewById(R.id.content);
//         path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/info.db";
        path = "infos.db";
        if (Build.VERSION.SDK_INT >= 23) {
            //检查权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CALENDAR}, 1);
            } else {
                db = DbManager.dbManager.getInstans(getApplicationContext()).OpenDb(path);
            }
        } else {
            //创建并打开数据库 一般情况下 getReadableDatabase 和getWritableDatabase是一样的，只有
            //在磁盘和数据库权限下打开对应的读写数据库
            db = DbManager.dbManager.getInstans(getApplicationContext()).OpenDb(path);

        }
        disposable = new CompositeDisposable();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @android.support.annotation.NonNull String[] permissions, @android.support.annotation.NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                db = DbManager.dbManager.getInstans(getApplicationContext()).OpenDb(path);
            }
        }
    }

    /**
     * 异步查询 查询语句
     *
     * @param view
     */
    public void query(View view) {


        QuerySubscrib query = new QuerySubscrib(db);
        disposable.add(
                Observable.create(query)
                        .subscribeOn(Schedulers.io())
                        .map(new Function<Cursor,String>() {
                            @Override
                            public String apply(@NonNull Cursor result) throws Exception {
                                Log.i(TAG, "apply: id=" + Thread.currentThread().getId());
                                String queryresulte = null;
                                //判断是否查到数据
                                int cout = result.getCount();
                                if (cout < 1) {
                                    return "";
                                }
                                while (!result.isLast()) {
                                    boolean has = result.moveToNext();
                                    Log.i(TAG, "apply: has====" + has);
                                    if (has) {
                                        queryresulte = "createDb: " + result.getString(1) + "|" + result.getInt(0);
                                    }


                                }
                                return queryresulte;
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableObserver<String>() {
                            @Override
                            public void onNext(String result) {
                                Log.i(TAG, "onNext: id=" + Thread.currentThread().getId() + "##result=" + result);

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.i(TAG, "onError: " + e.getMessage());
                            }

                            @Override
                            public void onComplete() {
                                Log.i(TAG, "onComplete: 查询完毕");
                            }
                        })
        );


    }

    public static class QuerySubscrib implements ObservableOnSubscribe<Cursor> {
        private SQLiteDatabase db;

        public QuerySubscrib(SQLiteDatabase db) {
            this.db = db;
        }

        @Override
        public void subscribe(@NonNull ObservableEmitter<Cursor> e) throws Exception {

            Log.i(TAG, "subscribe: id=" + Thread.currentThread().getId());
            Cursor result = null;//排序
            try {
                result = db.query(Constant.TABLENAME,
                        new String[]{"_id", "name"},//要查询的字段
                        "_id=?",//查询的条件
                        new String[]{"10"},//上面？的值
                        null,//分组
                        null,//分组后的条件
                        null);
            } catch (Exception e1) {
                Log.i(TAG, "subscribe: eeeeeeeeeeeeeeeeeeeeeeeeeeeee");
                e1.printStackTrace();
                e.onError(e1);
            }

            e.onNext(result);
            e.onComplete();
        }
    }


    public void query2(View view) {

        String sql = "select * from " + Constant.TABLENAME + " where _id=?";

        Cursor result2 = db.rawQuery(sql, new String[]{"20"});
        int count = result2.getCount();
        if (count < 1) {
            Log.i(TAG, "query2: =========没有查询到任何数据");
            return;
        }
        while (!result2.isLast()) {


            boolean has = result2.moveToNext();
            if (has)
                Log.i(TAG, "createDb22222222: " + result2.getString(1) + "|" + result2.getInt(0));


        }

    }

    /*
    创建表
     */
    public void createTable(View view) {

        DbManager.dbManager.createTable(Teacher.class, new DbCallBack<Boolean>() {
            @Override
            public void before() {
                Log.i(TAG, "before: 建表前");
            }

            @Override
            public void success(Boolean result) {
                Log.i(TAG, "success: 建表成功" + result);
            }

            @Override
            public void failure(Throwable error) {
                Log.i(TAG, "failure: 建表失败" + error.getMessage());
            }

            @Override
            public void finish() {
                Log.i(TAG, "finish: 建表完成");
            }
        });
//        String sql="create table Person (_id)";
//        db.execSQL(sql);
    }

    public void db_insert(View view) {
//        ContentValues values = new ContentValues();
//        values.put(Constant.name, "王强");
//        db.insert(Constant.TABLENAME, null, values);
        //数据表的类型使用引用类型
        Teacher teacher = new Teacher();
        teacher.friend = "bbbc";
        teacher.name = "赵云";
        DbManager.dbManager.insert(teacher, new DbCallBack<Long>() {
            @Override
            public void before() {
                Log.i(TAG, "before: 开始插入数据");
            }

            @Override
            public void success(Long row) {
                Log.i(TAG, "success: " + row);
            }

            @Override
            public void failure(Throwable error) {
                Log.i(TAG, "failure: 插入失败============" + error.getMessage());
            }

            @Override
            public void finish() {
                Log.i(TAG, "finish: 插入完成");
            }
        });
    }

    public void db_Sql_insert(View view) {
        //insert into 表名 (字段1，字段2，字段3...) values (值1，值2，值3 ...)
//        String sql = "insert into " + Constant.TABLENAME + " (name) values (\"李四\")";
//        db.execSQL(sql);
    }

    public void queryAll(View view) {
        String sql = "select * from " + Constant.TABLENAME;
        Cursor c = db.rawQuery(sql, null);
        Log.i(TAG, "queryAll: ===|" + c.getColumnName(0) + "\t|" + c.getColumnName(1));
        while (!c.isLast()) {
            boolean next = c.moveToNext();
            if (next) {
                Log.i(TAG, "queryAll: ===|" + c.getInt(0) + "\t|" + c.getString(1));
            }
        }
    }


    public void update1(View view) {
        Teacher teacher = new Teacher();
        teacher.name = "王思聪";
        teacher.age = 30;

        DbManager.dbManager.update(teacher, "_id=?", new String[]{"10"}, new DbCallBack<Integer>() {
            @Override
            public void before() {
                Log.i(TAG, "before: 更新数据");
            }

            @Override
            public void success(Integer result) {
                Log.i(TAG, "success: 更新成功" + result);
            }

            @Override
            public void failure(Throwable error) {
                Log.i(TAG, "failure: 更新失败" + error.getMessage());
            }

            @Override
            public void finish() {
                Log.i(TAG, "finish: 更新完成");
            }
        });
    }


    public void delete(View view){

        DbManager.dbManager.delete(Teacher.class, "name=?", new String[]{"王思聪"}, new DbCallBack<Integer>() {
            @Override
            public void before() {
                Log.i(TAG, "before: 删除开始");
            }

            @Override
            public void success(Integer result) {
                Log.i(TAG, "before: 删除成功"+result);
            }

            @Override
            public void failure(Throwable error) {
                Log.i(TAG, "failure: 删除失败"+error.getMessage());
            }

            @Override
            public void finish() {
                Log.i(TAG, "finish: 删除完毕");
            }
        });
    }

    public void deleteAll(View view) {

        DbManager.dbManager.delete(Teacher.class, null, null, new DbCallBack<Integer>() {
            @Override
            public void before() {
                Log.i(TAG, "before: 删除所有数据开始");
            }

            @Override
            public void success(Integer result) {
                Log.i(TAG, "before: 删除所有数据成功" + result);
            }

            @Override
            public void failure(Throwable error) {
                Log.i(TAG, "failure: 删除所有数据失败" + error.getMessage());
            }

            @Override
            public void finish() {
                Log.i(TAG, "finish: 删除所有数据完毕");
            }
        });
    }
}