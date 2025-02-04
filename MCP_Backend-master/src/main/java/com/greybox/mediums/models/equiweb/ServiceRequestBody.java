package com.greybox.mediums.models.equiweb;

import lombok.Builder;
import lombok.Data;

@Data
public class ServiceRequestBody {
    private String serviceCode;
    private Long requestId;
    private RequestInput requestInput;
}
