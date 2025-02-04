using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Web.Http;
using micropay_apis.Utils;

namespace micropay_apis.Controllers
{
    public class AppVersionController : ApiController
    {
         
        [HttpGet]
        public HttpResponseMessage appDownload()
        {
			HttpResponseMessage message = null;
			try
			{
                string fileName = PROPERTIES.MobileUpdatePath;
                LOGGER.info("App version path "+ fileName);
                FileStream stream = File.Open(fileName, FileMode.Open);
                message = new HttpResponseMessage(HttpStatusCode.OK)
                {
                    Content = new StreamContent(stream),
                };

                message.Content.Headers.ContentDisposition = new ContentDispositionHeaderValue("attachment")
                {
                    FileName = "micropay_mobile.apk"
                };

                message.Content.Headers.ContentType = new MediaTypeHeaderValue("application/octet-stream");
                return message;
            }
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
			}
			return message;
        }

        // POST: api/AppVersion
        public HttpResponseMessage appVersion([FromBody]string value)
        {
            Dictionary<string, object> response = new Dictionary<string, object>();
            response.Add("server_version", PROPERTIES.MobileVersion);
            return Request.CreateResponse(HttpStatusCode.OK, response);
        }
    }
}
