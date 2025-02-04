package com.micropay.api;
import android.util.Base64;
import android.util.Log;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class AESHandler {

    static final String TAG = AESHandler.class.getSimpleName();
    static SecretKeySpec sks = null;

    public static void init(String deviceId1) {
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(deviceId1.getBytes());
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128, sr);
            sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
        } catch (Exception e) {
            Log.e(TAG, "AES secret key spec error");
        }
    }

    public static String encrypt(String rawSting) {
        // Encode the original data with AES
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            if (sks == null)
                init("RAWDEVICEID");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(rawSting.getBytes());
        } catch (Exception e) {
            Log.e(TAG, "AES encryption error");
        }
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
    }

    public static String fromBase64(String rawString) {
        return new String(Base64.decode(rawString.getBytes(), Base64.NO_WRAP));
    }

    public static String toBase64(String base64String) {
        return Base64.encodeToString(base64String.getBytes(), Base64.NO_WRAP);
    }


    public static String decrypt(String rawString) {
        // Decode the encoded data with AES
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            if (sks == null)
                init("RAWDEVICEID");
            c.init(Cipher.DECRYPT_MODE, sks);
            decodedBytes = c.doFinal(rawString.getBytes());
        } catch (Exception e) {
            Log.e(TAG, "AES decryption error");
        }
        return new String(decodedBytes);

    }

}
