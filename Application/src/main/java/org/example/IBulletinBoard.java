package org.example;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBulletinBoard extends Remote {
    void add(int index, String value, String tag) throws RemoteException;
    String get(int index, String tag) throws RemoteException;
    int getSize();
}
