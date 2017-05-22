package liu.zhan.jun.sqlitetest;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        List<Teacher> teachers=new ArrayList<>();
        Teacher t=new Teacher();
        t._id=3;
        t.name="百尺";
        t.freads=new String[]{"百丈","大林","风和"};
        teachers.add(t);
        Gson gson=new Gson();
        String json=gson.toJson(teachers);
        System.out.print(json);
        assertEquals(4, 2 + 2);
    }


}