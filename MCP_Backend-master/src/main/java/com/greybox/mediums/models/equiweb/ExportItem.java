package com.greybox.mediums.models.equiweb;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class ExportItem {
    private String code;
    private String value;
}
