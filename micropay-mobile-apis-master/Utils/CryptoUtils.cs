using System;
using System.Text;
using System.IO;
using Org.BouncyCastle.Crypto;
using Org.BouncyCastle.Crypto.Parameters;
using Org.BouncyCastle.Security;
using Org.BouncyCastle.OpenSsl;
using Org.BouncyCastle.Crypto.Engines;
using System.Security.Cryptography;
using micropay_apis.Models;

namespace micropay_apis.Utils
{
    public class CryptoUtils
    { 
        public static string encrypt(string plaintext, string terminalKey)
        {
            try
            {
                var bcProvider = new Org.BouncyCastle.Crypto.Operators.BcDigestOperatorProvider();
                byte[] message = Encoding.UTF8.GetBytes(plaintext);
                byte[] iv = Hex.Decode(UtilMethods.RandomBytesHexEncoded(16));
                var ivParameterSpec = new Parameters.ParametersWithIV(new KeyParameter(Hex.Decode(terminalKey)), iv);
                var cipher = CipherUtilities.GetCipher("AES/CBC/PKCS7Padding");
                cipher.Init(true, ivParameterSpec);
                byte[] secret = cipher.DoFinal(message);
                var concatenated = new MemoryStream(iv.Length + secret.Length);
                concatenated.Write(iv, 0, iv.Length);
                concatenated.Write(secret, 0, secret.Length);
                return Convert.ToBase64String(concatenated.ToArray());
            }
            catch (Exception e)
            {
                Console.WriteLine("Exception trace " + e);
                throw new SystemApiException(PhoenixResponseCodes.INTERNAL_ERROR.CODE, "Failure to encrypt object");
            }
        }
         
        public static string decrypt(byte[] encryptedValue, string terminalKey)
        {
            try
            {
                var secretKeyBytes = Convert.FromBase64String(terminalKey);
                var iVbytes = new byte[16];
                var encryptedBytes = new byte[encryptedValue.Length - 16];
                Array.Copy(encryptedValue, 0, iVbytes, 0, 16);
                Array.Copy(encryptedValue, 16, encryptedBytes, 0, encryptedValue.Length - 16);
                var ivParameterSpec = new Parameters.ParametersWithIV(new KeyParameter(secretKeyBytes), iVbytes);
                var cipher = CipherUtilities.GetCipher("AES/CBC/PKCS7Padding");
                cipher.Init(false, ivParameterSpec);
                byte[] clear = cipher.DoFinal(encryptedBytes);
                return Encoding.UTF8.GetString(clear);
            }
            catch (Exception e)
            {
                Console.WriteLine("Exception trace " + e);
                throw new SystemApiException(PhoenixResponseCodes.INTERNAL_ERROR.CODE, "Failure to decrypt object");
            }
        }
         
        public static string decryptWithPrivate(string plaintext)
        {
            try
            {
                var message = Convert.FromBase64String(plaintext);
                var engine = new RsaEngine();
                engine.Init(false, GetRsaPrivate());
                byte[] secret = engine.ProcessBlock(message, 0, message.Length);
                return Encoding.UTF8.GetString(secret);
            }
            catch (Exception e)
            {
                Console.WriteLine("Exception trace " + e);
                throw new SystemApiException(PhoenixResponseCodes.INTERNAL_ERROR.CODE, "Failure to decryptWithPrivate ");
            }
        } 

        public static string decryptWithPrivate(byte[] message, AsymmetricKeyParameter privateKey)
        {
            try
            {
                var engine = new RsaEngine();
                engine.Init(false, privateKey);
                byte[] secret = engine.ProcessBlock(message, 0, message.Length);
                return Encoding.UTF8.GetString(secret);
            }
            catch (Exception e)
            {
                Console.WriteLine("Exception trace " + e);
                throw new MediumsException(new ResponseMessage("-99", "Failure to decryptWithPrivate");
            }
        }
         
        public static string decryptWithPrivate(string plaintext, string privateKey)
        {
            var message = Convert.FromBase64String(plaintext);
            return decryptWithPrivate(message, GetRsaPrivate(privateKey));
        }
         
        public static string encryptWithPrivate(string plaintext)
        {
            try
            {
                var message = Encoding.UTF8.GetBytes(plaintext);
                var engine = new RsaEngine();
                engine.Init(true, GetRsaPrivate());
                byte[] secret = engine.ProcessBlock(message, 0, message.Length);
                return Convert.ToBase64String(secret);
            }
            catch (Exception e)
            {
                Console.WriteLine("Exception trace " + e);
            }
        }

        public static AsymmetricKeyParameter GetRsaPrivate()
        {
            return GetRsaPrivate(Constants.PRIKEY.Trim());
        }

        public static AsymmetricKeyParameter GetRsaPrivate(string privateKey)
        {
            try
            {
                var keyBytes = Convert.FromBase64String(privateKey.Trim());
                using (var reader = new PemReader(new StreamReader(new MemoryStream(keyBytes))))
                {
                    var keyPair = (AsymmetricCipherKeyPair)reader.ReadObject();
                    return keyPair.Private;
                }
            }
            catch (Exception e)
            {
                Console.WriteLine("Exception trace " + e);
                throw new SystemApiException(PhoenixResponseCodes.INTERNAL_ERROR.CODE, "Failure to getRsaPrivate ");
            }
        }

        public static string SignWithPrivateKey(string data)
        {
            return SignWithPrivateKey(data, GetRsaPrivate());
        }

        public static string SignWithPrivateKey(string data, string privateKey)
        {
            return SignWithPrivateKey(data, GetRsaPrivate(privateKey));
        }

        public static string SignWithPrivateKey(string data, AsymmetricKeyParameter privateKey)
        {
            try
            {
                if (string.IsNullOrEmpty(data))
                    return "";
                var dataBytes = Encoding.UTF8.GetBytes(data);
                var signer = SignerUtilities.GetSigner("SHA-256withRSA");
                signer.Init(true, privateKey);
                signer.BlockUpdate(dataBytes, 0, dataBytes.Length);
                var signature = signer.GenerateSignature();
                return Convert.ToBase64String(signature);
            }
            catch (Exception e)
            {
                Console.WriteLine("Exception trace " + e);
                throw new SystemApiException(PhoenixResponseCodes.INTERNAL_ERROR.CODE, "Failure to signWithPrivateKey ");
            }
        }

        public static bool VerifySignature(string signature, string message)
        {
            var pubKey = GetPublicKey(Constants.PUBKEY);
            return VerifySignature(signature, message, pubKey);
        }


        public static Tuple<string, string> generateKeyPair()
        {
            int keySize = 2048; // Key length in bits
            using (RSACryptoServiceProvider rsa = new RSACryptoServiceProvider(keySize))
            {
                // Export the public key
                string publicKey = rsa.ToXmlString(false);
                // Export the private key (if needed)
                string privateKey = rsa.ToXmlString(true);
                Console.WriteLine("RSA public key:");
                Console.WriteLine(publicKey);
                return Tuple.Create(publicKey, privateKey);
            }
        }

        public static AsymmetricKeyParameter GetPublicKey(string publicKeyContent)
        {
            try
            {
                var keyBytes = Convert.FromBase64String(publicKeyContent);
                var keySpecX509 = new X509EncodedKeySpec(keyBytes);
                var kf = KeyFactory.GetInstance("RSA");
                return kf.GeneratePublic(keySpecX509);
            }
            catch (Exception e)
            {
                Console.WriteLine("Exception trace " + e);
                throw new SystemApiException(PhoenixResponseCodes.INTERNAL_ERROR.CODE, "Failure to getPublicKey ");
            }
        }
    }
}
