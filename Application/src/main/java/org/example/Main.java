package org.example;


import org.example.Form.Chat;

public class Main {
    public static void main(String[] args) throws Exception {
        // Run chat form
        Server.main(null);
        // Start new thread for chat form
        Thread thread = new Thread(() -> {
            try {
                Chat chatForm = new Chat();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Thread thread2 = new Thread(() -> {
            try {
                Chat chatForm = new Chat();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread2.start();
        thread.start();
    }
}