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
        board.get(i).put(Crypto.hash(tag), value);
        int d = 0;
    }

    @Override
    public synchronized void add(int i, String value, String tag, String nonce, String challenge) throws Exception {
        /*
            add(i, v, t): Add v,t to the set at cell i: B[i] := B[i] U {<v, t>}
         */
        PoW pow = new PoW(this.POW_DIFFICULTY);
        if (!pow.verifySolution(challenge, nonce)) {
            throw new SecurityException("Invalid Proof of Work");
        }
        if(board.get(i)==null){
            board.put(i, new HashMap<>());
        }
        board.get(i).put(Crypto.hash(tag), value);
        int d = 0;
    }

    @Override
    public synchronized String get(int i, String tag) throws Exception {
        /*
            get(i, b): Let t = Beta(b). If v,t B[i] for some value v, return
            v and remove v,t from B[i]. Otherwise return , and
            leave B[i] unchanged.
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

    // Return a unique challenge to the client
    public String requestChallenge() {
        PoW pow = new PoW(POW_DIFFICULTY);
        return pow.generateChallenge();
    }

}
