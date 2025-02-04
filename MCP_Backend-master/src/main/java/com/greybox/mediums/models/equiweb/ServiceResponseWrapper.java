package com.greybox.mediums.models.equiweb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceResponseWrapper {
    private String responseCode;
    private String responseMessage;
    private RequestOutput requestOutput;
}
