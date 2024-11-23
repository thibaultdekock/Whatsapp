package org.example;

import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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
        return ByteBuffer.allocate(4 + message.length() + tag.length() + 8)
                .put(ByteBuffer.allocate(4).putInt(message.length()).array())
                .put(message.getBytes(StandardCharsets.UTF_8))
                .put(ByteBuffer.allocate(4).putInt(index).array())
                .put(ByteBuffer.allocate(4).putInt(tag.length()).array())
                .put(tag.getBytes(StandardCharsets.UTF_8)).array();
    }
    public String encrypt(SecretKey key) throws Exception {
        byte[] buf = toByteArray();
        return Crypto.encrypt(new String(buf, StandardCharsets.UTF_8), key);
    }

    public static Message decrypt(String encryptedText, SecretKey key) throws Exception {
        byte[] decryptedMsg = Crypto.decrypt(encryptedText, key).getBytes(StandardCharsets.UTF_8);
        int msgLength = readInt(decryptedMsg, 0);
        String msg = readString(decryptedMsg, 4, msgLength);
        int index = readInt(decryptedMsg, 4+msgLength);
        int tagLength = readInt(decryptedMsg, 8+msgLength);
        String tag = readString(decryptedMsg, 12+msgLength, tagLength);
        return new Message(msg, index, tag);
    }

    private static int readInt(byte[] buffer, int offset){
        return (buffer[offset] << 24) | (buffer[offset+1] << 16) |
                (buffer[offset+2] << 8) | (buffer[offset+3]);
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
