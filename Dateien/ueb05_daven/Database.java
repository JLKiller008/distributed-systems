import java.util.HashMap;

/** Speicher-Datenbank, unveraendert aus ueb03 (HashMap, synchronisiert wegen mehrerer Threads). */
public class Database {
    private final HashMap<Integer, String> records = new HashMap<>();

    public synchronized String getRecord(int key) { return records.get(key); }
    public synchronized void addRecord(int key, String value) { records.put(key, value); }
    public synchronized int getSize() { return records.size(); }
}
