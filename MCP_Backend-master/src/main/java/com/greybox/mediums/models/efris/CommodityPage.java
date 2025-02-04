package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommodityPage {
    private int pageCount;
    private int pageNo;
    private int pageSize;
    private int totalSize;
}
