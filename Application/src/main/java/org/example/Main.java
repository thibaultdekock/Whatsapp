package org.example;


import org.example.Form.Chat;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        // Run chat form
        Server.main(null);

        // Delete all files in the directory
        deleteFiles("./bumpfiles/");

        // Run two clients
        for (int i = 0; i < 2; i++) {
            Thread client = new Thread(() -> {
                try {
                    Chat chatForm = new Chat();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            client.start();
        }

    }

    public static void deleteFiles(String path) {
        // Delete all files in the directory
        File directory = new File(path);

        // Check if the folder exists and is a directory
        if (directory.exists() && directory.isDirectory()) {
            // Get all files in the folder
            File[] files = directory.listFiles();

            if (files != null) {
                // Loop through the files and delete each one
                for (File file : files) {
                    if (file.isFile()) { // Only delete files, not subdirectories
                        if (file.delete()) {
                            System.out.println("Deleted: " + file.getName());
                        } else {
                            System.out.println("Failed to delete: " + file.getName());
                        }
                    }
                }
            } else {
                System.out.println("No files found in the folder.");
            }
        } else {
            System.out.println("The specified folder does not exist or is not a directory.");
        }
    }
}