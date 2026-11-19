private static DataBase readStubFromFile(String fileName) 
    throws FileNotFoundException, IOException, ClassNotFoundException {
        /* Deserialize stub using Java Serialization */
          FileInputStream fis = new FileInputStream(fileName);
          ObjectInputStream in = new ObjectInputStream(fis);
          DataBase remoteObj = (DataBase)in.readObject();
          in.close();
          return remoteObj;
    }
