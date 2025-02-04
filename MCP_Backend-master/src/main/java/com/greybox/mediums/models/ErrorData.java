package com.greybox.mediums.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorData<T> {

    private String code;
    private String error;
    private String message;
}
