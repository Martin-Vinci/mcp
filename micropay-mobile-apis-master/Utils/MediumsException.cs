using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.Models;
using Nancy.Json;

namespace micropay_apis.Utils
{
	public class MediumsException : Exception
    {
        private ResponseMessage message;
        public MediumsException() { }

        private static string serialize(ResponseMessage message)
        {
            return new JavaScriptSerializer().Serialize(message);
        }
          
        public MediumsException(ResponseMessage message)
            : base(serialize(message))
        {
            this.message = message;
        }

        public ResponseMessage getErrorMessage()
        {
            return this.message;
        }
    }
}
