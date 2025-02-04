package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TxnUpdateStatusRequest {
    private Long mcpTransDetailId;  // The ID of the transaction detail
    private String successFlag;      // Indicates if the transaction was successful ("Y" or "N")
    private String message;          // The message related to the transaction update
    private String status;           // The status of the transaction (e.g., "Posted", "Reversed", etc.)
}
