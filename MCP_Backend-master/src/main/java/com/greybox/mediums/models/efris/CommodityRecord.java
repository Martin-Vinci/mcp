package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommodityRecord {
    private String commodityCategoryCode;
    private String commodityCategoryLevel;
    private String commodityCategoryName;
    private String enableStatusCode;
    private String isLeafNode;
    private String nowTime;
    private int pageIndex;
    private int pageNo;
    private int pageSize;
    private String parentCode;
    private String exclusion;
    private String isExempt;
    private String isZeroRate;
    private String rate;
    private String serviceMark;
    private String exemptRateStartDate;
}
