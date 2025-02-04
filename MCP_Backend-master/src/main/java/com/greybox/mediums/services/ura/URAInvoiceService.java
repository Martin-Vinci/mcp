package com.greybox.mediums.services.ura;

import com.google.gson.Gson;
import com.greybox.mediums.config.SchemaConfig;
import com.greybox.mediums.entities.*;
import com.greybox.mediums.models.efris.*;
import com.greybox.mediums.models.ErrorData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.EfrisCommodityRepo;
import com.greybox.mediums.repository.EfrisInvoiceRepo;
import com.greybox.mediums.repository.UserRepo;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.MediumException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class URAInvoiceService {
    @Autowired
    EFRISService efrisService;
    @Autowired
    EfrisCommodityRepo efrisCommodityRepo;
    @Autowired
    EfrisInvoiceRepo efrisInvoiceRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private InvoiceGeneratorService invoiceGeneratorService;
    @Autowired
    private SchemaConfig schemaConfig;

    public TxnResult queryTaxPayerInformation(TaxPayer request) throws Exception {
        EFRISDataRequest responseData = efrisService.queryTaxPayerInformation(request);
        EFRISReturnStateInfo returnStateInfo = responseData.getReturnStateInfo();
        if (returnStateInfo == null || !returnStateInfo.getReturnCode().equals("00"))
            throw new MediumException(ErrorData.builder().code(returnStateInfo.getReturnCode())
                    .message(returnStateInfo.getReturnMessage()).build());
        Gson gson = new Gson();
        byte[] decodedBytes = Base64.getDecoder().decode(responseData.getData().getContent());
        String decodedString = new String(decodedBytes);
        JSONObject jsonObject = new JSONObject(decodedString);
        TaxPayer taxPayer = gson.fromJson(jsonObject.getJSONObject("taxpayer").toString(), TaxPayer.class);
        return TxnResult.builder().message("approved").
                code("00").data(taxPayer).build();
    }

    public TxnResult queryCommodityCategoryPagination() throws Exception {
        Gson gson = new Gson();
        File dir = new File("C:\\EFRIS_Static_Files");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File childFile : directoryListing) {
                if (!childFile.getAbsolutePath().contains("Commodity"))
                    continue;

                String jsonString = new String(Files.readAllBytes(Paths.get(childFile.getAbsolutePath())));
                CommodityData taxPayer = gson.fromJson(jsonString, CommodityData.class);
                for (CommodityRecord record : taxPayer.getRecords()) {
                    efrisCommodityRepo.save(EfrisCommodity.builder()
                            .commodityCategoryCode(record.getCommodityCategoryCode())
                            .commodityCategoryLevel(record.getCommodityCategoryLevel())
                            .commodityCategoryName(record.getCommodityCategoryName())
                            .parentCode(record.getParentCode())
                            .build());
                }
            }
        }
        if (1 == 1)
            return TxnResult.builder().message("approved").
                    code("00").build();

        EFRISDataRequest responseData = efrisService.queryCommodityCategoryPagination(
                CommodityPage.builder()
                        .pageNo(1)
                        .pageSize(100)
                        .build());
        EFRISReturnStateInfo returnStateInfo = responseData.getReturnStateInfo();
        if (returnStateInfo == null || !returnStateInfo.getReturnCode().equals("00"))
            throw new MediumException(ErrorData.builder().code(returnStateInfo.getReturnCode())
                    .message(returnStateInfo.getReturnMessage()).build());

        byte[] decodedBytes = Base64.getDecoder().decode(responseData.getData().getContent());
        String decodedString = new String(decodedBytes);
        JSONObject jsonObject = new JSONObject(decodedString);
        CommodityData commodityData = gson.fromJson(jsonObject.getJSONObject("taxpayer").toString(), CommodityData.class);
        for (CommodityRecord record : commodityData.getRecords()) {
            efrisCommodityRepo.save(EfrisCommodity.builder()
                    .commodityCategoryCode(record.getCommodityCategoryCode())
                    .commodityCategoryLevel(record.getCommodityCategoryLevel())
                    .commodityCategoryName(record.getCommodityCategoryName())
                    .parentCode(record.getParentCode())
                    .build());
        }
        return TxnResult.builder().message("approved").
                code("00").data(commodityData).build();
    }

    public TxnResult queryCommodityCategory(EfrisCommodity request) throws Exception {
        List<EfrisCommodity> customers = efrisCommodityRepo.findCommodityByParent(request.getParentCode());
        if (customers == null || customers.isEmpty()) {
            if (request.getParentCode().equals("0")) {
                queryCommodityCategoryPagination();
                customers = efrisCommodityRepo.findCommodityByParent(request.getParentCode());
            }
        }

        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult querySystemDictionaryUpdate() throws Exception {
        EFRISDataRequest responseData = efrisService.systemDictionaryUpdate();
        EFRISReturnStateInfo returnStateInfo = responseData.getReturnStateInfo();
        if (returnStateInfo == null || !returnStateInfo.getReturnCode().equals("00"))
            throw new MediumException(ErrorData.builder().code(returnStateInfo.getReturnCode())
                    .message(returnStateInfo.getReturnMessage()).build());

        String decodedString = responseData.getData().getContent();
        File invoicePath = filesystemRoot();
        String fileName = "EFRIS_Dictionary.txt";
        String filePath = invoicePath.getAbsolutePath() + "/" + fileName;
        System.out.println("PPPPPPAAAAAATTTTTTTTTHHHHHHH: " + filePath);
        File file;
        try {
            file = new File(filePath);
            if (file.createNewFile()) {
                System.out.println("The file is created successfully!");
            } else {
                System.out.println("The file already exists.");
            }
            Files.write(Paths.get(filePath), decodedString.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File dictionaryFile = new File("C:\\EFRIS_Static_Files\\EFRIS_Dictionary.txt");
        String jsonString = new String(Files.readAllBytes(Paths.get(dictionaryFile.getAbsolutePath())));
        Gson gson = new Gson();
        DictionaryData taxPayer = gson.fromJson(jsonString, DictionaryData.class);
        if (1 == 1)
            return TxnResult.builder().message("approved").
                    code("00").data(taxPayer).build();

        // Gson gson = new Gson();
        byte[] decodedBytes = Base64.getDecoder().decode(responseData.getData().getContent());
        decodedString = new String(decodedBytes);
        System.out.println("################# Decoded content response: " + decodedString);
        DictionaryData dictionaryData = gson.fromJson(decodedString.trim(), DictionaryData.class);
        return TxnResult.builder().message("approved").
                code("00").data(dictionaryData).build();
    }

//    public TxnResult fundsTransfer(TransactionRef request) throws Exception {
//        RestTemplateConfig restTemplate = new RestTemplateConfig();
//        final String baseUrl = schemaConfig.getEquiwebEndpoint() + "/V2/fundsTransfer";
//        HashMap<String, Object> requestData = new HashMap<>();
//        requestData.put("primaryAcctNo", request.getCrAcctNo());
//        requestData.put("contraAcctNo", request.getDrAcctNo());
//        requestData.put("paymentAmount", request.getAmount());
//        requestData.put("externalRefNo", request.getExternalTransRef());
//        requestData.put("adviceNote", request.getTransDescr());
//        requestData.put("transType", request.getTransType());
//        requestData.put("apiUserName", schemaConfig.getGateWayUserName());
//        requestData.put("apiUserPassword", schemaConfig.getGateWayPassword());
//
//        JSONObject jsonObject = new JSONObject(requestData);
//        String jsonRequest = jsonObject.toString();
//        TxnResult wsResponse = restTemplate.post(jsonRequest, baseUrl, "POST", "eQuiWeb");
//        JSONObject response = new JSONObject(wsResponse);
//        String responseCode = response.getString("responseCode").toString();
//        String responseMessage = response.getString("responseMessage").toString();
//        return TxnResult.builder().message("approved").
//                code("00").build();
//    }


    public TxnResult createInvoice(InvoiceData request) throws Exception {
        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long milliseconds = cal.getTimeInMillis();

        MathContext mathContext = new MathContext(3);
        Summary summary = request.getSummary();
        if (summary.getRemarks() == null)
            return TxnResult.builder().message("Invoice Remarks is missing").
                    code("403").build();

        summary.setNetAmount(summary.getNetAmount().setScale(2, RoundingMode.HALF_EVEN));
        summary.setGrossAmount(summary.getGrossAmount().setScale(2, RoundingMode.HALF_EVEN));
        summary.setTaxAmount(summary.getTaxAmount().setScale(2, RoundingMode.HALF_EVEN));
        request.setSummary(summary);

        ArrayList<TaxDetails> taxDetails = new ArrayList<>();
        for (TaxDetails taxDetail : request.getTaxDetails()) {
            taxDetail.setNetAmount(taxDetail.getNetAmount().setScale(2, RoundingMode.HALF_EVEN));
            taxDetail.setGrossAmount(taxDetail.getGrossAmount().setScale(2, RoundingMode.HALF_EVEN));
            taxDetail.setTaxAmount(taxDetail.getTaxAmount().setScale(2, RoundingMode.HALF_EVEN));
            taxDetails.add(taxDetail);
        }
        request.setTaxDetails(taxDetails);

        List<User> data = userRepo.findUserByLoginId(request.getCreatedBy());
        if (data.isEmpty()) {
            return TxnResult.builder().message("Invalid user details specified").
                    code("403").build();
        }

        request.setSellerDetails(SellerDetails.builder()
                .tin("1004466192")
                .ninBrn("/156627")
                .address("61 KANJOKYA MICROPAY UGANDA KAMWOKYA KAMPALA KAMPALA CENTRAL DIVI KAMPALA CENTRAL DIVISION KAMWOKYA I ")
                .branchId(schemaConfig.getEfrisBranchId())
                .businessName("MICROPAY(U) LTD")
                .emailAddress("office@micropay.co.ug")
                .isCheckReferenceNo("0")
                .legalName("MICROPAY(U) LTD")
                .linePhone("256702461049")
                .mobilePhone("256702461049")
                .referenceNo(String.valueOf(milliseconds))
                .ninBrn("")
                .build());
        request.setBasicInformation(BasicInformation.builder()
                .invoiceType("1")
                .invoiceKind("1")
                .dataSource("106")
                .invoiceIndustryCode("101")
                .deviceNo(schemaConfig.getEfrisDeviceNo())
                .issuedDate(dateTimeFormatter.format(new Date()))
                .operator(data.get(0).getFullName().trim())
                .currency("UGX")
                .build());

        EfrisInvoice efrisInvoice = saveInvoice(request);
        EFRISDataRequest responseData = efrisService.createInvoice(request);
        EFRISReturnStateInfo returnStateInfo = responseData.getReturnStateInfo();
        InvoiceData efrisDataResponse = new InvoiceData();
        String invoiceFilePath = null;
        if (returnStateInfo.getReturnCode().equals("00")) {
            Gson gson = new Gson();
            byte[] decodedBytes = Base64.getDecoder().decode(responseData.getData().getContent());
            String decodedString = new String(decodedBytes);
            efrisDataResponse = gson.fromJson(decodedString.trim(), InvoiceData.class);
            efrisInvoice.setInvoiceNo(efrisDataResponse.getBasicInformation().getInvoiceNo());
            efrisInvoice.setAntifakeCode(efrisDataResponse.getBasicInformation().getAntifakeCode());

            String invoiceNo = efrisDataResponse.getBasicInformation().getInvoiceNo();
            String fileName = invoiceNo + ".pdf";
            File invoicePath = filesystemRoot();
            invoiceFilePath = invoicePath.getAbsolutePath() + "/" + fileName;
        }

        efrisInvoice.setResponseCode(returnStateInfo.getReturnCode());
        efrisInvoice.setResponseMessage(returnStateInfo.getReturnMessage());
        efrisInvoice.setInvoicePath(invoiceFilePath);
        if (returnStateInfo.getReturnCode().equals("00"))
            invoiceGeneratorService.createPDF(invoiceFilePath, efrisDataResponse);
        efrisInvoiceRepo.save(efrisInvoice);
        if (returnStateInfo == null || !returnStateInfo.getReturnCode().equals("00"))
            throw new MediumException(ErrorData.builder().code(returnStateInfo.getReturnCode())
                    .message(returnStateInfo.getReturnMessage()).build());

        // "DR [Debit]------> GL   Cente Agent Income Receivable  \n" +
        //       "CR  [Credit]----->  VAT Payable\n" +
        //        "CR  [Credit] -----> GL 01-01-00-4050319  Cente Agent Income "

//        fundsTransfer(TransactionRef.builder()
//                .crAcctNo("01-01-00-2100401")
//                .drAcctNo("01-01-00-1990305")
//                .transType("GL2GL")
//                .amount(new BigDecimal(request.getSummary().getGrossAmount()))
//                .externalTransRef(String.valueOf(milliseconds))
//                .transDescr(request.getSummary().getRemarks())
//                .build());

        return TxnResult.builder().message("approved").
                code("00").data(efrisDataResponse).build();
    }

    public TxnResult goodsUpload(GoodsRecord request) throws Exception {
        GoodsRecord[] requestArray = new GoodsRecord[1];
        Gson gson = new Gson();
        requestArray[0] = request;
        EFRISDataRequest responseData = efrisService.goodsUpload(requestArray);
        EFRISReturnStateInfo returnStateInfo = responseData.getReturnStateInfo();
        if (returnStateInfo == null || !returnStateInfo.getReturnCode().equals("00")) {
            if (returnStateInfo.getReturnCode().equals("45")) { // Checking for Partial errors
                byte[] decodedBytes = Base64.getDecoder().decode(responseData.getData().getContent());
                String decodedString = new String(decodedBytes);
                JSONArray jsonArray = new JSONArray(decodedString);
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        GoodsRecord response = gson.fromJson(jsonArray.get(i).toString(), GoodsRecord.class);
                        if (!response.getReturnCode().equals("00"))
                            throw new MediumException(ErrorData.builder().code(response.getReturnCode())
                                    .message(response.getReturnMessage()).build());
                    }
                }
            } else
                throw new MediumException(ErrorData.builder().code(returnStateInfo.getReturnCode())
                        .message(returnStateInfo.getReturnMessage()).build());
        }

        byte[] decodedBytes = Base64.getDecoder().decode(responseData.getData().getContent());
        String decodedString = new String(decodedBytes);
        JSONArray jsonArray = new JSONArray(decodedString);
        ArrayList<GoodsRecord> listdata = new ArrayList<>();
        if (jsonArray != null) {
            GoodsRecord goodsData;
            for (int i = 0; i < jsonArray.length(); i++) {
                goodsData = gson.fromJson(jsonArray.get(i).toString(), GoodsRecord.class);
                listdata.add(goodsData);
            }
        }
        return TxnResult.builder().message("approved").
                code("00").data(listdata).build();
    }

    public TxnResult goodsAndServiceInquiry(GoodsRecord request) throws Exception {
        Gson gson = new Gson();
        request.setPageSize(50);
        request.setBranchId(schemaConfig.getEfrisBranchId());
        EFRISDataRequest responseData = efrisService.goodsAndServiceInquiry(request);
        EFRISReturnStateInfo returnStateInfo = responseData.getReturnStateInfo();
        if (returnStateInfo == null || !returnStateInfo.getReturnCode().equals("00")) {
            throw new MediumException(ErrorData.builder().code(returnStateInfo.getReturnCode())
                    .message(returnStateInfo.getReturnMessage()).build());
        }
        byte[] decodedBytes = Base64.getDecoder().decode(responseData.getData().getContent());
        String decodedString = new String(decodedBytes);
        //JSONObject jsonObject = new JSONObject(decodedString);
        GoodsData goodsData = gson.fromJson(decodedString, GoodsData.class);

        ArrayList<GoodsRecord> records = goodsData.getRecords();
//        for (int i = 0; i < records.size(); i++) {
//            switch (records.get(i).getTaxRate()) {
//                case "0.18":
//                    records.get(i).setTaxCategory("01");
//                    records.get(i).setTaxCategory("A: Standard");
//                    break;
//                case "0":
//                    records.get(i).setTaxCategory("02");
//                    records.get(i).setTaxCategory("B: Zero");
//                    break;
//                case "-":
//                    records.get(i).setTaxCategory("02");
//                    records.get(i).setTaxCategory("B: Zero");
//                    break;
////                case "0":
////                    records.get(i).setTaxCategory("02");
////                    records.get(i).setTaxCategory("B: Zero");
////                    break;
//                case "0":
//                    records.get(i).setTaxCategory("02");
//                    records.get(i).setTaxCategory("B: Zero");
//                    break;
//                case "0":
//                    records.get(i).setTaxCategory("02");
//                    records.get(i).setTaxCategory("B: Zero");
//                    break;
//                case "0":
//                    records.get(i).setTaxCategory("02");
//                    records.get(i).setTaxCategory("B: Zero");
//                    break;
//                case "0":
//                    records.get(i).setTaxCategory("02");
//                    records.get(i).setTaxCategory("B: Zero");
//                    break;
//                case "0":
//                    records.get(i).setTaxCategory("02");
//                    records.get(i).setTaxCategory("B: Zero");
//                    break;
//                default:
//                    records.get(i).setTaxCategory("10");
//                    records.get(i).setTaxCategory("Others");
//            }
//        }


        return TxnResult.builder().message("approved").
                code("00").data(goodsData.getRecords()).build();
    }

    private EfrisInvoice saveInvoice(InvoiceData request) {
        EfrisInvoice efrisInvoice = efrisInvoiceRepo.save(EfrisInvoice.builder()
                .buyerTin(request.getBuyerDetails().getBuyerTin())
                .buyerNinBrn(request.getBuyerDetails().getBuyerNinBrn())
                .buyerPassportNumber(request.getBuyerDetails().getBuyerPassportNum())
                .buyerLegalName(request.getBuyerDetails().getBuyerLegalName())
                .buyerBusinessName(request.getBuyerDetails().getBuyerBusinessName())
                .buyerAddress(request.getBuyerDetails().getBuyerAddress())
                .buyerEmail(request.getBuyerDetails().getBuyerEmail())
                .buyerMobilePhone(request.getBuyerDetails().getBuyerMobilePhone())
                .buyerLinePhone(request.getBuyerDetails().getBuyerLinePhone())
                .buyerPlaceOfBusiness(request.getBuyerDetails().getBuyerPlaceOfBusi())
                .buyerType(request.getBuyerDetails().getBuyerType())
                .buyerCitizenship(request.getBuyerDetails().getBuyerCitizenship())
                .buyerSector(request.getBuyerDetails().getBuyerSector())
                .buyerReferenceNo(request.getBuyerDetails().getBuyerReferenceNo())
                .deviceNo(request.getBasicInformation().getDeviceNo())
                .issuedDate(DataUtils.getCurrentTimeStamp().toLocalDateTime())
                .efrisOperator(request.getBasicInformation().getOperator())
                .currency(request.getBasicInformation().getCurrency())
                .originInvoiceId(request.getBasicInformation().getOriInvoiceId())
                .invoiceType(request.getBasicInformation().getInvoiceType())
                .invoiceKind(request.getBasicInformation().getInvoiceKind())
                .dataSource(request.getBasicInformation().getDataSource())
                .invoiceIndustryCode(request.getBasicInformation().getInvoiceIndustryCode())
                .isBatch(request.getBasicInformation().getIsBatch())
                .tin(request.getSellerDetails().getTin())
                .ninBrn(request.getSellerDetails().getNinBrn())
                .legalName(request.getSellerDetails().getLegalName())
                .businessName(request.getSellerDetails().getBusinessName())
                .address(request.getSellerDetails().getAddress())
                .mobilePhone(request.getSellerDetails().getMobilePhone())
                .linePhone(request.getSellerDetails().getLinePhone())
                .emailAddress(request.getSellerDetails().getEmailAddress())
                .placeOfBusiness(request.getSellerDetails().getPlaceOfBusiness())
                .referenceNo(request.getSellerDetails().getReferenceNo())
                .branchId(request.getSellerDetails().getBranchId())
                .isCheckReferenceNo(request.getSellerDetails().getIsCheckReferenceNo())
                .createdBy(request.getCreatedBy())
                .createDate(DataUtils.getCurrentTimeStamp())
                .build());
        return efrisInvoice;
    }

    public TxnResult findAllInvoices(EfrisInvoice request) {
        Iterable<EfrisInvoice> customers = efrisInvoiceRepo.findAll();
        if (customers == null)
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    File filesystemRoot() {
        File tmpDir = new File("efris-resources");
        if (!tmpDir.isDirectory()) {
            try {
                return Files.createDirectory(tmpDir.toPath()).toFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tmpDir;
    }

    public Resource getInvoiceAsResource(Integer invoiceId) throws IOException {
        EfrisInvoice efrisInvoice = efrisInvoiceRepo.findById(invoiceId).get();
        if (efrisInvoice != null) {
            return new UrlResource(String.valueOf(Paths.get(efrisInvoice.getInvoicePath().trim())));
        }
        return null;
    }


    public TxnResult extractCommoditiesFromURAPortal(CommodityPage request) throws Exception {
        Gson gson = new Gson();
        EFRISDataRequest responseData;
        EFRISReturnStateInfo returnStateInfo;
        File invoicePath = filesystemRoot();
        String filePath;
        for (int i = 0; i < 100; i++) {
            responseData = efrisService.queryCommodityCategoryPagination(
                    CommodityPage.builder()
                            .pageNo(i)
                            .pageSize(request.getPageSize())
                            .build());
            returnStateInfo = responseData.getReturnStateInfo();
            if (returnStateInfo == null || !returnStateInfo.getReturnCode().equals("00"))
                throw new MediumException(ErrorData.builder().code(returnStateInfo.getReturnCode())
                        .message(returnStateInfo.getReturnMessage()).build());

            String decodedString = responseData.getData().getContent();
            String fileName = "Commodity_" + i + ".txt";
            filePath = invoicePath.getAbsolutePath() + "/" + fileName;
            System.out.println("PPPPPPAAAAAATTTTTTTTTHHHHHHH: " + filePath);
            File file;
            try {
                file = new File(filePath);
                if (file.createNewFile()) {
                    System.out.println("The file is created successfully!");
                } else {
                    System.out.println("The file already exists.");
                }
                Files.write(Paths.get(filePath), decodedString.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return TxnResult.builder().message("approved").
                code("00").build();
    }

    public TxnResult queryAllCommodityCategory() throws Exception {
        Gson gson = new Gson();
        EFRISDataRequest responseData;
        EFRISReturnStateInfo returnStateInfo;
        File invoicePath = filesystemRoot();
        String filePath;
        responseData = efrisService.queryAllCommodityCategory(
                CommodityPage.builder()
                        .pageNo(1)
                        .pageSize(300)
                        .build());
        returnStateInfo = responseData.getReturnStateInfo();
        if (returnStateInfo == null || !returnStateInfo.getReturnCode().equals("00"))
            throw new MediumException(ErrorData.builder().code(returnStateInfo.getReturnCode())
                    .message(returnStateInfo.getReturnMessage()).build());

        String decodedString = responseData.getData().getContent();
        String fileName = "Commodities.txt";
        filePath = invoicePath.getAbsolutePath() + "/" + fileName;
        System.out.println("PPPPPPAAAAAATTTTTTTTTHHHHHHH: " + filePath);
        File file;
        try {
            file = new File(filePath);
            if (file.createNewFile()) {
                System.out.println("The file is created successfully!");
            } else {
                System.out.println("The file already exists.");
            }
            Files.write(Paths.get(filePath), decodedString.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return TxnResult.builder().message("approved").
                code("00").build();
    }


}
