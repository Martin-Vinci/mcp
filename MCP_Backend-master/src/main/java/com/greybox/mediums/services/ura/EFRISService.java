package com.greybox.mediums.services.ura;

import com.google.gson.Gson;
import com.greybox.mediums.config.SchemaConfig;
import com.greybox.mediums.models.efris.*;
import com.greybox.mediums.utils.RSAEncryption;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@Service
public class EFRISService {

    @Autowired
    SchemaConfig schemaConfig;

    private static SimpleDateFormat isoTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        MathContext mathContext = new MathContext(3, RoundingMode.HALF_UP);
        BigDecimal amount = new BigDecimal("925");
        BigDecimal finals = amount.setScale(2, RoundingMode.HALF_EVEN);
        System.out.println("====================================" + finals);
//        File dictionaryFile = new File("C:\\EFRIS_Static_Files\\EFRIS_Dictionary.txt");
//        String jsonString = new String(Files.readAllBytes(Paths.get(dictionaryFile.getAbsolutePath())));
//        Gson gson = new Gson();
//        JSONObject jsonObject = new JSONObject(jsonString);
//        DictionaryData taxPayer = gson.fromJson(jsonObject.toString(), DictionaryData.class);
//        String fins = "";
    }


    private EFRISDataRequest pushRequest(String apiName, EFRISDataRequest efrisRequest) throws IOException {
        JSONArray jsonArray = new JSONArray();
        String jsonInString = gson.toJson(efrisRequest);
        JSONObject jsonObject = new JSONObject(jsonInString);
        jsonArray.put(jsonObject);
        String requestJson = jsonArray.toString().replaceAll("[\\[\\]]", "");
        String apiURL = schemaConfig.getEfrisOfflineURL() + apiName;
        System.out.println("=======================================================================================");
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@ " + efrisRequest.getGlobalInfo().getInterfaceCode() + " EFRIS URL : " + apiURL);
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&& " + efrisRequest.getGlobalInfo().getInterfaceCode() + " EFRIS REQUEST : " + requestJson);
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url(apiURL)
                .post(body)
                .addHeader("content-type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        String strResponse = response.body().string();
        System.out.println("########################## " + efrisRequest.getGlobalInfo().getInterfaceCode() + " EFRIS RESPONSE : " + strResponse);
        EFRISDataRequest efrisDataRequest = gson.fromJson(strResponse, EFRISDataRequest.class);
        return efrisDataRequest;
    }

    public EFRISDataRequest prepareRequestObject(String requestMessage,
                                                 String interfaceCode,
                                                 String referenceNo,
                                                 String dataExchangeId,
                                                 String tinNo) throws Exception {
        String content = requestMessage == null ? null : Base64.getEncoder().encodeToString(requestMessage.getBytes());
        String signature = "";
        EFRISDataDescr efrisDataDescr = EFRISDataDescr.builder()
                .codeType("0")
                .encryptCode("1")
                .zipCode("0")
                .build();
        EFRISData efrisData = EFRISData.builder()
                .content(content)
                .signature(signature)
                .dataDescription(efrisDataDescr)
                .build();
        EFRISExtendField efrisExtendField = EFRISExtendField.builder()
                .responseDateFormat("dd/MM/yyyy")
                .responseTimeFormat("dd/MM/yyyy HH:mm:ss")
                .referenceNo(referenceNo)
                .build();
        EFRISGlobalInfo globalInfo = EFRISGlobalInfo.builder()
                .appId(schemaConfig.getEfrisAppId())
                .version("1.1.20191201")
                .dataExchangeId(dataExchangeId)
                .interfaceCode(interfaceCode)
                .requestCode("TP")
                .requestTime(isoTimeFormatter.format(new Date()))
                .responseCode("TA")
                .userName("admin")
                .deviceMAC("FFFFFFFFFFFF")
                .deviceNo(schemaConfig.getEfrisDeviceNo())
                .tin(tinNo)
                .brn("")
                .taxpayerID("1")
                .longitude("116.397128")
                .latitude("39.916527")
                .extendField(efrisExtendField)
                .build();
        EFRISReturnStateInfo efrisReturnStateInfo = EFRISReturnStateInfo.builder()
                .returnCode("")
                .returnMessage("")
                .build();
        return EFRISDataRequest.builder()
                .data(efrisData)
                .globalInfo(globalInfo)
                .returnStateInfo(efrisReturnStateInfo)
                .build();
    }

    private String getEfrisEncryptionKey() throws Exception {
        EFRISDataRequest request = prepareRequestObject(
                null,
                "T104",
                "1234567890",
                "12345678",
                "1004466192");
        EFRISDataRequest responseData = pushRequest("/getInformation", request);
        byte[] decodedBytes = Base64.getDecoder().decode(responseData.getData().getContent());
        String decodedString = new String(decodedBytes);
        System.out.println("xxxxxxxxxxxxxxxxxxxxx: " + decodedString);
        Gson gson = new Gson();
        EFRISKey efrisKey = gson.fromJson(decodedString.trim(), EFRISKey.class);
        String decryptedString = RSAEncryption.decrypt(efrisKey.getPassowrdDes());
        System.out.println("@@@@@@@ Final AES Key: " + decryptedString);
        return decryptedString;
    }

    public EFRISDataRequest queryTaxPayerInformation(TaxPayer taxPayer) throws Exception {
        EFRISDataRequest request = prepareRequestObject(
                gson.toJson(taxPayer),
                "T119",
                "1234567890",
                "12345678",
                "1004466192");
        EFRISDataRequest responseData = pushRequest("/getInformation", request);
        return responseData;
    }

    public EFRISDataRequest systemDictionaryUpdate() throws Exception {
        EFRISDataRequest request = prepareRequestObject(
                null,
                "T115",
                "1234567890",
                "12345678",
                "1004466192");
        EFRISDataRequest responseData = pushRequest("/getInformation", request);
        return responseData;
    }

    public EFRISDataRequest queryCommodityCategoryPagination(CommodityPage commodityPage) throws Exception {
        EFRISDataRequest request = prepareRequestObject(
                gson.toJson(commodityPage),
                "T124",
                "1234567890",
                "12345678",
                "1004466192");
        EFRISDataRequest responseData = pushRequest("/getInformation", request);
        return responseData;
    }

    public EFRISDataRequest queryAllCommodityCategory(CommodityPage commodityPage) throws Exception {
        EFRISDataRequest request = prepareRequestObject(
                null,
                "T123",
                "1234567890",
                "12345678",
                "1004466192");
        EFRISDataRequest responseData = pushRequest("/getInformation", request);
        return responseData;
    }

    public EFRISDataRequest createInvoice(InvoiceData invoiceData) throws Exception {
        EFRISDataRequest request = prepareRequestObject(
                gson.toJson(invoiceData),
                "T109",
                "1234567890",
                "12345678",
                "1004466192");
        EFRISDataRequest responseData = pushRequest("/getInformation", request);
        return responseData;
    }

    public EFRISDataRequest goodsUpload(GoodsRecord[] goodsData) throws Exception {
        EFRISDataRequest request = prepareRequestObject(
                gson.toJson(goodsData),
                "T130",
                "1234567890",
                "12345678",
                "1004466192");
        EFRISDataRequest responseData = pushRequest("/getInformation", request);
        return responseData;
    }

    public EFRISDataRequest goodsAndServiceInquiry(GoodsRecord goodsData) throws Exception {
        EFRISDataRequest request = prepareRequestObject(
                gson.toJson(goodsData),
                "T127",
                "1234567890",
                "12345678",
                "1004466192");
        EFRISDataRequest responseData = pushRequest("/getInformation", request);
        return responseData;
    }


}

