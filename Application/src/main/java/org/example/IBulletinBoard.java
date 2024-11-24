package org.example;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBulletinBoard extends Remote {
    int POW_DIFFICULTY = 3;
    void add(int index, String value, String tag) throws Exception;
    void add(int index, String value, String tag, String nonce, String challenge) throws Exception;
    String get(int index, String tag) throws Exception;
    int getSize() throws Exception;
    String requestChallenge() throws Exception;
}
