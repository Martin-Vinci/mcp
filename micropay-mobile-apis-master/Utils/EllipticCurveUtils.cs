using Org.BouncyCastle.Asn1.Nist;
using Org.BouncyCastle.Asn1.Pkcs;
using Org.BouncyCastle.Asn1.X509;
using Org.BouncyCastle.Asn1.X9;
using Org.BouncyCastle.Crypto;
using Org.BouncyCastle.Crypto.Agreement;
using Org.BouncyCastle.Crypto.Digests;
using Org.BouncyCastle.Crypto.Encodings;
using Org.BouncyCastle.Crypto.Engines;
using Org.BouncyCastle.Crypto.Generators;
using Org.BouncyCastle.Crypto.Parameters;
using Org.BouncyCastle.Math;
using Org.BouncyCastle.Pkcs;
using Org.BouncyCastle.Security;
using Org.BouncyCastle.X509;
using System;
using System.Security.Cryptography;
using System.Text;

namespace micropay_apis.Utils
{
    public class EllipticCurveUtils
    {
        private static string ELIPTIC_CURVE_PRIME256 = "prime256v1";
        private string protocol;

        public EllipticCurveUtils(string protocol)
        {
            this.protocol = protocol;
            //Security.AddProvider(new BouncyCastleProvider());
        }


        public AsymmetricCipherKeyPair generateKeyPair()
        {
            // Create a SecureRandom instance to provide randomness.
            SecureRandom random = new SecureRandom();

            // Choose the NIST P-256 curve (prime256v1) parameters.
            X9ECParameters curveParams = Org.BouncyCastle.Asn1.Sec.SecNamedCurves.GetByName("secp256r1");
            ECDomainParameters domainParameters = new ECDomainParameters(curveParams.Curve, curveParams.G, curveParams.N);

            // Generate the ECDH key pair using the NIST P-256 curve.
            ECKeyPairGenerator generator = new ECKeyPairGenerator();
            generator.Init(new ECKeyGenerationParameters(domainParameters, random));

            AsymmetricCipherKeyPair keyPair = generator.GenerateKeyPair();
            ECPublicKeyParameters publicKey = (ECPublicKeyParameters)keyPair.Public;
            ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters)keyPair.Private;
            return keyPair;

        }

        public string getPrivateKey(AsymmetricCipherKeyPair keyPair)
        {
            ECPublicKeyParameters publicKeyParam = (ECPublicKeyParameters)keyPair.Public;
            ECPrivateKeyParameters privateKeyParam = (ECPrivateKeyParameters)keyPair.Private;
            //string privateKey = privateKeyParam.Q.X.ToBigInteger().ToString();
            //string publicKey = publicKeyParam.Q.Y.ToBigInteger().ToString();
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfoFactory.CreatePrivateKeyInfo(privateKeyParam);
            byte[] privateKeyBytes = privateKeyInfo.ToAsn1Object().GetDerEncoded();
            return Convert.ToBase64String(privateKeyBytes);
        }


        public string getPublicKey(AsymmetricCipherKeyPair keyPair)
        {
            ECPublicKeyParameters publicKeyParam = (ECPublicKeyParameters)keyPair.Public;
            ECPrivateKeyParameters privateKeyParam = (ECPrivateKeyParameters)keyPair.Private;
            // string privateKey = privateKeyParam.Q.X.ToBigInteger().ToString();
            // string publicKey = publicKeyParam.Q.Y.ToBigInteger().ToString();
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfoFactory.CreateSubjectPublicKeyInfo(publicKeyParam);
            byte[] publicKeyBytes = publicKeyInfo.ToAsn1Object().GetDerEncoded();
            return Convert.ToBase64String(publicKeyBytes);
        }






        public AsymmetricKeyParameter LoadPublicKey(byte[] data)
        {
            X9ECParameters x9Params = NistNamedCurves.GetByName(ELIPTIC_CURVE_PRIME256);
            ECDomainParameters domainParameters = new ECDomainParameters(
                x9Params.Curve, x9Params.G, x9Params.N, x9Params.H);
            ECPublicKeyParameters publicKey = new ECPublicKeyParameters("EC", x9Params.Curve.DecodePoint(data), domainParameters);
            return publicKey;
        }

        public ECPrivateKeyParameters LoadPrivateKey(byte[] data)
        {
            X9ECParameters x9Params = NistNamedCurves.GetByName(ELIPTIC_CURVE_PRIME256);
            ECDomainParameters domainParameters = new ECDomainParameters(
                x9Params.Curve, x9Params.G, x9Params.N, x9Params.H);
            ECPrivateKeyParameters privateKey = new ECPrivateKeyParameters(new BigInteger(data), domainParameters);
            return privateKey;
        }

        public static byte[] SavePrivateKey(ECPrivateKeyParameters key)
        {
            return key.D.ToByteArray();
        }

        public static byte[] SavePublicKey(AsymmetricKeyParameter key)
        {
            ECPublicKeyParameters ecKey = (ECPublicKeyParameters)key;
            return ecKey.Q.GetEncoded(true);
        }

        public string GetSignature(string plaintext, ECPrivateKeyParameters privateKey)
        {
            ISigner ecdsaSign = SignerUtilities.GetSigner("SHA-256withECDSA");
            ecdsaSign.Init(true, privateKey);
            byte[] data = System.Text.Encoding.UTF8.GetBytes(plaintext);
            ecdsaSign.BlockUpdate(data, 0, data.Length);
            byte[] signature = ecdsaSign.GenerateSignature();
            return Convert.ToBase64String(signature);
        }

        public bool VerifySignature(string signature, string plaintext, AsymmetricKeyParameter publicKey)
        {
            ISigner ecdsaVerify = SignerUtilities.GetSigner("SHA-256withECDSA");
            ecdsaVerify.Init(false, publicKey);
            byte[] data = System.Text.Encoding.UTF8.GetBytes(plaintext);
            ecdsaVerify.BlockUpdate(data, 0, data.Length);
            byte[] signatureBytes = Convert.FromBase64String(signature);
            return ecdsaVerify.VerifySignature(signatureBytes);
        }


        public string doECDH(string privateKey, string publicKey)
        {
            byte[] privateKeyBytes = Convert.FromBase64String(privateKey);
            byte[] publicKeyBytes = Convert.FromBase64String(publicKey);
            using (ECDiffieHellman ecdh = ECDiffieHellman.Create())
            {
                ECParameters ecParameters = new ECParameters
                {
                    D = privateKeyBytes, // Private key
                    Q = new ECPoint
                    {
                        X = publicKeyBytes,
                        Y = publicKeyBytes
                    }
                };
                ecdh.ImportParameters(ecParameters);
                byte[] secret = ecdh.DeriveKeyFromHash(ecdh.PublicKey, HashAlgorithmName.SHA256);
                return Convert.ToBase64String(secret);
            }
        }
    }
}
