package liu.zhan.jun.sqlitetest.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by 刘展俊 on 2017/5/21.
 * //约束
 * not null
 * unique (唯一约束)
 * primary key (主键约束)
 * check (检查约束)
 * default (默认约束)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldConstraint {
    public String [] value();
}
