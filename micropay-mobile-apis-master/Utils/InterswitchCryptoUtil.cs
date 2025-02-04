using Org.BouncyCastle.Crypto;
using Org.BouncyCastle.Crypto.Digests;
using Org.BouncyCastle.Crypto.Encodings;
using Org.BouncyCastle.Crypto.Engines;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Serialization;

namespace micropay_apis.Utils
{
    public class InterswitchCryptoUtil
    {

        public static (RSAParameters publicKey, RSAParameters privateKey) generateRSAKeyPair()
        {
            using (RSACryptoServiceProvider rsa = new RSACryptoServiceProvider(2048))
            {
                RSAParameters publicKey = rsa.ExportParameters(false);
                RSAParameters privateKey = rsa.ExportParameters(true);
                return (publicKey, privateKey);
            }
        }

        public static string convertKeyToString(RSAParameters key)
        {
            var sw = new StringWriter();
            var xs = new XmlSerializer(typeof(RSAParameters));
            xs.Serialize(sw, key);
            return sw.ToString();
        }

        public static string signWithPrivateKey(string data)
        {
            string privateKey = PROPERTIES.interswitchPrivateKey;

            byte[] dataToSign = Encoding.UTF8.GetBytes(data);

            using (RSACryptoServiceProvider rsa = new RSACryptoServiceProvider())
            {
                rsa.FromXmlString(privateKey);
                byte[] signature = rsa.SignData(dataToSign, new SHA256CryptoServiceProvider());
                return Convert.ToBase64String(signature);
            }
        }


        public static string signWithPrivateKey(string data, string privateKey)
        {
            if (privateKey == null)
                privateKey = PROPERTIES.interswitchPrivateKey;

            byte[] dataToSign = Encoding.UTF8.GetBytes(data);

            using (RSACryptoServiceProvider rsa = new RSACryptoServiceProvider())
            {
                rsa.FromXmlString(privateKey);
                byte[] signature = rsa.SignData(dataToSign, new SHA256CryptoServiceProvider());
                return Convert.ToBase64String(signature);
            }
        }


        public static string decryptWithPrivateKey(string data)
        {
           string privateKey = PROPERTIES.interswitchPrivateKey;

            byte[] encryptedData = Convert.FromBase64String(data);
            using (RSACryptoServiceProvider rsa = new RSACryptoServiceProvider())
            {
                rsa.FromXmlString(privateKey);

                byte[] decryptedData = rsa.Decrypt(encryptedData, false);
                string decryptedText = Encoding.UTF8.GetString(decryptedData);

                return decryptedText;
            }
        }

        public static string decryptWithPrivateKey(string data, string privateKey)
        {
            byte[] encryptedData = Convert.FromBase64String(data);
            using (RSACryptoServiceProvider rsa = new RSACryptoServiceProvider())
            {
                rsa.FromXmlString(privateKey);

                byte[] decryptedData = rsa.Decrypt(encryptedData, false);
                string decryptedText = Encoding.UTF8.GetString(decryptedData);

                return decryptedText;
            }
        }

        public static string encrypt(string plaintext, string terminalKey)
        {
            using (AesCryptoServiceProvider aes = new AesCryptoServiceProvider())
            {
                aes.Mode = CipherMode.CBC;
                aes.Padding = PaddingMode.PKCS7;
                aes.Key = Convert.FromBase64String(terminalKey);
                aes.GenerateIV();

                using (ICryptoTransform encryptor = aes.CreateEncryptor())
                {
                    byte[] message = Encoding.UTF8.GetBytes(plaintext);
                    byte[] encryptedMessage;

                    using (MemoryStream ms = new MemoryStream())
                    {
                        using (CryptoStream cs = new CryptoStream(ms, encryptor, CryptoStreamMode.Write))
                        {
                            cs.Write(message, 0, message.Length);
                        }
                        encryptedMessage = ms.ToArray();
                    }

                    byte[] ivAndCiphertext = new byte[aes.IV.Length + encryptedMessage.Length];
                    Array.Copy(aes.IV, 0, ivAndCiphertext, 0, aes.IV.Length);
                    Array.Copy(encryptedMessage, 0, ivAndCiphertext, aes.IV.Length, encryptedMessage.Length);

                    return Convert.ToBase64String(ivAndCiphertext);
                }
            }
        }
    }
}
