package org.example;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Crypto {
    public static String encrypt(String text, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(text.getBytes()));
    }

    public static String decrypt(String ciphertext, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)));
    }

    public static SecretKey deriveKey(SecretKey key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        byte[] derivedBytes = mac.doFinal("key-derivation".getBytes());
        return new SecretKeySpec(derivedBytes, 0, 16, "AES");
    }

    public static String hash(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(input.getBytes());
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

    public static KeyPair generateKeys() throws Exception {
        String curveName = "secp256r1";
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(curveName);
        keyPairGenerator.initialize(ecGenParameterSpec);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    public static String encodeBumpfile(PublicKey key, int index, String tag){
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        return Base64.getEncoder().encodeToString((encodedKey + " " + index + " " + tag).getBytes(StandardCharsets.UTF_8));
    }

    public static String decryptBumpfile(File file) throws Exception {
        String fileContent = Files.readString(Path.of(file.getPath()));
        return new String(Base64.getDecoder().decode(fileContent), StandardCharsets.UTF_8);
    }

    public static PublicKey stringToPublicKey(String key) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePublic(keySpec);
    }
}
