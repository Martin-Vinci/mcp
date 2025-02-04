package com.greybox.mediums.models.equiweb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class TransOutput {
    @JsonProperty("StatementOutput")
    private ArrayList<StatementOutput> statementOutput;
}
