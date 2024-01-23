package com.yit.simpleDB;

import java.io.*;

public class Util {

    public static void ObjectOutput(Object obj, String fileName) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(fileName);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(obj);
        out.close();
        fileOut.close();
    }

    public static Object ObjectInput(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(fileName);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Object obj = in.readObject();
        in.close();
        fileIn.close();
        return obj;
    }
}
