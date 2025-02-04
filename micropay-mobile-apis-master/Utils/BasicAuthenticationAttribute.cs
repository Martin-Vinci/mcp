using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Security.Principal;
using System.Threading;
using System.Web;
using System.Web.Http.Controllers;
using System.Web.Http.Filters;


namespace micropay_apis.Utils
{

	public class BasicAuthenticationAttribute : AuthorizationFilterAttribute
	{
		private const string Realm = "My Realm";

		public override void OnAuthorization(HttpActionContext actionContext)
		{
			//If the Authorization header is empty or null
			//then return Unauthorized

			string ipAddress = HttpContext.Current.Request.UserHostAddress;		
			if (ipAddress == null)
			{
				actionContext.Response = actionContext.Request
					.CreateResponse(HttpStatusCode.Unauthorized);
				// If the request was unauthorized, add the WWW-Authenticate header 
				// to the response which indicates that it require basic authentication
				if (actionContext.Response.StatusCode == HttpStatusCode.Unauthorized)
				{
					actionContext.Response.Headers.Add("WWW-Authenticate",
						string.Format("Basic realm=\"{0}\"", Realm));
				}
			}
			else
			{
				var ipAddressString = HttpContext.Current.Request.UserHostAddress;
				if (!allowIP(ipAddressString))
				{
					actionContext.Response = actionContext.Request
				  .CreateResponse(HttpStatusCode.Unauthorized);
					LOGGER.error("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx UnAuthorized  Client IP Address: " + ipAddressString);
				}
				else
				{
					LOGGER.error("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx Authorized  Client IP Address: " + ipAddressString);
					var identity = new GenericIdentity(ipAddress);
					IPrincipal principal = new GenericPrincipal(identity, null);
					Thread.CurrentPrincipal = principal;
					if (HttpContext.Current != null)
					{
						HttpContext.Current.User = principal;
					}
				}
			}
		}
		 
		public static bool allowIP(string ipAddressString)
		{
			var whiteListedIPs = PROPERTIES.WhiteListedIPAddresses;
			if (!string.IsNullOrEmpty(whiteListedIPs))
			{
				var whiteListIPList = whiteListedIPs.Split(',').ToList();	
				var ipAddress = IPAddress.Parse(ipAddressString);
				var isInwhiteListIPList =
						whiteListIPList
							.Where(a => a.Trim()
							.Equals(ipAddressString, StringComparison.InvariantCultureIgnoreCase))
							.Any();
				return isInwhiteListIPList;
			}
			return true;
		}
	}
}