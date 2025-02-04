export class AirlineGoodsDetail {

    public item: string;

    public itemCode: string;

    public qty: string;

    public unitOfMeasure: string;

    public unitPrice: string;

    public total: string;

    public taxRate: string;

    public tax: string;

    public discountTotal: string;

    public discountTaxRate: string;

    public orderNumber: string;

    public discountFlag: string;

    public deemedFlag: string;

    public exciseFlag: string;

    public categoryId: string;

    public categoryName: string;

    public goodsCategoryId: string;

    public goodsCategoryName: string;

    public exciseRate: string;

    public exciseRule: string;

    public exciseTax: string;

    public pack: string;

    public stick: string;

    public exciseUnit: string;

    public exciseCurrency: string;

    public exciseRateName: string;
}
export class BasicInformation {

    public invoiceNo: string;

    public antifakeCode: string;

    public deviceNo: string;

    public issuedDate: string;

    public operator: string;

    public currency: string;

    public oriInvoiceId: string;

    public invoiceType: string;

    public invoiceKind: string;

    public dataSource: string;

    public invoiceIndustryCode: string;

    public isBatch: string;
}
export class BuyerDetails {

    public buyerTin: string;

    public buyerNinBrn: string;

    public buyerPassportNum: string;

    public buyerLegalName: string;

    public buyerBusinessName: string;

    public buyerAddress: string;

    public buyerEmail: string;

    public buyerMobilePhone: string;

    public buyerLinePhone: string;

    public buyerPlaceOfBusi: string;

    public buyerType: string;

    public buyerCitizenship: string;

    public buyerSector: string;

    public buyerReferenceNo: string;
}
export class BuyerExtend {

    public propertyType: string;

    public district: string;

    public municipalityCounty: string;

    public divisionSubcounty: string;

    public town: string;

    public cellVillage: string;

    public effectiveRegistrationDate: string;

    public meterStatus: string;
}
export class Extend {

    public reason: string;

    public reasonCode: string;
}
export class GoodsDetail {
    public item: string;
    public itemCode: string;
    public qty: string;
    public unitOfMeasure: string;
    public unitPrice: number;
    public total: number;
    public taxRate: number;
    public tax: number;
    public discountTotal: number;
    public discountTaxRate: number;
    public orderNumber: number;
    public discountFlag: string;
    public deemedFlag: string;
    public exciseFlag: string;
    public categoryId: string;
    public categoryName: string;
    public goodsCategoryId: string;
    public goodsCategoryName: string;
    public exciseRate: string;
    public exciseRule: string;
    public exciseTax: string;
    public pack: string;
    public stick: string;
    public exciseUnit: string;
    public exciseCurrency: string;
    public exciseRateName: string;
}
export class ImportServicesSeller {

    public importBusinessName: string;

    public importEmailAddress: string;

    public importContactNumber: string;

    public importAddress: string;

    public importInvoiceDate: string;

    public importAttachmentName: string;

    public importAttachmentContent: string;
}
export class PayWay {

    public paymentMode: string;

    public paymentAmount: string;

    public orderNumber: string;
}

export class InvoiceData {
    public sellerDetails: SellerDetails;
    public basicInformation: BasicInformation;
    public buyerDetails: BuyerDetails;
    public buyerExtend: BuyerExtend;
    public goodsDetails: GoodsDetail[];
    public taxDetails: TaxDetail[];
    public summary: Summary;
    public payWay: PayWay[];
    public extend: Extend;
    public importServicesSeller: ImportServicesSeller;
    public airlineGoodsDetails: AirlineGoodsDetail[];
    public createdBy: string;
}
export class SellerDetails {

    public tin: string;

    public ninBrn: string;

    public legalName: string;

    public businessName: string;

    public address: string;

    public mobilePhone: string;

    public linePhone: string;

    public emailAddress: string;

    public placeOfBusiness: string;

    public referenceNo: string;

    public branchId: string;

    public isCheckReferenceNo: string;
}
export class Summary {

    public netAmount: number;

    public taxAmount: number;

    public grossAmount: number;

    public itemCount: number;

    public modeCode: string;

    public remarks: string;

    public qrCode: string;
}
export class TaxDetail {
    public taxCategoryCode: string;
    public netAmount: number;
    public taxRate: number;
    public taxAmount: number;
    public grossAmount: number;
    public exciseUnit: string;
    public exciseCurrency: string;
    public taxRateName: string;
}