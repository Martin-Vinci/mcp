package com.greybox.mediums.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import java.util.Base64;


/**
 * Class for encrypting/decrypting texts using AES encryption
 *
 * @author Manjula AW
 */
public class AESEncryptor {
    /**
     * Reference to the cipher
     */
    private Cipher cipher;

    /**
     * 8 byte salt to be used is stored in this array
     */
    private final byte[] salt = {-87, -101, -56, 50, 86, 53, -29, 3};

    /**
     * iteration count
     */
    private static final int ITERATION_COUNT = 1024;

    /**
     * Key Strengh
     */
    private static final int KEY_STRENGTH = 128;

    /**
     * iteration count
     */
    private static SecretKey key;

    /**
     * Initialization Vector
     */
    private static final byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    /**
     * Default password phrase
     */

    public AESEncryptor(String aesKey) throws Exception {
        super();
        // Create SecretKeyFactory
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        // Create PBEKeySpec
        KeySpec spec = new PBEKeySpec(aesKey.toCharArray(), salt, ITERATION_COUNT, KEY_STRENGTH);
        // Create SecretKey
        SecretKey tmp = factory.generateSecret(spec);
        // Create SecretKeySpec
        key = new SecretKeySpec(tmp.getEncoded(), "AES");
        // Get Cipher Instance
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    /**
     * Encrypts a clear text string
     *
     * @param plainStr the clear text string
     * @return the encrypted String
     * @throws Exception if any error occurs
     */
    public String encrypt(final String plainStr) throws Exception {
        if (plainStr == null) {
            throw new IllegalArgumentException("The argument 'plainStr' is null");
        }
        // IvParameterSpec is a wrapper for an initialization vector
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        // Initialize Cipher for ENCRYPT_MODE
        cipher.init(Cipher.ENCRYPT_MODE, key, ivspec);
        // Encrypt
        byte[] utf8EncryptedData = cipher.doFinal(plainStr.getBytes());
        // Encode bytes to base64 to get a string
        String base64EncryptedData = Base64.getEncoder().encodeToString(utf8EncryptedData);
        return base64EncryptedData;
    }

    /**
     * Decrypts a previously encrypted string
     *
     * @param encryptedStr the encrypted string
     * @return the clear text String
     */
    public String decrypt(final String encryptedStr) throws Exception {
        if (encryptedStr == null) {
            throw new IllegalArgumentException("The argument 'encryptedStr' is null");
        }
        // IvParameterSpec is a wrapper for an initialization vector
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        // Initialize Cipher for DECRYPT_MODE
        cipher.init(Cipher.DECRYPT_MODE, key, ivspec);
        // Decode base64 to get bytes
        byte[] decryptedData = Base64.getDecoder().decode(encryptedStr);
        // Decrypt the decoded bytes
        byte[] utf8 = cipher.doFinal(decryptedData);
        // Decode using utf-8 to get the final plain string object
        return new String(utf8, "UTF8");
    }

    public static void main(String args[]) throws Exception {
        AESEncryptor decrypter = new AESEncryptor("23232323");
        String encrypted = decrypter.encrypt("tEn_C0nn_3v1L");// ozRsaYN5xMMR/Yf24JxLTA==,terbBYpcZIcgFRvvTgsP6A==
        System.out.println("encrypted==>" + encrypted);
        String decrypted = decrypter.decrypt(encrypted);
        System.out.println("decrypted==>" + decrypted);
    }
}

