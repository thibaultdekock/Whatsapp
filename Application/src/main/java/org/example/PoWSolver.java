package org.example;

public class PoWSolver {
    public String solveChallenge(String challenge, int difficulty) throws Exception {
        String prefix = "0".repeat(difficulty); // Required hash prefix
        String nonce = "";
        int counter = 0;

        // Time the PoW solving process
        long startTime = System.currentTimeMillis();

        while (true) {
            nonce = String.valueOf(counter);
            String input = challenge + nonce;
            String hash = Crypto.hash(input);

            if (hash.startsWith(prefix)) {
                long endTime = System.currentTimeMillis();
                System.out.println("PoW solved in " + (endTime - startTime) + " ms");
                return nonce; // Valid nonce found
            }

            counter++;
        }
    }
}
