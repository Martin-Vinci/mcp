package com.greybox.mediums.services.ura;

import com.google.gson.Gson;
import com.greybox.mediums.models.efris.GoodsDetails;
import com.greybox.mediums.models.efris.InvoiceData;
import com.greybox.mediums.models.efris.TaxDetails;
import com.greybox.mediums.utils.NumberToWord;
import com.greybox.mediums.utils.StringUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Service
public class InvoiceGeneratorService {
    static File filesystemRoot() {
        File tmpDir = new File("invoices");
        if (!tmpDir.isDirectory()) {
            try {
                return Files.createDirectory(tmpDir.toPath()).toFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tmpDir;
    }

    public static void main(String[] args) {
        String jsonString = "{\"airlineGoodsDetails\":[],\n" +
                "\"basicInformation\":{\"antifakeCode\":\"27417446387733613296\",\"currency\":\"UGX\",\"currencyRate\":\"1\",\"dataSource\":\"106\",\"deviceNo\":\"TCS3763c62043026625\",\"invoiceId\":\"174449854332338240\",\"invoiceIndustryCode\":\"101\",\"invoiceKind\":\"1\",\"invoiceNo\":\"322046916616\",\"invoiceType\":\"1\",\"isBatch\":\"0\",\"isInvalid\":\"0\",\"isPreview\":\"0\",\"isRefund\":\"0\",\"issuedDate\":\"04/10/2022 13:42:53\",\"issuedDatePdf\":\"04/10/2022 13:42:53\",\"operator\":\"Patrick\"},\"buyerDetails\":{\"buyerAddress\":\"61 KANJOKYA MICROPAY UGANDA KAMWOKYA KAMPALA KAMPALA CENTRAL DIVI KAMPALA CENTRAL DIVISION KAMWOKYA I \",\"buyerBusinessName\":\"MICROPAY(U) LTD\",\"buyerCitizenship\":\"0\",\"buyerEmail\":\"nthakkar@ura.go.ug\",\"buyerLegalName\":\"MICROPAY(U) LTD\",\"buyerLinePhone\":\"2560778497936\",\"buyerMobilePhone\":\"2560778497936\",\"buyerNinBrn\":\"/156627\",\"buyerPlaceOfBusi\":\"61 KANJOKYA MICROPAY UGANDA KAMWOKYA KAMPALA KAMPALA CENTRAL DIVI KAMPALA CENTRAL DIVISION KAMWOKYA I \",\"buyerTin\":\"1004466192\",\"buyerType\":\"0\",\"dateFormat\":\"dd/MM/yyyy\",\"nowTime\":\"2022/10/04 13:42:53\",\"pageIndex\":0,\"pageNo\":0,\"pageSize\":0,\"timeFormat\":\"dd/MM/yyyy HH24:mi:ss\"},\"extend\":{},\"goodsDetails\":[{\"categoryName\":\"Systems integration design\",\"deemedFlag\":\"2\",\"discountFlag\":\"2\",\"exciseFlag\":\"2\",\"exciseTax\":\"0\",\"goodsCategoryId\":\"81111503\",\"goodsCategoryName\":\"Systems integration design\",\"item\":\"Comission\",\"itemCode\":\"81111503\",\"orderNumber\":\"0\",\"qty\":\"1\",\"tax\":\"35084.75\",\"taxRate\":\"0.18\",\"total\":\"12304550000\",\"unitOfMeasure\":\"115\",\"unitPrice\":\"12304550000\",\"vatApplicableFlag\":\"1\"}],\"payWay\":[],\"sellerDetails\":{\"address\":\"61 KANJOKYA MICROPAY UGANDA KAMWOKYA KAMPALA KAMPALA CENTRAL DIVI KAMPALA CENTRAL DIVISION KAMWOKYA I \",\"branchCode\":\"00\",\"branchId\":\"770549392658370863\",\"branchName\":\"MICROPAY(U) LTD\",\"businessName\":\"MICROPAY(U) LTD\",\"emailAddress\":\"nthakkar@ura.go.ug\",\"legalName\":\"MICROPAY(U) LTD\",\"linePhone\":\"2560778497936\",\"mobilePhone\":\"2560778497936\",\"referenceNo\":\"1664880172875\",\"tin\":\"1004466192\"},\"summary\":{\"grossAmount\":\"12304550000\",\"remarks\":\"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\",\"itemCount\":\"1\",\"modeCode\":\"1\",\"netAmount\":\"194915.25\",\"qrCode\":\"02000000116A4C32204691661600015EF3C000003588FBFFFFFFFFFFFFA1004466192A1004466192~MICROPAY(U) LTD~MICROPAY(U) LTD~Comission\",\"taxAmount\":\"35084.75\"},\"taxDetails\":[{\"exciseCurrency\":\"UGX\",\"grossAmount\":\"12304550000\",\"netAmount\":\"194915.25\",\"taxAmount\":\"35084.75\",\"taxCategory\":\"A: Standard\",\"taxCategoryCode\":\"01\",\"taxRate\":\"0.18\",\"taxRateName\":\"18%\"}]}";
        //JSONObject jsonObject = new JSONObject(jsonString);
        Gson gson = new Gson();
        InvoiceData efrisDataResponse = gson.fromJson(jsonString.trim(), InvoiceData.class);

        String invoiceNo = efrisDataResponse.getBasicInformation().getInvoiceNo();
        String fileName = invoiceNo + ".pdf";
        File invoicePath = filesystemRoot();
        String invoiceFilePath = invoicePath.getAbsolutePath() + "/" + fileName;

        InvoiceGeneratorService invoiceGeneratorService = new InvoiceGeneratorService();
        invoiceGeneratorService.createPDF(invoiceFilePath, efrisDataResponse);

        //		Gson gson = new Gson();
//		String jsonString = new String(Files.readAllBytes(Paths.get("C:\\EFRIS_Commodities\\page_1.txt")));
//		CommodityData taxPayer = gson.fromJson(jsonString, CommodityData.class);
//		System.out.println(taxPayer);
    }

    public Boolean createPDF(String pdfFilename, InvoiceData invoiceData) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
            OutputStream file = new FileOutputStream(new File(pdfFilename));
            Document document = new Document();
            PdfWriter.getInstance(document, file);
            //Inserting Image in PDF
            Image image = Image.getInstance("efris-resources/logo.jpg");//Header Image
            image.setWidthPercentage(50f);//image width,height

            PdfPTable logoTable = new PdfPTable(2); //one page contains 15 records
            logoTable.setWidthPercentage(100);
            logoTable.setWidths(new float[]{6, 4});
            //logoTable.setSpacingBefore(4.0f);
            logoTable.addCell(getdescCell("FISCAL INVOICE", Element.ALIGN_MIDDLE, 17));
            logoTable.addCell(getCellImage(image, Element.ALIGN_RIGHT));

            PdfPTable buyerDetailsTable = new PdfPTable(2); //one page contains 15 records
            buyerDetailsTable.setSpacingBefore(4.0f);
            buyerDetailsTable.setSpacingAfter(4.0f);
            buyerDetailsTable.setWidthPercentage(100);
            buyerDetailsTable.setWidths(new float[]{5, 5});
            buyerDetailsTable.addCell(getBuyerDetailCell(invoiceData.getSellerDetails().getLegalName(), "", Element.ALIGN_LEFT));
            buyerDetailsTable.addCell(getBuyerDetailCell("FDN:", invoiceData.getBasicInformation().getInvoiceNo(), Element.ALIGN_LEFT));
            buyerDetailsTable.addCell(getBuyerDetailCell("Address:", invoiceData.getSellerDetails().getAddress(), Element.ALIGN_LEFT));
            buyerDetailsTable.addCell(getBuyerDetailCell("Verification Code:", invoiceData.getBasicInformation().getAntifakeCode(), Element.ALIGN_LEFT));
            buyerDetailsTable.addCell(getBuyerDetailCell("TIN:", invoiceData.getSellerDetails().getTin(), Element.ALIGN_LEFT));
            buyerDetailsTable.addCell(getBuyerDetailCell("Reference Number:", invoiceData.getSellerDetails().getReferenceNo(), Element.ALIGN_LEFT));
            buyerDetailsTable.addCell(getBuyerDetailCell("Tel:", invoiceData.getSellerDetails().getMobilePhone(), Element.ALIGN_LEFT));
            buyerDetailsTable.addCell(getBuyerDetailCell("Issued Date:", invoiceData.getBasicInformation().getIssuedDate(), Element.ALIGN_LEFT));
            buyerDetailsTable.addCell(getBuyerDetailCell("Email:", invoiceData.getSellerDetails().getEmailAddress(), Element.ALIGN_LEFT));
            buyerDetailsTable.addCell(getBuyerDetailCell("Served By:", invoiceData.getBasicInformation().getOperator(), Element.ALIGN_LEFT));
            buyerDetailsTable.addCell(getBuyerDetailCell("Currency:", invoiceData.getBasicInformation().getCurrency(), Element.ALIGN_LEFT));
            buyerDetailsTable.addCell(getBuyerDetailCell("", "", Element.ALIGN_LEFT));

            PdfPTable sellerDetailsTable = new PdfPTable(2); //one page contains 15 records
            sellerDetailsTable.setSpacingBefore(4.0f);
            sellerDetailsTable.setSpacingAfter(4.0f);
            sellerDetailsTable.setWidthPercentage(100);
            sellerDetailsTable.setWidths(new float[]{5, 5});
            sellerDetailsTable.addCell(getBuyerDetailCell("Bill To:", "", Element.ALIGN_LEFT));
            sellerDetailsTable.addCell(getBuyerDetailCell("", "", Element.ALIGN_LEFT));
            sellerDetailsTable.addCell(getBuyerDetailCell(invoiceData.getBuyerDetails().getBuyerBusinessName(), "", Element.ALIGN_LEFT));
            sellerDetailsTable.addCell(getBuyerDetailCell("", "", Element.ALIGN_LEFT));
            sellerDetailsTable.addCell(getBuyerDetailCell("TIN:", invoiceData.getBuyerDetails().getBuyerTin(), Element.ALIGN_LEFT));
            sellerDetailsTable.addCell(getBuyerDetailCell("", "", Element.ALIGN_LEFT));
            LineSeparator lineSeparator = new LineSeparator();


            PdfPTable utilityTable = new PdfPTable(5); //one page contains 15 records
            utilityTable.setWidthPercentage(100);
            utilityTable.setWidths(new float[]{4, 4, 2, 3, 3});
            utilityTable.setSpacingBefore(4.0f);
            utilityTable.addCell(getBillHeaderCell("Type", Element.ALIGN_LEFT));
            utilityTable.addCell(getBillHeaderCell("Description", Element.ALIGN_LEFT));
            utilityTable.addCell(getBillHeaderCell("Quantity", Element.ALIGN_RIGHT));
            utilityTable.addCell(getBillHeaderCell("Unit Price", Element.ALIGN_RIGHT));
            utilityTable.addCell(getBillHeaderCell("Amount", Element.ALIGN_RIGHT));

           ArrayList<GoodsDetails> goodsDetails = invoiceData.getGoodsDetails();
            for (int i = 0; i < goodsDetails.size(); i++) {
                utilityTable.addCell(getBillRowCell(goodsDetails.get(i).getGoodsCategoryName(), Element.ALIGN_LEFT));
                utilityTable.addCell(getBillRowCell(goodsDetails.get(i).getItem(), Element.ALIGN_LEFT));
                utilityTable.addCell(getBillRowCell(goodsDetails.get(i).getQty(), Element.ALIGN_RIGHT));
                utilityTable.addCell(getBillRowCell(StringUtil.toCurrencyFormat(new BigDecimal(goodsDetails.get(i).getUnitPrice())), Element.ALIGN_RIGHT));
                utilityTable.addCell(getBillRowCell(StringUtil.toCurrencyFormat(new BigDecimal(goodsDetails.get(i).getTotal())), Element.ALIGN_RIGHT));
            }

            utilityTable.addCell(getBillRowCell(" ", Element.ALIGN_LEFT)).setBorderWidthBottom(1);
            utilityTable.addCell(getBillRowCell(" ", Element.ALIGN_LEFT)).setBorderWidthBottom(1);
            utilityTable.addCell(getBillRowCell("", Element.ALIGN_RIGHT)).setBorderWidthBottom(1);
            utilityTable.addCell(getBillRowCell("", Element.ALIGN_RIGHT)).setBorderWidthBottom(1);
            utilityTable.addCell(getBillRowCell("", Element.ALIGN_RIGHT)).setBorderWidthBottom(1);

            PdfPTable billTable = new PdfPTable(5); //one page contains 15 records
            billTable.setWidthPercentage(100);
            billTable.setWidths(new float[]{4, 2, 3, 3, 3});
            billTable.setSpacingBefore(4.0f);

            billTable.addCell(getBillHeaderCell("Tax Category", Element.ALIGN_LEFT));
            billTable.addCell(getBillHeaderCell("Tax Rate", Element.ALIGN_RIGHT));
            billTable.addCell(getBillHeaderCell("Net Amount", Element.ALIGN_RIGHT));
            billTable.addCell(getBillHeaderCell("Tax Amount", Element.ALIGN_RIGHT));
            billTable.addCell(getBillHeaderCell("Gross Amount", Element.ALIGN_RIGHT));

            ArrayList<TaxDetails> taxDetails = invoiceData.getTaxDetails();
            for (int i = 0; i < taxDetails.size(); i++) {
                billTable.addCell(getBillRowCell(taxDetails.get(i).getTaxCategory(), Element.ALIGN_LEFT));
                billTable.addCell(getBillRowCell(taxDetails.get(i).getTaxRateName(), Element.ALIGN_RIGHT));
                billTable.addCell(getBillRowCell(StringUtil.toCurrencyFormat(taxDetails.get(i).getNetAmount()), Element.ALIGN_RIGHT));
                billTable.addCell(getBillRowCell(StringUtil.toCurrencyFormat(taxDetails.get(i).getTaxAmount()), Element.ALIGN_RIGHT));
                billTable.addCell(getBillRowCell(StringUtil.toCurrencyFormat(taxDetails.get(i).getGrossAmount()), Element.ALIGN_RIGHT));
            }
            billTable.addCell(getBillRowCell("  ", Element.ALIGN_LEFT)).setBorderWidthBottom(1);
            billTable.addCell(getBillRowCell("", Element.ALIGN_RIGHT)).setBorderWidthBottom(1);
            billTable.addCell(getBillRowCell("", Element.ALIGN_RIGHT)).setBorderWidthBottom(1);
            billTable.addCell(getBillRowCell("", Element.ALIGN_RIGHT)).setBorderWidthBottom(1);
            billTable.addCell(getBillRowCell("", Element.ALIGN_RIGHT)).setBorderWidthBottom(1);

            PdfPTable summaryTable = new PdfPTable(5); //one page contains 15 records
            summaryTable.setWidthPercentage(100);
            summaryTable.setWidths(new float[]{5, 2, 2, 2, 3});
            summaryTable.setSpacingBefore(1.0f);

            PdfPTable validity = new PdfPTable(1);
            validity.setWidthPercentage(100);
            validity.addCell(getValidityCell(" "));
            validity.addCell(getValidityCell("Remarks"));
            validity.addCell(getValidityCell(invoiceData.getSummary().getRemarks()));
            PdfPCell summaryL = new PdfPCell(validity);
            summaryL.setColspan(3);
            summaryL.setPadding(1.0f);
            summaryTable.addCell(summaryL);

            PdfPTable accounts = new PdfPTable(2);
            accounts.setWidthPercentage(100);
            summaryTable.setWidths(new float[]{4, 5, 5, 5, 5});
            accounts.addCell(getAccountsCell("Net Amount"));
            accounts.addCell(getAccountsCellR(StringUtil.toCurrencyFormat(invoiceData.getSummary().getNetAmount())));
            accounts.addCell(getAccountsCell("Tax Amount"));
            accounts.addCell(getAccountsCellR(StringUtil.toCurrencyFormat(invoiceData.getSummary().getTaxAmount())));
            accounts.addCell(getAccountsCell("Total Due"));
            accounts.addCell(getAccountsCellR(StringUtil.toCurrencyFormat(invoiceData.getSummary().getGrossAmount())));
            PdfPCell summaryR = new PdfPCell(accounts);
            summaryR.setColspan(3);
            summaryR.setBorderWidthBottom(0);
            summaryTable.addCell(summaryR);

            PdfPTable describerTable = new PdfPTable(1); //one page contains 15 records
            describerTable.setWidthPercentage(100);
            describerTable.setWidths(new float[]{10});
            describerTable.addCell(getBillFooterCell(NumberToWord.convertToWords(invoiceData.getSummary().getGrossAmount()))).setBorderWidthBottom(1);
            document.open();//PDF document opened........
            document.add(logoTable);
            document.add(new Chunk(lineSeparator));
            document.add(buyerDetailsTable);
            document.add(new Chunk(lineSeparator));
            document.add(sellerDetailsTable);
            document.add(setSubHeader("Goods & Services"));
            document.add(new Chunk(lineSeparator));
            document.add(utilityTable);

            document.add(setSubHeader("Tax Details"));
            document.add(new Chunk(lineSeparator));
            document.add(billTable);

            document.add(setSubHeader("Invoice Summary"));
            document.add(new Chunk(lineSeparator));
            document.add(summaryTable);
            document.add(describerTable);
            BarcodeQRCode my_code = new BarcodeQRCode(invoiceData.getSummary().getQrCode(), 20, 20, null);
            Image qr_image = my_code.getImage();

            PdfPTable qrTable = new PdfPTable(3); //one page contains 15 records
            qrTable.setWidthPercentage(100);
            qrTable.setWidths(new float[]{3, 3, 4});
            qrTable.setSpacingBefore(1.0f);
            qrTable.addCell(getdescCell("", Element.ALIGN_MIDDLE, 10));
            qrTable.addCell(getCellImage(qr_image, Element.ALIGN_CENTER));
            qrTable.addCell(getdescCell("", Element.ALIGN_MIDDLE, 10));
            document.add(qrTable);

            document.close();

            file.close();

            System.out.println("Pdf created successfully..");
            System.out.println(pdfFilename);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static PdfPCell getBillHeaderCell(String text, Integer horizontalAlignment) {
        FontSelector fs = new FontSelector();
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
        //font.setColor(BaseColor.GRAY);
        fs.addFont(font);
        Phrase phrase = fs.process(text);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setHorizontalAlignment(horizontalAlignment);
        cell.setPadding(5.0f);
        return cell;
    }

    public static PdfPCell getBillRowCell(String text, Integer horizontalAlignment) {
        FontSelector fs = new FontSelector();
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);
        fs.addFont(font);
        Phrase phrase = fs.process(text);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setPadding(5.0f);
        cell.setBorderWidthBottom(0);
        cell.setHorizontalAlignment(horizontalAlignment);
        cell.setBorderWidthTop(0);
        return cell;
    }

    public static PdfPCell getValidityCell(String text) {
        FontSelector fs = new FontSelector();
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
        fs.addFont(font);
        Phrase phrase = fs.process(text);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(0);
        return cell;
    }

    public static PdfPCell getAccountsCell(String text) {
        FontSelector fs = new FontSelector();
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);
        fs.addFont(font);
        Phrase phrase = fs.process(text);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorderWidthRight(0);
        cell.setBorderWidthTop(0);
        cell.setPadding(5.0f);
        return cell;
    }

