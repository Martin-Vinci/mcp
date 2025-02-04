package com.greybox.mediums.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.greybox.mediums.config.RestTemplateConfig;
import com.greybox.mediums.config.SchemaConfig;
import com.greybox.mediums.entities.Biller;
import com.greybox.mediums.entities.BillerProduct;
import com.greybox.mediums.entities.BillerProductCategory;
import com.greybox.mediums.models.InterswitchPaymentItem;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.BillerProductCategoryRepo;
import com.greybox.mediums.repository.BillerProductRepo;
import com.greybox.mediums.repository.BillerRepo;
import com.greybox.mediums.utils.DataUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static com.greybox.mediums.utils.Logger.logError;

@Service
public class BillerService {

    @Autowired
    private BillerRepo billerRepo;
    @Autowired
    private BillerProductCategoryRepo billerProductCategoryRepo;
    @Autowired
    private BillerProductRepo billerProductRepo;
    @Autowired
    private SchemaConfig schemaConfig;

    public TxnResult find(Biller request) {
        List<Biller> customers = billerRepo.findBillers();
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }


    public TxnResult findBillerByBillerCode(Biller request) {
        Biller customers = billerRepo.findBillerByBillerCode(request.getBillerCode());
        if (customers == null)
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult save(Biller request) {
        billerRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(Biller request) {
        billerRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult findBillerProductsByBillerId(BillerProduct request) {
        List<BillerProduct> customers = billerProductRepo.findBillerProductsByBillerId(request);
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No packages have been configured for this biller.")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult findMobileBillerProductsByBiller(BillerProduct request) {
        List<BillerProduct> customers = billerProductRepo.findMobileBillerProductsByBiller(request);
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No packages have been configured for this biller.")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult saveBillerProduct(BillerProduct request) {
        request.setCreateDate(DataUtils.getCurrentDate().toLocalDate());
        billerProductRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult findBillerProductCategoryByBillerId(BillerProductCategory request) {
        List<BillerProductCategory> customers = billerProductCategoryRepo
                .findBillerProductCategoryById(request.getBillerId());
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No package categories have been configured for this biller")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult findProductCategoryByBillerCode(Biller request) {
        List<BillerProductCategory> customers = billerProductCategoryRepo
                .findProductCategoryByBillerCode(request.getBillerCode());
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult saveBillerProductCategory(BillerProductCategory request) {
        billerProductCategoryRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void syncInterswitchPaymentItems() {
        try {
            List<Biller> billerList = billerRepo.findBillerForPriceUpdate();
            if (billerList == null || billerList.isEmpty()) {
                return;
            }
            RestTemplateConfig restTemplate = new RestTemplateConfig();
            final String baseUrl = schemaConfig.getMcpGateWayURL() + "/BillPayment/getInterswitchBillerPaymentList";
            HashMap<String, Object> requestData;
            JSONObject jsonObject;
            String description;
            Gson gson = new Gson();
            // Define the type of the list
            TypeToken<List<InterswitchPaymentItem>> typeToken = new TypeToken<List<InterswitchPaymentItem>>() {};
            logError("========================>>>> Starting Biller Product Price Syncing.");
            for (Biller biller : billerList) {
                requestData = new HashMap<>();
                if (!isNumeric(biller.getAcctNo()))
                    continue;
                requestData.put("billerProdCode", biller.getAcctNo().trim());
                jsonObject = new JSONObject(requestData);
                String jsonRequest = jsonObject.toString();
                TxnResult wsResponse = restTemplate.post(jsonRequest, baseUrl, "POST", "eQuiWeb");
                if (!wsResponse.getCode().equals("00"))
                    continue;

                String jsonArray = (String) wsResponse.getData();
                if (jsonArray == null)
                    continue;
                List<InterswitchPaymentItem> objectList = gson.fromJson(jsonArray, typeToken.getType());
                for (InterswitchPaymentItem item : objectList) {
                    description = item.getPaymentitemname();// + " " + item.getItemCurrencySymbol() + " " + item.getAmount()

                    // Check If Biller Product ID already exists.
                    // BillerProduct product = billerProductRepo.findBillerProduct(item.getPaymentitemid().toString(), biller.getId());
                    BigDecimal amount = item.getAmount().divide(BigDecimal.valueOf(100));
                    billerProductRepo.updateBillerProductPrice(item.getPaymentCode(), biller.getId(), amount, description);
                }
                biller.setLastUpdateDate(DataUtils.getCurrentTimeStamp().toLocalDateTime());
                billerProductRepo.updateBillerProductPrice(biller.getId());
            }
            logError("========================>>>> Completing Biller Product Price Syncing.");
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
}
