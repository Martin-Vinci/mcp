package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImportServicesSeller {
    private String importBusinessName;
    private String importEmailAddress;
    private String importContactNumber;
    private String importAddress;
    private String importInvoiceDate;
    private String importAttachmentName;
    private String importAttachmentContent;
}
