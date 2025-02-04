using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Web;

namespace micropay_apis.Utils
{ 
	public class ENCRYPTER
	{
        static string passPhrase = "RWEQWERE2354TSFDFHRTY4531312#$%@#u!@#!@!#@##@#";
        public static string encryptWithTripleDES(string message)
        {

            UTF8Encoding utF8Encoding = new UTF8Encoding();
            MD5CryptoServiceProvider cryptoServiceProvider1 = new MD5CryptoServiceProvider();
            byte[] hash = cryptoServiceProvider1.ComputeHash(utF8Encoding.GetBytes(passPhrase));
            TripleDESCryptoServiceProvider cryptoServiceProvider2 = new TripleDESCryptoServiceProvider();
            cryptoServiceProvider2.Key = hash;
            cryptoServiceProvider2.Mode = CipherMode.ECB;
            cryptoServiceProvider2.Padding = PaddingMode.PKCS7;
            byte[] bytes = utF8Encoding.GetBytes(message);
            byte[] inArray;
            try
            {
                inArray = cryptoServiceProvider2.CreateEncryptor().TransformFinalBlock(bytes, 0, bytes.Length);
            }
            finally
            {
                cryptoServiceProvider2.Clear();
                cryptoServiceProvider1.Clear();
            }
            return Convert.ToBase64String(inArray);
        }

         
        public static string decryptWithTripleDES(string message)
        {
            UTF8Encoding utF8Encoding = new UTF8Encoding();
            MD5CryptoServiceProvider cryptoServiceProvider1 = new MD5CryptoServiceProvider();
            byte[] hash = cryptoServiceProvider1.ComputeHash(utF8Encoding.GetBytes(passPhrase));
            TripleDESCryptoServiceProvider cryptoServiceProvider2 = new TripleDESCryptoServiceProvider();
            cryptoServiceProvider2.Key = hash;
            cryptoServiceProvider2.Mode = CipherMode.ECB;
            cryptoServiceProvider2.Padding = PaddingMode.PKCS7;
            byte[] inputBuffer = Convert.FromBase64String(message);
            byte[] bytes;
            try
            {
                bytes = cryptoServiceProvider2.CreateDecryptor().TransformFinalBlock(inputBuffer, 0, inputBuffer.Length);
            }
            finally
            {
                cryptoServiceProvider2.Clear();
                cryptoServiceProvider1.Clear();
            }
            return utF8Encoding.GetString(bytes);
        }
         
        public static string encryptWithMD5(string input, bool isLowercase = false)
        {
            using (var md5 = MD5.Create())
            {
                var byteHash = md5.ComputeHash(Encoding.UTF8.GetBytes(input));
                var hash = BitConverter.ToString(byteHash).Replace("-", "");
                return (isLowercase) ? hash.ToLower() : hash;
            }
        } 

        public static string encryptMCPValue(string input)
        {
            string key = "YourSecretKey123"; // 16, 24, or 32 bytes for AES128, AES192, or AES256
            string iv = "InitializationVe"; // 16 bytes for AES
            using (Aes aesAlg = Aes.Create())
            {
                aesAlg.Key = Encoding.UTF8.GetBytes(key);
                aesAlg.IV = Encoding.UTF8.GetBytes(iv);

                ICryptoTransform encryptor = aesAlg.CreateEncryptor(aesAlg.Key, aesAlg.IV);

                using (MemoryStream msEncrypt = new MemoryStream())
                {
                    using (CryptoStream csEncrypt = new CryptoStream(msEncrypt, encryptor, CryptoStreamMode.Write))
                    {
                        using (StreamWriter swEncrypt = new StreamWriter(csEncrypt))
                        {
                            swEncrypt.Write(input);
                        }
                    }

                    return Convert.ToBase64String(msEncrypt.ToArray());
                }
            }
        }


    }

}