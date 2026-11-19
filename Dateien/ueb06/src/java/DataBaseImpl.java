import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataBaseImpl implements DataBase {
    private final Map<Integer, String> records = new ConcurrentHashMap<>();

    @Override
    public String getRecord(int index) throws RemoteException {
        String record = records.get(index);
        System.out.printf("getRecord(%d) -> %s%n", index, record);
        return record;
    }

    @Override
    public void addRecord(int index, String record) throws RemoteException {
        if (record == null) {
            throw new IllegalArgumentException("record must not be null");
        }
        records.put(index, record);
        System.out.printf("addRecord(%d, %s) -> size=%d%n", index, record, records.size());
    }

    @Override
    public int getSize() throws RemoteException {
        int size = records.size();
        System.out.printf("getSize() -> %d%n", size);
        return size;
    }

    @Override
    public int getIndex(String record) throws RemoteException {
        if (record == null) {
            System.out.println("getIndex(null) -> -1");
            return -1;
        }
        int matchedIndex = -1;
        for (Map.Entry<Integer, String> entry : records.entrySet()) {
            if (record.equals(entry.getValue())
                    && (matchedIndex == -1 || entry.getKey() < matchedIndex)) {
                matchedIndex = entry.getKey();
            }
        }
        System.out.printf("getIndex(%s) -> %d%n", record, matchedIndex);
        return matchedIndex;
    }

    @Override
    public DBResult getRecordObj(int index) throws RemoteException {
        String record = records.get(index);
        if (record == null) {
            System.out.printf("getRecordObj(%d) -> null%n", index);
            return null;
        }
        DBResult result = new DBResult(index, record);
        System.out.printf("getRecordObj(%d) -> %s%n", index, result);
        return result;
    }
}
