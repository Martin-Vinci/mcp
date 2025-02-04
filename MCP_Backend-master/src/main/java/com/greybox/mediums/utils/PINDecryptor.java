package com.greybox.mediums.utils;

import com.greybox.mediums.models.ErrorData;

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
public class PINDecryptor {


    public static String decrypt(String encryptedStr) {
        if (encryptedStr == null)
            throw new IllegalArgumentException("The argument 'encryptedStr' is null");
        try {
            String iv = "InitializationVe";
            String key = "YourSecretKey123";
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes("UTF-8"));
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(2, secretKeySpec, ivParameterSpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedStr));
            return new String(decrypted);
        } catch (Exception e) {
            Logger.logError(e);
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Decryption error: Error occurred  while deciphering encrypted value").build());
        }
    }


}

