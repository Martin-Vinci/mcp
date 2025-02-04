package com.greybox.mediums.models.equiweb;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceRequestWrapper {
    private AuthRequest authRequest;
    private ServiceRequestBody requestBody;
}
