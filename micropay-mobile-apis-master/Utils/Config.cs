using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using System.Web;
using micropay_apis.Utils;
using Org.BouncyCastle.Crypto.Digests;
using Org.BouncyCastle.Security;

namespace Interswitch
{
    public class Config
    {
        //public string Authorizations;
        public static readonly String Sha1 = "Sha1";
        private String clientID;
        private String secretKey;
        private String HTTPVerb;
        private String url;
        private String accessToken;
        public string SignedParameters { get; private set; }
        public string Nonce { get; private set; }
        public string PasportAuthorization { get; private set; }
        public string TimeStamp { get; set; }
        public string Authorization { get; private set; }
        public string Signature { get; set; }
        public static SecureRandom Random
        {
            get { return _random; }
            set { _random = value; }
        }
        
        private static SecureRandom _random = new SecureRandom();
        
        public long GetTimeStamp()
        {
            return (Int32)(DateTime.UtcNow.Subtract(new DateTime(1970, 1, 1))).TotalSeconds;
        }


        public static string Base64Encode(string plainText)
        {
            var plainTextBytes = Encoding.UTF8.GetBytes(plainText);
            return System.Convert.ToBase64String(plainTextBytes);
        }


        public String GetAuthorization()
        {
            String clientIdBase64 = Base64Encode(clientID);
            Authorization = "InterswitchAuth" + " " + clientIdBase64;
            return Authorization;
        }
        public String GetNonce()
        {
            Guid uuid = Guid.NewGuid();
            String nonce = uuid.ToString();
            nonce = nonce.Replace("-", "");
            return nonce;
        }


        public string GetSignature()
        {
            
            LOGGER.info("================================== Complete URL");
            LOGGER.info(url.ToString());

            StringBuilder signature = new StringBuilder(HTTPVerb);
            signature.Append("&")
                .Append(WebUtility.UrlEncode(url))
                .Append("&")
                .Append(TimeStamp)
                .Append("&")
                .Append(Nonce)
                .Append("&")
                .Append(clientID)
                .Append("&")
                .Append(secretKey);

            if(SignedParameters != null && !SignedParameters.Equals(""))
            {
                signature.Append("&")
                .Append(SignedParameters);
            }
            LOGGER.info("================================== Plain Signature");
            LOGGER.info(signature.ToString());

            string hashedSignature = ComputeHash(signature.ToString());
            LOGGER.info("================================== Hashed Signature");
            LOGGER.info(hashedSignature.ToString());
            return hashedSignature;
        }
         

        public static string ComputeHash(string input)
        {
            var data = Encoding.UTF8.GetBytes(input);
            Sha512Digest hash = new Sha512Digest();
            hash.BlockUpdate(data, 0, data.Length);
            byte[] result = new byte[hash.GetDigestSize()];
            hash.DoFinal(result, 0);
            return Convert.ToBase64String(result);
        }

        /*
        public Config(String httpVerb, String url, String clientId, String secretKey, String accessToken, String postData, String authorization)
        {
            HTTPVerb = httpVerb;
            this.url = url;
            clientID = clientId;
            this.secretKey = secretKey;
            this.accessToken = accessToken;
            PostData = postData;
            TimeStamp = GetTimeStamp().ToString();
            Nonce = GetNonce();
            Authorization = GetAuthorization();
            PasportAuthorization = authorization;
            Signature = GetSignature();
        }
        */

        public Config(String httpVerb, String url, String clientId, String secretKey, String accessToken, String signedParameters = null)
        {
            HTTPVerb = httpVerb;
            this.url = url;
            this.clientID = clientId;
            this.secretKey = secretKey;
            this.accessToken = accessToken;
            this.SignedParameters = signedParameters;
            TimeStamp = GetTimeStamp().ToString();
            Nonce = GetNonce();
            Authorization = GetAuthorization();            
            Signature = GetSignature();

        }

        public Config()
        {

        }
    }
}
