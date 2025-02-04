package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String emailSender;
    private String emailReceipient;
    private String messageBody;
    private String emailCopyList;
    private String emailSubject;
    private Long emailDataId;
}
