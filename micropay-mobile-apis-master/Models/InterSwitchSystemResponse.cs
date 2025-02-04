using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace micropay_apis.Models
{
    public class InterSwitchSystemResponse<T>
    {
        public string responseCode { get; set; }
        public string responseMessage { get; set; }
        public T response { get; set; }
    }
}
