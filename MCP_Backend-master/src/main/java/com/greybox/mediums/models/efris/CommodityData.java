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
public class CommodityData {
    private String dateFormat;
    private String nowTime;
    private CommodityPage page;
    private ArrayList<CommodityRecord> records;
    private String timeFormat;
}
