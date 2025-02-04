
package com.greybox.mediums.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ISWBillerItem {
    private String billerId;
    private String itemId;
    private String billerCode;
}