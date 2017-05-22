package liu.zhan.jun.sqlitetest;

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
}
