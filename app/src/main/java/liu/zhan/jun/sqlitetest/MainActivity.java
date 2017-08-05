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
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

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
import liu.zhan.jun.sqlitetest.db.DbCallBack;
import liu.zhan.jun.sqlitetest.db.DbManager;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "LOGI";
    private TextView content;
    private String path;
    private String path2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        content = (TextView) findViewById(R.id.content);
//         path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/info.db";
        //如果是4.2或者4.4
        path=getFilesDir().getParent()+"/databases/infos.db";
        path2=getFilesDir().getParent()+"/databases/inos2.db";
//        path = "infos.db";
        if (Build.VERSION.SDK_INT >= 23) {
            //检查权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CALENDAR}, 1);
            } else {
                DbManager.dbManager.getInstans(getApplicationContext()).OpenDb(path);
                DbManager.dbManager.getInstans(getApplicationContext()).OpenDb(path2);
            }
        } else {
            //创建并打开数据库 一般情况下 getReadableDatabase 和getWritableDatabase是一样的，只有
            //在磁盘和数据库权限下打开对应的读写数据库
            DbManager.dbManager.getInstans(getApplicationContext()).OpenDb(path);
            DbManager.dbManager.getInstans(getApplicationContext()).OpenDb(path2);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @android.support.annotation.NonNull String[] permissions, @android.support.annotation.NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                DbManager.dbManager.getInstans(getApplicationContext()).OpenDb(path);
                DbManager.dbManager.getInstans(getApplicationContext()).OpenDb(path2);
            }
        }
    }

    /**
     * 异步查询 查询语句
     *
     * @param view
     */
    public void query(View view) {
        DbManager.dbManager.checkOpen(path);
        Teacher t=new Teacher(1);
            DbManager.dbManager.query(t, new DbCallBack<List<Teacher>>() {
                @Override
                public void before() {
                    Log.i(TAG, "before: ============查询");
                }

                @Override
                public void success(List<Teacher> result) {
                    for (Teacher teacher : result) {
                        content.setText(teacher.getStudents().toString());
                    }
                }

                @Override
                public void failure(Throwable error) {
                    Log.i(TAG, "failure: f"+error.getMessage());
                }

                @Override
                public void finish() {

                }
            });

    }




    public void query2(View view) {
        DbManager.dbManager.checkOpen(path);
        Teacher teacher = new Teacher();
        //设置的值表示这个是要查询的字段
        teacher._id = 0;//设置的值无实际意义
        teacher.name = "?";//设置的值无实际意义

        DbManager.dbManager.query(teacher, "_id DESC", new DbCallBack<List<Teacher>>() {
            @Override
            public void before() {
                Log.i(TAG, "before: 开始查询");
            }

            @Override
            public void success(List<Teacher> result) {
                if (result == null) {
                    return;
                }
                Log.i(TAG, "success: 查询成功");
                for (int i = 0; i < result.size(); i++) {
                    Log.i(TAG, "success: =======" + result.get(i).toString());
                }
            }

            @Override
            public void failure(Throwable error) {
                Log.i(TAG, "failure: 失败了" + error.getMessage());
            }

            @Override
            public void finish() {
                Log.i(TAG, "finish: 查询完毕");
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DbManager.dbManager.unscribe();
    }



    /*
    创建表
     */
    public void createTable(View view) {
        DbManager.dbManager.checkOpen(path);
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

    }

    public void db_insert(View view) {
//        ContentValues values = new ContentValues();
//        values.put(Constant.name, "王强");
//        db.insert(Constant.TABLENAME, null, values);
        //数据表的类型使用引用类型
        Teacher teacher = new Teacher();
        teacher.friend = "bbbc";
        teacher.name = "赵云";
        teacher.setBesetStudent(new Student(0,"小白",20));
        List<Student> students=getStudents();
        teacher.setStudents(students);
        DbManager.dbManager.checkOpen(path);

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



    public void queryAll(View view) {

    }


    public void update1(View view) {
        Teacher teacher = new Teacher();
        teacher.name = "王思聪";
        teacher.age = 30;
        DbManager.dbManager.checkOpen(path);
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


    public void delete(View view) {
        DbManager.dbManager.checkOpen(path);
        DbManager.dbManager.delete(Teacher.class, "name=?", new String[]{"王思聪"}, new DbCallBack<Integer>() {
            @Override
            public void before() {
                Log.i(TAG, "before: 删除开始");
            }

            @Override
            public void success(Integer result) {
                Log.i(TAG, "before: 删除成功" + result);
            }

            @Override
            public void failure(Throwable error) {
                Log.i(TAG, "failure: 删除失败" + error.getMessage());
            }

            @Override
            public void finish() {
                Log.i(TAG, "finish: 删除完毕");
            }
        });
    }

    public void createStudent(View view) {
        DbManager.dbManager.checkOpen(path2);
        DbManager.dbManager.createTable(Student.class, new DbCallBack<Boolean>() {
            @Override
            public void before() {
                Log.i(TAG, "before: ");
            }

            @Override
            public void success(Boolean result) {
                Log.i(TAG, "success: "+result);
            }

            @Override
            public void failure(Throwable error) {
                Log.i(TAG, "failure: "+error.getMessage());
            }

            @Override
            public void finish() {

            }
        });
    }
    public void insertStudent(View view) {
        DbManager.dbManager.checkOpen(path2);
        Student s=new Student(null,"赵云",1);
        DbManager.dbManager.insert(s, new DbCallBack<Long>() {
            @Override
            public void before() {
                Log.i(TAG, "before: ");
            }

            @Override
            public void success(Long result) {
                Log.i(TAG, "success: "+result);
            }

            @Override
            public void failure(Throwable error) {
                Log.i(TAG, "failure: "+error.getMessage());
            }

            @Override
            public void finish() {

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

    public List<Student> getStudents() {
        List<Student> students=new ArrayList<>();
        for(int i=0;i<5;i++){
            Student student=new Student(i,"编号"+i,(23+i));
            students.add(student);
        }
        return students;
    }
}
