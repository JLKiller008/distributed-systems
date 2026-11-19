import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DatabaseClient {
    private static final String DEFAULT_STUB_FILE = "database.stub";

    public static void main(String[] args) {
        String stubFile = args.length > 0 ? args[0] : DEFAULT_STUB_FILE;
        try {
            DataBase database = readStubFromFile(stubFile);
            run(database);
        } catch (IOException | ClassNotFoundException exception) {
            System.err.println("Could not communicate with RMI database server: "
                + exception.getMessage());
        }
    }

    private static void run(DataBase database) throws RemoteException {
        System.out.println("Initial getSize(): " + database.getSize());

        Map<Integer, String> records = new LinkedHashMap<>();
        records.put(4101, "Appen");
        records.put(4102, "Ahrensburg");
        records.put(4103, "Wedel");
        records.put(4104, "Aumühle");
        records.put(4105, "Seevetal");
        records.put(4106, "Quickborn");

        for (Map.Entry<Integer, String> entry : records.entrySet()) {
            database.addRecord(entry.getKey(), entry.getValue());
        }

        printRecord(database, 4103);
        printRecord(database, 4107);
        System.out.println("getSize(): " + database.getSize());

        printIndex(database, "Wedel");
        printIndex(database, "Hamburg");

        printRecordObj(database, 4103);
        printRecordObj(database, 4107);
    }

    private static void printRecord(DataBase database, int index) throws RemoteException {
        String record = database.getRecord(index);
        System.out.printf("getRecord(%d): %s%n", index, record != null ? record : "null (not found)");
    }

    private static void printIndex(DataBase database, String value) throws RemoteException {
        int index = database.getIndex(value);
        System.out.printf("getIndex(%s): %s%n", value, index >= 0 ? index : "-1 (not found)");
    }

    private static void printRecordObj(DataBase database, int index) throws RemoteException {
        DBResult result = database.getRecordObj(index);
        if (result == null) {
            System.out.printf("getRecordObj(%d): null (not found)%n", index);
        } else {
            System.out.printf("getRecordObj(%d): key=%d, value=%s%n",
                index, result.getKey(), result.getValue());
        }
    }

    private static DataBase readStubFromFile(String fileName)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        /* Deserialize stub using Java Serialization */
        FileInputStream fis = new FileInputStream(fileName);
        ObjectInputStream in = new ObjectInputStream(fis);
        DataBase remoteObj = (DataBase) in.readObject();
        in.close();
        return remoteObj;
    }
}
