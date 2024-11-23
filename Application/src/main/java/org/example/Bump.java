package org.example;

import java.security.PublicKey;

public class Bump {
    public PublicKey publicKey;
    public int index;
    public String tag;

    public Bump(PublicKey publicKey, int index, String tag){
        this.publicKey = publicKey;
        this.index = index;
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "Bump\nPublic Key:\n" + publicKey + "\nIndex: " + index + "\nTag: " +tag;
    }
}
