package com.androidscanapp;

import java.util.Comparator;
import java.util.Map;


public class MapComparator implements Comparator<Map<String, Object>> {
    @Override
    public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
        String app1 = (String) ((Map) lhs).get("appName");
        String app2 = (String) ((Map) rhs).get("appName");
        if (app1 != null && app2 != null){
            return app1.compareTo(app2);
        }
        return 0;
    }

    @Override
    public boolean equals(Object object) {
        return false;
    }
}
