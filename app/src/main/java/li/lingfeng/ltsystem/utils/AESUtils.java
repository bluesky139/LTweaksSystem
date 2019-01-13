package li.lingfeng.ltsystem.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    private static final String CIPHER_MODE = "AES/ECB/PKCS5Padding";

    public static String encrypt(String content, String password) throws Throwable {
        byte[] data = content.getBytes("UTF-8");
        data = encrypt(data, password);
        String result = bytes2hex(data);
        return result;
    }

    public static byte[] encrypt(byte[] content, String password) throws Throwable {
        SecretKeySpec key = createKey(password);
        Cipher cipher = Cipher.getInstance(CIPHER_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(content);
    }

    public static String decrypt(String content, String password) throws Throwable {
        byte[] data = hex2bytes(content);
        data = decrypt(data, password);
        if (data == null) {
            return null;
        }
        return new String(data, "UTF-8");
    }

    public static byte[] decrypt(byte[] content, String password) throws Throwable {
        SecretKeySpec key = createKey(password);
        Cipher cipher = Cipher.getInstance(CIPHER_MODE);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(content);
    }

    private static SecretKeySpec createKey(String password) throws Throwable {
        if (password == null) {
            password = "";
        }
        StringBuffer buffer = new StringBuffer(32);
        buffer.append(password);
        while (buffer.length() < 32) {
            buffer.append("0");
        }
        if (buffer.length() > 32) {
            buffer.setLength(32);
        }
        byte[] data = buffer.toString().getBytes("UTF-8");
        return new SecretKeySpec(data, "AES");
    }

    private static String bytes2hex(byte[] bytes) {
        StringBuffer buffer = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; ++i) {
            String str = (java.lang.Integer.toHexString(bytes[i] & 0XFF));
            if (str.length() == 1) {
                buffer.append("0");
            }
            buffer.append(str);
        }
        return buffer.toString().toUpperCase();
    }

    private static byte[] hex2bytes(String hex) {
        if (hex == null || hex.length() < 2) {
            return new byte[0];
        }
        hex = hex.toLowerCase();
        int len = hex.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; ++i) {
            String str = hex.substring(2 * i, 2 * i + 2);
            result[i] = (byte) (Integer.parseInt(str, 16) & 0xFF);
        }
        return result;
    }
}
