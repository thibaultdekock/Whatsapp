package org.example;

import java.util.HashMap;

public class BulletinBoard {
    //2e hashmap <tag, value> voor makkelijk ophalen in get
    private HashMap<Integer, HashMap<String, String>> board = new HashMap<>();
    private final int size;
    public BulletinBoard(int size){
        this.size = size;
    }
    public void add(int i, String value, String tag){
        /*
            add(i, v, t): Add v,t to the set at cell i: B[i] := B[i] U {<v, t>}
         */
        if(board.get(i)==null){
            board.put(i, new HashMap<>());
        }
        board.get(i).put(tag, value);
    }
    public String get(int i, String tag){
        /*
            get(i, b): Let t = Beta(b). If v,t B[i] for some value v, return
            v and remove v,t from B[i]. Otherwise return , and
            leave B[i] unchanged.

            let tag = Beta(b) buitenaf doen tag meegeven als parameter.
         */
        HashMap<String, String> set = board.get(i);
        if(set != null && set.containsKey(tag)){
            return set.remove(tag);
        }
        return null;
    }
}
