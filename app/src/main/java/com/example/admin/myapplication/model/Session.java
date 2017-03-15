package com.example.admin.myapplication.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2017-03-15.
 */

public class Session {

    public Map<String,Object> map;

    public Session() {
        map=new HashMap<>();
    }
    public void set(String key,Object value){
        map.put(key, value);
    }
    public boolean getBoolean(String key) {
        Object value = map.get(key);
        return value == null ? false:(boolean) value;
    }

    public <T> T getObj(String key) {
        return (T) map.get(key);
    }
}
