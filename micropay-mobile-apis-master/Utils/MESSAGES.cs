using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.Models;

namespace micropay_apis.Utils
{
	public class MESSAGES
	{
        public static ResponseMessage getSuccessMessage()
        {
            ResponseMessage response = new ResponseMessage();
            response.responseCode = "0";
            response.responseMessage = "success";
            return response;
        }

        public static ResponseMessage getLoginFailedPassword()
        {
            ResponseMessage response = new ResponseMessage();
            response.responseCode = "-99";
            response.responseMessage = "Invalid UserName or Password";
            return response;
        }
        public static ResponseMessage getLoginUpdateFailedPassword()
        {
            ResponseMessage response = new ResponseMessage();
            response.responseCode = "-99";
            response.responseMessage = "Invalid password input, please correct current password";
            return response;
        }
        public static ResponseMessage getNoUserFound()
        {
            ResponseMessage response = new ResponseMessage();
            response.responseCode = "-99";
            response.responseMessage = "User Not found, please check your username";
            return response;
        }

        public static ResponseMessage getRecordNotFound()
        {
            ResponseMessage response = new ResponseMessage();
            response.responseCode = "-11";
            response.responseMessage = "No record(s) found";
            return response;
        }
        public static ResponseMessage getUndefinedMessage()
        {
            ResponseMessage response = new ResponseMessage();
            response.responseCode = "-99";
            response.responseMessage = "An error has occurred, please contact System Admin";
            return response;
        }
        public static ResponseMessage getDatabaseErrorMessage()
        {
            ResponseMessage response = new ResponseMessage();
            response.responseCode = "-99";
            response.responseMessage = "Database is unreachable, please contact System Admin";
            return response;
        }

        public static ResponseMessage getFailedToSaveRecordMessage()
        {
            ResponseMessage response = new ResponseMessage();
            response.responseCode = "-22";
            response.responseMessage = "Failed to save record";
            return response;
        }
    }
}