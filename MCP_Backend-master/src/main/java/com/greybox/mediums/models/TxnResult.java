package com.greybox.mediums.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TxnResult<T> {

    private String code;
    private String message;
    private T data;
    private ErrorData[] errorData;
}
