package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoodsOtherUnit {
    private String otherUnit;
    private String otherPrice;
    private String otherScaled;
    private String packageScaled;
}
