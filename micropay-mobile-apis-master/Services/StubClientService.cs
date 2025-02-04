using micropay_apis.equiweb.apis;
using micropay_apis.Remote;

namespace micropay_apis.Services
{
	public class StubClientService
	{
		public static PegasusService pegasusService;
		public static InterswitchService interswitchService;
		public static DTBService dtbService;
		public static ChannelIntegrator equiwebService;
		public static PreTUPAirtelService pretupService;
		public static MTNServices mtnServices;
		public static MediumsService mediumsService;
		public static LycaMobileService lycaMobileService;
		public static AirtelMoneyService airtelMoneyService; 
        public static SurepayService surepayService;
        public static CenteESBService centeESBService;
		public static void initialize() 
		{
			pegasusService = new PegasusService();
			pretupService = new PreTUPAirtelService();
			interswitchService = new InterswitchService();
			equiwebService = new ChannelIntegrator();
			mtnServices = new MTNServices();
			dtbService = new DTBService();
			mediumsService = new MediumsService();
			lycaMobileService = new LycaMobileService();
			centeESBService = new CenteESBService();
			airtelMoneyService = new AirtelMoneyService();
            surepayService = new SurepayService();
        }
	}
}