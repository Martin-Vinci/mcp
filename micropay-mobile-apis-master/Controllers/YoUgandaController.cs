using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using micropay_apis.Services;
using micropay_apis.Models;
using micropay_apis.Utils;

namespace micropay_apis.Controllers
{
    public class YoUgandaController : ApiController
    {
		YoUgandaProcessor dao = new YoUgandaProcessor();
		[HttpPost]  
		public HttpResponseMessage doAcctBalInquiry([FromBody] YoAcctBalRequest request)
		{ 
			YoBalanceResp response = new YoBalanceResp();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = dao.doBalanceInquiry(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}
		[HttpPost] 
		public HttpResponseMessage doCashOut([FromBody] YoCashRequest request)
		{
			YoCashResponse response = new YoCashResponse();
			HttpResponseMessage message;
			try
			{ 
				LOGGER.objectInfo(request);
				response = dao.doCashOut(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}
		 
		[HttpPost]
		public HttpResponseMessage doCashIn([FromBody] YoCashRequest request)
		{
			YoCashResponse response = new YoCashResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = dao.doCashIn(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}
	}
}
