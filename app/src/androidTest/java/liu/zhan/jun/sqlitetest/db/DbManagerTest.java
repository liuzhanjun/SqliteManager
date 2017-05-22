package liu.zhan.jun.sqlitetest.db;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import liu.zhan.jun.sqlitetest.Teacher;

import static org.junit.Assert.*;

/**
 * Created by 刘展俊 on 2017/5/21.
 */
@RunWith(AndroidJUnit4.class)
public class DbManagerTest {
    @Test
    public void createTable() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        Log.i("LOGI","===============");
        DbManager.dbManager.getInstans(appContext).createTable(Teacher.class);
        assertEquals(1,1);
    }

    @Test
    public void insert() throws  Exception{
        Context appContext = InstrumentationRegistry.getTargetContext();
        Log.i("LOGI","===============");
        Teacher teacher=new Teacher();
        teacher._id=20;
        teacher.age=20;
        teacher.name="小白";
        teacher.friend="xiaohei";
        DbManager.dbManager.getInstans(appContext).insert(teacher);
        assertEquals(1,1);

    }

}