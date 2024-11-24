package org.example;

import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Message {
    public String message;
    public int index;
    public String tag;

    public Message(String message, int index, String tag){
        this.message = message;
        this.index = index;
        this.tag = tag;
    }

    private byte[] toByteArray(){
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] tagBytes = tag.getBytes(StandardCharsets.UTF_8);
        return ByteBuffer.allocate(4 + msgBytes.length + 4 + 4 + tagBytes.length)
                .putInt(msgBytes.length)
                .put(msgBytes)
                .putInt(index)
                .putInt(tagBytes.length)
                .put(tagBytes)
                .array();
    }
    public String encrypt(SecretKey key) throws Exception {
        byte[] buf = toByteArray();
        return Crypto.encrypt(Base64.getEncoder().encodeToString(buf), key);
    }

    public static Message decrypt(String encryptedText, SecretKey key) throws Exception {
        String decrypted = Crypto.decrypt(encryptedText, key);
        byte[] decryptedMsg = Base64.getDecoder().decode(decrypted);
        int msgLength = readInt(decryptedMsg, 0);
        String msg = readString(decryptedMsg, 4, msgLength);
        int index = readInt(decryptedMsg, 4+msgLength);
        int tagLength = readInt(decryptedMsg, 8+msgLength);
        String tag = readString(decryptedMsg, 12+msgLength, tagLength);
        return new Message(msg, index, tag);
    }

    private static int readInt(byte[] buffer, int offset){
        return ByteBuffer.wrap(buffer, offset, 4).getInt();
    }

    private static String readString(byte[] buffer, int offset, int length){
        return new String(buffer, offset, length);
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", index=" + index +
                ", tag='" + tag + '\'' +
                '}';
    }
}
