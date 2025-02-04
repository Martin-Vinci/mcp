package com.greybox.mediums.utils;

import com.greybox.mediums.models.ErrorData;

public class MediumException extends RuntimeException {

    private static final long serialVersionUID = 7718828512143293558L;

    private final ErrorData code;

    public MediumException(ErrorData code) {
        super(code.getMessage());
        this.code = code;
    }

    public ErrorData getErrorMessage() {
        return this.code;
    }
}