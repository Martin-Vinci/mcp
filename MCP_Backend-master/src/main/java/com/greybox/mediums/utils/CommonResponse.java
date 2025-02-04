package com.greybox.mediums.utils;


import com.greybox.mediums.models.ErrorData;
import com.greybox.mediums.models.TxnResult;

public class CommonResponse {
    public  static TxnResult getUndefinedError() {
        return TxnResult.builder().code("-99")
                .message("A system related error has occurred").build();
    }
    public  static TxnResult getSuccessMessage() {
        return TxnResult.builder().code("00")
                .message("Approved").build();
    }
    public  static TxnResult getMediumExceptionError(ErrorData errorData) {
        return TxnResult.builder().code(errorData.getCode())
                .message(errorData.getMessage()).build();
    }
}
