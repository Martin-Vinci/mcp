package com.greybox.mediums.inter_switch;

import com.greybox.mediums.inter_switch.dto.JSONDataTransform;
import com.greybox.mediums.inter_switch.dto.PaymentRequest;
import com.greybox.mediums.inter_switch.dto.SystemResponse;
import com.greybox.mediums.inter_switch.utils.AuthUtils;
import com.greybox.mediums.inter_switch.utils.Constants;
import com.greybox.mediums.inter_switch.utils.CryptoUtils;
import com.greybox.mediums.inter_switch.utils.HttpUtil;
import com.greybox.mediums.models.ISWBillerItem;
import com.greybox.mediums.repository.BillerProductRepo;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentsService {
    @Autowired
    BillerProductRepo billerProductRepo;

    public String validateCustomer(PaymentRequest request) throws Exception {
        String endpointUrl = Constants.ROOT_LINK + "/api/v1/phoenix/sente/customerValidation";
        request.setTerminalId(Constants.TERMINAL_ID);
        ISWBillerItem iswBillerItem = this.billerProductRepo.findISWPaymentItem(request.getBillerCode(), String.valueOf(request.getPaymentCode()));
        if (iswBillerItem == null) {
            SystemResponse systemResponse = new SystemResponse();
            systemResponse.setResponseCode("-3452");
            long var10001 = request.getPaymentCode();
            systemResponse.setResponseMessage("Unable to locate Product code [" + var10001 + "] for Biller Code [" + request.getBillerCode() + "] from MCP");
            return JSONDataTransform.marshall(systemResponse);
        } else {
            String billerId = iswBillerItem.getBillerId();
            String itemId = String.valueOf(request.getPaymentCode());
            Long paymentCode = Long.valueOf(billerId.length() + billerId + itemId);
            request.setPaymentCode(paymentCode);
            if (request.getOtp() != null && request.getOtp().length() > 3) {
                request.setOtp(CryptoUtils.encrypt(request.getOtp(), Constants.exchangeKeys.getResponse().getTerminalKey()));
            }

            Map<String, String> headers = AuthUtils.generateInterswitchAuth(Constants.POST_REQUEST, endpointUrl, "", Constants.exchangeKeys.getResponse().getAuthToken(), Constants.exchangeKeys.getResponse().getTerminalKey());
            String jsonString = JSONDataTransform.marshall(request);
            return HttpUtil.postHTTPRequest(endpointUrl, headers, jsonString);
        }
    }

    public String makePayment(PaymentRequest request) throws Exception {
        String endpointUrl;

        // Determine the endpoint URL based on the biller code
        if (!request.getBillerCode().equalsIgnoreCase("WENRECO")
                && !request.getBillerCode().equalsIgnoreCase("TUGENDE")) {
            endpointUrl = Constants.ROOT_LINK + "/api/v1/phoenix/sente/xpayment";
        } else {
            endpointUrl = Constants.ROOT_LINK + "/api/v1/phoenix/sente/payment";
        }

        // Find the corresponding biller product for the payment request
        ISWBillerItem iswBillerItem = this.billerProductRepo.findISWPaymentItem(request.getBillerCode(), String.valueOf(request.getPaymentCode()));

        // Check if the biller item exists
        if (iswBillerItem == null) {
            // If the biller item is not found, return an error response
            SystemResponse systemResponse = new SystemResponse();
            systemResponse.setResponseCode("-3452");
            long var10001 = request.getPaymentCode();
            systemResponse.setResponseMessage("Unable to locate Product code [" + var10001 + "] for Biller Code [" + request.getBillerCode() + "] from MCP");
            return JSONDataTransform.marshall(systemResponse);
        } else {
            // Retrieve billerId and construct the paymentCode
            String billerId = iswBillerItem.getBillerId();
            String itemId = String.valueOf(request.getPaymentCode());
            Long paymentCode = Long.valueOf(billerId.length() + billerId + itemId);
            request.setPaymentCode(paymentCode);

            // Set the terminal ID for the request
            request.setTerminalId(Constants.TERMINAL_ID);

            // Construct additional data required for authentication
            double var10000 = request.getAmount();
            String additionalData = var10000 + "&" + request.getTerminalId() + "&" + request.getRequestReference() + "&" + request.getCustomerId() + "&" + request.getPaymentCode();

            // Retrieve authentication and session keys
            String authToken = Constants.exchangeKeys.getResponse().getAuthToken();
            String sessionKey = Constants.exchangeKeys.getResponse().getTerminalKey();

            // If OTP is present and valid, encrypt it
            if (request.getOtp() != null && request.getOtp().length() > 3) {
                request.setOtp(CryptoUtils.encrypt(request.getOtp(), Constants.exchangeKeys.getResponse().getTerminalKey()));
            }

            // Generate headers for the Interswitch API request
            Map<String, String> headers = AuthUtils.generateInterswitchAuth(Constants.POST_REQUEST, endpointUrl, additionalData, authToken, sessionKey);

            // Send the HTTP POST request to the payment endpoint
            String httpResponse = HttpUtil.postHTTPRequest(endpointUrl, headers, JSONDataTransform.marshall(request));

            // Check if the biller is MTN Mobile Money for further processing
            if (!request.getBillerCode().equalsIgnoreCase("MTN_MOBILE_MONEY")) {
                return httpResponse;
            } else {
                // Parse the response and check for a specific response code
                JSONObject jsonObject = new JSONObject(httpResponse);
                String responseCode = jsonObject.optString("responseCode");

                // If response code is '90009' (pending), keep checking the transaction status
                if (!responseCode.equalsIgnoreCase("90009")) {
                    return httpResponse;
                } else {
                    String checkTransactionResponse;
                    do {
                        short milliseconds = 5000;

                        // Sleep for 5 seconds before checking transaction status again
                        try {
                            Thread.sleep(milliseconds);
                        } catch (InterruptedException var18) {
                            Thread.currentThread().interrupt();
                        }

                        // Check the transaction status using the request reference
                        checkTransactionResponse = this.checkTransaction(request.getRequestReference());
                        jsonObject = new JSONObject(checkTransactionResponse);
                        responseCode = jsonObject.optString("responseCode");

                        // Keep checking until the transaction is no longer pending
                    } while (responseCode.equalsIgnoreCase("90009"));

                    return checkTransactionResponse;
                }
            }
        }
    }


    public String fetchBalance() throws Exception {
        String endpointUrl = Constants.ROOT_LINK + "/api/v1/phoenix/sente/accountBalance";
        String request = endpointUrl + "?terminalId=" + endpointUrl + "&requestReference=" + Constants.TERMINAL_ID;
        Map<String, String> headers = AuthUtils.generateInterswitchAuth(Constants.GET_REQUEST, request, "", Constants.exchangeKeys.getResponse().getAuthToken(), Constants.exchangeKeys.getResponse().getTerminalKey());
        return HttpUtil.getHTTPRequest(request, headers);
    }

    public String checkTransaction(String requestReference) throws Exception {
        String endpointUrl = Constants.ROOT_LINK + "/api/v1/phoenix/sente/status?terminalId=4MPY0001&requestReference=" + Constants.ROOT_LINK;
        Map<String, String> headers = AuthUtils.generateInterswitchAuth(Constants.GET_REQUEST, endpointUrl, "", Constants.exchangeKeys.getResponse().getAuthToken(), Constants.exchangeKeys.getResponse().getTerminalKey());

        return HttpUtil.getHTTPRequest(endpointUrl, headers);
    }

    public String getCategories() throws Exception {
        String endpointUrl = Constants.ROOT_LINK + "/infinity/qt-api/Biller/categories-by-client/4MPY0001/4MPY0001";
        Map<String, String> headers = AuthUtils.generateInterswitchAuth(Constants.POST_REQUEST, endpointUrl, "", Constants.exchangeKeys.getResponse().getAuthToken(), Constants.exchangeKeys.getResponse().getTerminalKey());

        return HttpUtil.getHTTPRequest(endpointUrl, headers);
    }

    public String getCategoryBillers(long categoryId) throws Exception {
        String endpointUrl = Constants.ROOT_LINK + "/infinity/qt-api/Biller/biller-by-category/" + Constants.ROOT_LINK;
        Map<String, String> headers = AuthUtils.generateInterswitchAuth(Constants.POST_REQUEST, endpointUrl, "", Constants.exchangeKeys.getResponse().getAuthToken(), Constants.exchangeKeys.getResponse().getTerminalKey());

        return HttpUtil.getHTTPRequest(endpointUrl, headers);
    }

    public String getPaymentItems(long billerId) throws Exception {
        String endpointUrl = Constants.ROOT_LINK + "/infinity/qt-api/Biller/partneritems/biller-id/" + Constants.ROOT_LINK;
        Map<String, String> headers = AuthUtils.generateInterswitchAuth(Constants.POST_REQUEST, endpointUrl, "", Constants.exchangeKeys.getResponse().getAuthToken(), Constants.exchangeKeys.getResponse().getTerminalKey());

        return HttpUtil.getHTTPRequest(endpointUrl, headers);
    }
}
