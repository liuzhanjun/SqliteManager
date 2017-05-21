package liu.zhan.jun.sqlitetest;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import liu.zhan.jun.sqlitetest.db.Constant;
import liu.zhan.jun.sqlitetest.db.DbManager;
import liu.zhan.jun.sqlitetest.db.MySqlHelpe;

public class MainActivity extends AppCompatActivity {

    public static final String TAG="LOGI";
    private TextView content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        content= (TextView) findViewById(R.id.content);
    }


    public void createDb(View view){
        MySqlHelpe helper=DbManager.dbManager.getInstans(getApplicationContext());
        //创建并打开数据库 一般情况下 getReadableDatabase 和getWritableDatabase是一样的，只有
        //在磁盘和数据库权限下打开对应的读写数据库
        SQLiteDatabase db=helper.getWritableDatabase();

        ContentValues values=new ContentValues();
        values.put(Constant.name,"王强");
        db.insert(Constant.TABLENAME,null,values);
        Cursor result = db.query(Constant.TABLENAME, null, null, null, null, null, null);

        String resultString=result.getColumnCount()+"";
        content.setText(resultString);
    }
}
