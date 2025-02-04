using System.Web.Http;
using System.Web.Mvc;
using System.Web.Optimization;
using System.Web.Routing;
using micropay_apis.Services;
using log4net;
using micropay_apis.Utils;
 
namespace micropay_apis
{
	public class WebApiApplication : System.Web.HttpApplication
	{
		protected void Application_Start()
		{
			log4net.Config.XmlConfigurator.Configure();
			AreaRegistration.RegisterAllAreas();
			GlobalConfiguration.Configure(WebApiConfig.Register);
			FilterConfig.RegisterGlobalFilters(GlobalFilters.Filters);
			RouteConfig.RegisterRoutes(RouteTable.Routes);
			BundleConfig.RegisterBundles(BundleTable.Bundles);
			StubClientService.initialize();
		}
	}
}
