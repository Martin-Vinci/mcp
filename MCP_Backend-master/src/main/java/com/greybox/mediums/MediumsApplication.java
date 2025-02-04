package com.greybox.mediums;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@SpringBootApplication
@EnableEncryptableProperties
@EnableScheduling
public class MediumsApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MediumsApplication.class, args);

//        URAInvoiceService efrisService = new URAInvoiceService();
//        efrisService.extractCommoditiesFromURAPortal();


//        String jsonString = "[{\"commodityCategoryId\":\"10121702\",\"currency\":\"101\",\"dateFormat\":\"dd/MM/yyyy\",\"description\":\"1\",\"exciseDutyCode\":\"1\",\"goodsCode\":\"32222\",\"goodsName\":\"TEST\",\"goodsTypeCode\":\"101\",\"haveExciseTax\":\"101\",\"haveOtherUnit\":\"102\",\"havePieceUnit\":\"101\",\"id\":\"148531511727879768\",\"nowTime\":\"2022/10/01 13:43:01\",\"operationType\":\"101\",\"pageIndex\":0,\"pageNo\":0,\"pageSize\":0,\"pieceMeasureUnit\":\"101\",\"pieceScaledValue\":\"1\",\"returnCode\":\"2233\",\"returnMessage\":\"measureUnit:cannot be empty!\",\"stockPrewarning\":\"23\",\"timeFormat\":\"dd/MM/yyyy HH24:mi:ss\",\"tin\":\"1004466192\",\"unitPrice\":\"1200\"}]";
//        //JSONObject jsonObject = new JSONObject(jsonString);
//        Gson gson = new Gson();
//        JSONArray jsonArray = new JSONArray(jsonString);
//        if (jsonArray != null) {
//            for (int i = 0; i < jsonArray.length(); i++) {
//                GoodsRecord response = gson.fromJson(jsonArray.get(i).toString(), GoodsRecord.class);
//                if(!response.getReturnCode().equals("00"))
//                    throw new MediumException(ErrorData.builder().code(response.getReturnCode())
//                            .message(response.getReturnMessage()).build());
//            }
//        }
        //		Gson gson = new Gson();
//		String jsonString = new String(Files.readAllBytes(Paths.get("C:\\EFRIS_Commodities\\page_1.txt")));
//		CommodityData taxPayer = gson.fromJson(jsonString, CommodityData.class);
//		System.out.println(taxPayer);
    }
}
