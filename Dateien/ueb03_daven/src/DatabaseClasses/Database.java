package DatabaseClasses;

import java.util.HashMap;

public class Database {

    private HashMap<Integer, String> database;

    public Database() {
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
}
