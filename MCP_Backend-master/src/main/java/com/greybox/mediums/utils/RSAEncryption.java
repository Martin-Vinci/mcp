package com.greybox.mediums.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.DestroyFailedException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class RSAEncryption {
    private static String keyStorePassword = "Micro@21%";
    private static String alias = "micropay";
    public static PublicKey getPublicKey(String base64PublicKey) {
        PublicKey publicKey = null;
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey.getBytes()));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    public static PrivateKey getPrivateKey(String base64PrivateKey) {
        PrivateKey privateKey = null;
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64PrivateKey.getBytes()));
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    public static String signData(final String plainText) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, SignatureException {
        final char[] keystpassw = keyStorePassword.toCharArray();
        // --- retrieve private key from store
        final PrivateKey privateKey;
        try (final FileInputStream is = new FileInputStream(
                "D:\\Paul\\SITES\\Edgar\\mediums-backend\\certificate\\configured_test\\Micropay_test.p12")) {
            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(is, keystpassw);
            privateKey = (PrivateKey) ks.getKey(alias, keystpassw);
        } catch (final KeyStoreException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException e) {
            throw new IllegalStateException("Could not load key from key store", e);
        }

        Signature privateSignature = Signature.getInstance("SHA1withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(plainText.getBytes("UTF-8"));
        byte[] signature = privateSignature.sign();
        return Base64.getEncoder().encodeToString(signature);
    }


    public static String decrypt(final String encrptedString) throws IllegalBlockSizeException,
            BadPaddingException {
        // --- setup (should be stored differently)
        final char[] keystpassw = keyStorePassword.toCharArray();
        // --- retrieve private key from store
        final PrivateKey privateKey;
        try (final FileInputStream is = new FileInputStream(
                "D:\\Paul\\SITES\\Edgar\\mediums-backend\\certificate\\configured_test\\Micropay_test.p12")) {
            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(is, keystpassw);
            privateKey = (PrivateKey) ks.getKey(alias, keystpassw);
        } catch (final KeyStoreException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException e) {
            throw new IllegalStateException("Could not load key from key store", e);
        }
        // --- initialize cipher
        final Cipher cipher;
        try {
            // should use OAEP instead
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (final NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException(
                    "RSA PKCS v1.5 should always be available", e);
        } catch (final InvalidKeyException e) {
            throw new IllegalStateException("Key is not an RSA private key", e);
        }
        // --- decode
        final byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(encrptedString);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid encoded ciphertext", e);
        }

        // --- decrypt
        final byte[] cipherData = cipher.doFinal(decoded);
        final String cardNo = new String(cipherData, StandardCharsets.US_ASCII);

        // --- clean up
        try {
            privateKey.destroy();
        } catch (final DestroyFailedException e) {
            // we tried, possibly log this
        }
        Arrays.fill(cipherData, (byte) 0);

        return cardNo;
    }

    public static void main(String[] args) throws IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, BadPaddingException {
        try {
            String encryptedString = Base64.getEncoder().encodeToString(signData("Dhiraj is the author").getBytes());
            System.out.println(encryptedString);
            String decryptedString = RSAEncryption.decrypt(encryptedString);
            System.out.println(decryptedString);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | SignatureException e) {
            System.err.println(e.getMessage());
        }

    }

}
