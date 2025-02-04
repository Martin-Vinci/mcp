using System;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using System.Text;

namespace micropay_apis.Utils
{
	public class DIGITAL_SIGNATURE
	{ 
        private static string certificate = System.Configuration.ConfigurationManager.AppSettings["certificate"]; //normally has both private and public key

        private static X509Certificate2 getPrivateKeyBankCert()
        {
            string certname = "MICROPAY";
            X509Certificate2 x509_2 = null;
            X509Store store = new X509Store(StoreName.My);// .Root
            store.Open(OpenFlags.ReadOnly);
            if (certname.Length > 0)
            {
                foreach (X509Certificate2 cert in store.Certificates)
                {
                    if (cert.SubjectName.Name.Contains(certname))
                    {
                        x509_2 = cert;
                        return x509_2;
                    }
                }

                if (x509_2 == null)
                    return null;
            }
            else
            {
                x509_2 = store.Certificates[0];
                return x509_2;
            }
            return null;
        }  


        public static string sha1(string plainText)
        {
            string hash = "";
            using (SHA1 sha256 = SHA1.Create())
            {
                byte[] sourceBytes = Encoding.UTF8.GetBytes(plainText);
                byte[] hashBytes = sha256.ComputeHash(sourceBytes);
                hash = BitConverter.ToString(hashBytes).Replace("-", String.Empty);
            }
            return hash;
        }
    }
}