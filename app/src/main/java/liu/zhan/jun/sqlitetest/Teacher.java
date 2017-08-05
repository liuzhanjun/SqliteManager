package liu.zhan.jun.sqlitetest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liu.zhan.jun.sqlitetest.db.DbManager;
import liu.zhan.jun.sqlitetest.db.FieldConstraint;
import liu.zhan.jun.sqlitetest.db.FieldType;
import liu.zhan.jun.sqlitetest.db.TableField;

/**
 * Created by 刘展俊 on 2017/5/21.
 * 类型最好使用引用类型
 */

public class Teacher implements DbManager.TableModel{

    @TableField
    @FieldType(value ="Integer")
    @FieldConstraint(value = {"primary key"})
    public Integer _id;
    @TableField
    @FieldType(value ="varchar(10)")
    @FieldConstraint(value = {"not null"})
    public String name;
    @TableField
    @FieldType(value ="Integer")
    public Integer age;
    public String friend;
    public String [] freads;
    @TableField
    @FieldType(value ="text(200)")
    public List<Student> students;

    @TableField
    @FieldType(value ="text(200)")
    public Student besetStudent;

    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }

    public String[] getFreads() {
        return freads;
    }

    public void setFreads(String[] freads) {
        this.freads = freads;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public Student getBesetStudent() {
        return besetStudent;
    }

    public void setBesetStudent(Student besetStudent) {
        this.besetStudent = besetStudent;
    }


    public Teacher() {

    }
    public Teacher(int q) {
        this._id = 0;
        this.name = "";
        this.age = 0;
        this.students = new ArrayList<>();
        this.besetStudent = new Student();
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "_id=" + _id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", friend='" + friend + '\'' +
                ", freads=" + Arrays.toString(freads) +
                ", students=" + students +
                ", besetStudent=" + besetStudent +
                '}';
    }
}
