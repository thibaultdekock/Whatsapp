package org.example;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class BulletinBoard extends UnicastRemoteObject implements IBulletinBoard{
    //2e hashmap <tag, value> voor makkelijk ophalen in get
    private HashMap<Integer, HashMap<String, String>> board = new HashMap<>();
    private final int size;
    public BulletinBoard(int size) throws RemoteException {
        this.size = size;
    }
    @Override
    public synchronized void add(int i, String value, String tag) throws Exception {
        /*
            add(i, v, t): Add v,t to the set at cell i: B[i] := B[i] U {<v, t>}
         */
        if(board.get(i)==null){
            board.put(i, new HashMap<>());
        }
        board.get(i).put(tag, value);
        int d = 0;
    }

    @Override
    public synchronized String get(int i, String tag) throws Exception {
        /*
            get(i, b): Let t = Beta(b). If v,t B[i] for some value v, return
            v and remove v,t from B[i]. Otherwise return , and
            leave B[i] unchanged.

            let tag = Beta(b) buitenaf doen tag meegeven als parameter.
         */
        String hashedTag = Crypto.hash(tag);
        HashMap<String, String> set = board.get(i);
        if(set != null && set.containsKey(hashedTag)){
            return set.remove(hashedTag);
        }
        return null;
    }
    @Override
    public int getSize() throws Exception{
        return size;
    }
}
