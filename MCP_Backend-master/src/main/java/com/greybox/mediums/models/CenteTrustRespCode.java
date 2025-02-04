package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CenteTrustRespCode {
   private String responseCode;
   private String responseMessage;
}
