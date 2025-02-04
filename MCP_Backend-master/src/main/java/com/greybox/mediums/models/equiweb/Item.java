package com.greybox.mediums.models.equiweb;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Item {
    private String code;
    private String value;
}
