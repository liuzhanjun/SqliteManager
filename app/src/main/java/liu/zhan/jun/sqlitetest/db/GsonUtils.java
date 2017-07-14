package liu.zhan.jun.sqlitetest.db;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by 刘展俊 on 2017/5/22.
 */

public class GsonUtils {

    /**
     * 将json to map
     *
     * @param obj
     * @return
     */
    public static HashMap<String,String> jsonToMap(Object obj,Gson mGson) {
        if (obj == null) {
            return null;
        }
        HashMap<String,String> map = null;
        JsonElement jsonTree = mGson.toJsonTree(obj);
        JsonObject asJsonObject = jsonTree.getAsJsonObject();
        Set<Map.Entry<String,JsonElement>> entrySet = asJsonObject.entrySet();
        if (entrySet.size() > 0) {
            map = new HashMap<String,String>();
        }

        for (Map.Entry<String,JsonElement> entry : entrySet) {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            map.put(key, value);
        }
        return map;
    }
}
