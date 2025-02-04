package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardActiveOutlet {
private String outletNo;
private String transDate;
private String outletName;
private Integer transCount;
}
