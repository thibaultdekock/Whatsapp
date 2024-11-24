package org.example;


import java.util.UUID;

// proof-of-work (PoW)
public class PoW {
    private final int difficulty; // Number of leading zeros required

    public PoW(int difficulty) {
        this.difficulty = difficulty;
    }


    public String generateChallenge() {
        return UUID.randomUUID().toString();
    }

    public boolean verifySolution(String challenge, String nonce) throws Exception {
        String input = challenge + nonce;
        String hash = Crypto.hash(input);
        return hash.startsWith("0".repeat(difficulty));
    }
}

