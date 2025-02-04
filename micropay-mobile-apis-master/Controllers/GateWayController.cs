using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using micropay_apis.APIModals;
using micropay_apis.EQUIWEB_API;
using micropay_apis.Models;
using micropay_apis.Remote;
using micropay_apis.Services;
using micropay_apis.Utils;

namespace micropay_apis.Controllers
{
    public class GateWayController : ApiController
    {
		private readonly GateWayService gateWayService = new GateWayService();

        [HttpPost]
        public HttpResponseMessage processServiceRequest([FromBody] ServiceRequestExport request)
        {
            ServiceResponseImport response = new ServiceResponseImport();
			HttpResponseMessage message;
			try
			{ 
				LOGGER.objectInfo(request);
				//gateWayService.pinAuthentication(request);
				LOGGER.objectInfo(request);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				ResponseMessage resp = e.getErrorMessage();
				response.responseMessage = resp.responseMessage;
				response.responseCode = resp.responseCode;
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				LOGGER.objectInfo(request);
				ResponseMessage resp = MESSAGES.getUndefinedMessage();
				response.responseMessage = resp.responseMessage;
				response.responseCode = resp.responseCode;
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
        }
    }
}
