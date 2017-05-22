package liu.zhan.jun.sqlitetest.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by 刘展俊 on 2017/5/21.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
//create table 表名 (字段 类型 约束)
public @interface TableField {

}





