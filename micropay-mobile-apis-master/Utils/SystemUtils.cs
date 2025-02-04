using Newtonsoft.Json;

namespace micropay_apis.Utils
{
    public class SystemUtils
    {
        public static JsonSerializerSettings getJsonSettings()
        {
            var settings = new JsonSerializerSettings
            {
                NullValueHandling = NullValueHandling.Ignore,
                Formatting = Formatting.Indented,
            };
            return settings;
        }
    }
}
