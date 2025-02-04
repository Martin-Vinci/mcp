
package com.greybox.mediums.services.yoUganda;


//import org.apache.commons.net.util.Base64;

/**
 * @author Munaawa Philip (swiftugandan@gmail.com) This class is a sample
 *         application of the Yo! Payments API (https://payments.yo.co.ug)
 */
public class YoPaymentsSample {

    /**
     * @param args
     */
    public final static void main(String[] args) throws Exception {

        YoPaymentsAPIClient yoPaymentsClient = new YoPaymentsAPIClient("90008748831", "3935389056");

        // String inputXML
        // =yoPaymentsClient.createWithdrawalXml(1000,"25677123456","Narrative 6");
        String inputXML = yoPaymentsClient.createWithdrawalXml(10000, "25677123456", "",
                "This is a Narrative", "", "1298988", "328989");
        // String inputXML =
        // yoPaymentsClient.createDepositXml(1000000,"25677123456",
        // "Narrative 7");
        // String inputXML = yoPaymentsClient.createBalanceCheckXml();

        String serviceUrl = "https://sandbox.yo.co.ug/services/yopaymentsdev/task.php";

        YoPaymentsResponse yoPaymentsResponse = yoPaymentsClient
                .executeYoPaymentsRequest(inputXML, serviceUrl);

        System.out.println(yoPaymentsResponse.toString());

    }

}
