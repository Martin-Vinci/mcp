package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceData {
    private SellerDetails sellerDetails;
    private BasicInformation basicInformation;
    private BuyerDetails buyerDetails;
    private BuyerExtend buyerExtend;
    private ArrayList<GoodsDetails> goodsDetails;
    private ArrayList<TaxDetails> taxDetails;
    private Summary summary;
    private ArrayList<PayWay> payWay;
    private Extend extend;
    private ImportServicesSeller importServicesSeller;
    private ArrayList<AirlineGoodsDetails> airlineGoodsDetails;
    private String createdBy;
}
