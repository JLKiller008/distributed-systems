package de.fhwedel.distributedSystems;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataBaseImpl implements DataBase {

    private HashMap<Integer, String> database;

    public DataBaseImpl() {
        this.database = new HashMap<>();
    }

    public synchronized String getRecord(int key) {
        return database.get(key);
    }

    public synchronized void addRecord(int key, String value) {
        database.put(key, value);
    }

    public synchronized int getSize() {
        return database.size();
    }

    public synchronized int getIndex(String value) {
        Set<Map.Entry<Integer, String>> entries = database.entrySet();
        for (Map.Entry<Integer, String> entry : entries) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public DBResult getRecordObj(int idx) {
        DBResult result = new DBResult();
        result.setKey(idx);
        result.setValue(this.getRecord(idx));
        return result;
    }
}
