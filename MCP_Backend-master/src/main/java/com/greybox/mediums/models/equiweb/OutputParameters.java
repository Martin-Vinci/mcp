package com.greybox.mediums.models.equiweb;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class OutputParameters {
    private ArrayList<ExportItem> exportItems;
}
