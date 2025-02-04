using System.Collections.Generic;

namespace micropay_apis.Models
{
	public class Authorization
    {
        public string requestId { get; set; }
        public string channelCode { get; set; }
        public string requestSignature { get; set; }
    }

    public class DeviceInformation
    {
        public string deviceId { get; set; }
        public string model { get; set; }
        public string imei { get; set; }
    }

    public class Entry
    {
        public string key { get; set; }
        public string value { get; set; }
    }
     
    public class CoporateAgentRequest
    {
        public Authorization authorization { get; set; }
        public DeviceInformation deviceInformation { get; set; }
        public List<Entry> entries { get; set; }
    }
}