    public static PdfPCell getAccountsCellR(String text) {
        FontSelector fs = new FontSelector();
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);
        fs.addFont(font);
        Phrase phrase = fs.process(text);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthTop(0);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(5.0f);
        cell.setPaddingRight(20.0f);
        return cell;
    }

    public static PdfPCell getBillFooterCell(String text) {
        FontSelector fs = new FontSelector();
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);
        fs.addFont(font);
        Phrase phrase = fs.process(text);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(5.0f);
        cell.setBorderWidthBottom(1);
        cell.setBorderWidthTop(1);
        return cell;
    }

    public static PdfPCell getdescCell(String text, Integer horizontalAlignment, float fontSize) {
        FontSelector fs = new FontSelector();
        Font font = FontFactory.getFont(FontFactory.HELVETICA, fontSize, Font.BOLD);
        fs.addFont(font);
        Phrase phrase = fs.process(text);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setHorizontalAlignment(horizontalAlignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(0);
        return cell;
    }

    public static PdfPCell getBuyerDetailCell(String lable, String value, Integer horizontalAlignment) {
        Font bold = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
        Chunk chunkLable = new Chunk(lable, bold);
        Chunk chunkValue = new Chunk(value, valueFont);
        Phrase phrase = new Phrase();
        phrase.add(chunkLable);
        phrase.add(chunkValue);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setHorizontalAlignment(horizontalAlignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(0);
        return cell;
    }


    public static PdfPCell getCellImage(Image image, Integer horizontalAlignment) {
        PdfPCell cell = new PdfPCell(image, true);
        cell.setPadding(20);
        cell.setPaddingRight(30);
        cell.setHorizontalAlignment(horizontalAlignment);
        cell.setBorder(0);
        return cell;
    }

    public static Paragraph setSubHeader(String headerText) {
        Font subTitleHeaderFonts = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
        Paragraph paragraph = new Paragraph(headerText, subTitleHeaderFonts);
        paragraph.setSpacingBefore(15.0f);
        paragraph.setMultipliedLeading(0.5f);
        return paragraph;
    }


}