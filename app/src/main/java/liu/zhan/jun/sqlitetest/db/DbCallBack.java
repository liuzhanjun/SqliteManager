package liu.zhan.jun.sqlitetest.db;

import com.google.gson.internal.$Gson$Types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by 刘展俊 on 2017/5/22.
 */

public abstract class DbCallBack<T> {
    public abstract void before();
    public abstract void success(T result);
    public abstract void failure(Throwable error);
    public abstract void finish();

    private Type mType;

    public Type getmType() {
        return mType;
    }

    public DbCallBack() {
        mType = getSuperclassTypeParameter(getClass());
    }

    private Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter. 请在RequestCallBack上添加泛型类型");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types
                .canonicalize(parameterized.getActualTypeArguments()[0]);
    }
}
