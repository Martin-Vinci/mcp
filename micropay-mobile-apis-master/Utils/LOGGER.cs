using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Web;
using System.Xml;
using System.Xml.Serialization;
using log4net;
using micropay_apis.APIModals;
using micropay_apis.Models;

namespace micropay_apis.Utils
{
	public class LOGGER
	{
		private static readonly ILog ILogger = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static void error(string message)
		{
			ILogger.Error(message);
			//ThreadPool.QueueUserWorkItem(task => OrbitliteController.getLogger().Error(message));
		}

		public static void info(string message)
		{
			ILogger.Info(message);
			//ThreadPool.QueueUserWorkItem(task => OrbitliteController.getLogger().Info(message));
		}

		public static void objectInfo(object objects)
		{
			try
			{
				info(toXML(objects));
			}
			catch (Exception ex)
			{
				ILogger.Error(ex.ToString());
			}
		}

		public static string toXML(object objectData)
		{
			string xmlString = "";
			try
			{
				//if (objectData is OutletAuthentication)
				//{
				//	OutletAuthentication authRequest = (OutletAuthentication)objectData;
				//	authRequest.pinNo = "XXXX";
				//	objectData = authRequest;
				//}
				//else if (objectData is SignUpRequest)
				//{
				//	SignUpRequest data = (SignUpRequest)objectData;
				//	data.customerPhoto = null;
				//	data.customerSign = null;
				//	objectData = data;
				//}
				XmlDocument xmlDoc = new XmlDocument();
				XmlSerializer xmlSerializer = new XmlSerializer(objectData.GetType());
				using (MemoryStream xmlStream = new MemoryStream())
				{
					XmlSerializerNamespaces _namespace = new XmlSerializerNamespaces();
					_namespace.Add("", "");
					xmlSerializer.Serialize(xmlStream, objectData, _namespace);
					xmlStream.Position = 0;
					xmlDoc.Load(xmlStream);
					xmlString = xmlDoc.InnerXml;
				}
			}
#pragma warning disable CS0168 // The variable 'ex' is declared but never used
			catch (Exception ex)
#pragma warning restore CS0168 // The variable 'ex' is declared but never used
			{

			}
			return xmlString;
		}
		
		public static void appError(string errorText)
		{

			try
			{
				string fileName = "_Mobile_Crush_Report";

				string filePath = PROPERTIES.ERROR_LOG_PATH + fileName + ".log";
				if (Directory.Exists(PROPERTIES.ERROR_LOG_PATH))
				{
					if (File.Exists(filePath))
					{
						FileInfo fileInfo = new FileInfo(filePath);
						long fileLength = fileInfo.Length;
						if (fileLength >= 12582912)
						{
							File.Move(filePath, PROPERTIES.ERROR_LOG_PATH + fileName + DateTime.Now.ToString("yyMMddHHssmm") + ".log");
						}
					}
				}
				else
				{
					Directory.CreateDirectory(PROPERTIES.ERROR_LOG_PATH);
				}
				using (StreamWriter Writer = new StreamWriter(filePath, true))
				{
					Writer.WriteLine(System.DateTime.Now.ToString() + " ERROR: " + errorText);
				}
			}
			catch (Exception ex)
			{

			}
		}
	}
}