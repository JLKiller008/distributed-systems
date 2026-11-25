package de.fhwedel.distributedSystems;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;

public class Server {

    public static void main(String[] args) throws IOException {
        new Server().startServer();
        System.out.println("Server running");
    }

    private static void writeStubToFile(String fileName, Remote stub) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(stub);
        out.close();
    }

    private void startServer() throws FileNotFoundException, IOException {
        DataBaseImpl db = new DataBaseImpl();
        writeStubToFile("dataBase", UnicastRemoteObject.exportObject(db, 0));
    }

}
