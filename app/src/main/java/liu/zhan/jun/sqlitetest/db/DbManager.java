package liu.zhan.jun.sqlitetest.db;

import android.content.Context;

/**
 * Created by 刘展俊 on 2017/5/19.
 */

public enum DbManager {
    dbManager {
        @Override
        public MySqlHelpe getInstans(Context context) {
            this.helper=new MySqlHelpe(context);
            return this.helper;
        }
    };

    private DbManager() {

    }
    public abstract MySqlHelpe getInstans(Context context);

    public MySqlHelpe helper;

}
