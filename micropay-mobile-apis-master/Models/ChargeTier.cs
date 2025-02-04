using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
 
namespace micropay_apis.Models
{
    public class ChargeTier
    {
        public long serialNo;
        public double fromAmount;
        public double toAmount;
        public double tierAmount;
        public double? craftSilconAmt;
        public long employeeId;
        public string createdBy;
    }
}