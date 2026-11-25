package de.fhwedel.distributedSystems;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Client {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new Client().runClient();
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

    private void runClient() throws FileNotFoundException, ClassNotFoundException, IOException {
        System.out.println("Client started");
        DataBase db = readStubFromFile("dataBase");

        db.addRecord(4101, "Appen");
        db.addRecord(4102, "Ahrensburg");
        db.addRecord(4103, "Wedel");
        db.addRecord(4104, "Aumühle");
        db.addRecord(4105, "Seevetal");
        db.addRecord(4106, "Quickborn");

        System.out.println("4103: " + db.getRecord(4103));
        System.out.println("4107: " + db.getRecord(4107));

        System.out.println("Size: " + db.getSize());

        System.out.println("Idx Appen: " + db.getIndex("Appen"));
        System.out.println("Idx Moorrege: " + db.getIndex("Moorrege"));

        DBResult result = db.getRecordObj(4101);
        System.out.println("Result Object Appen: " + result.getKey() + " " + result.getValue());

    }

}
