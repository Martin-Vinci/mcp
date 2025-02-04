package com.greybox.mediums.models.equiweb;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthRequest {
    private String channelCode;
    private Integer institutionId;
    private String vCode;
    private String vPassword;
    private String userName;
}

