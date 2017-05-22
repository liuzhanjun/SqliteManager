package liu.zhan.jun.sqlitetest.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by 刘展俊 on 2017/5/21.
 * value的取值
 * NULL、
 * REAL(浮点数 8byte) 、
 * BLOB(二进制对象)
 * Integer
 * varchar(10)(括号内为varchar的长度)
 * float
 * double
 * char(10)
 * text
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldType {
    public String value();
}
