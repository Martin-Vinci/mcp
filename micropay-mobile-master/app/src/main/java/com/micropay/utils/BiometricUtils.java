package com.micropay.utils;

import com.morpho.morphosmart.sdk.CallbackMask;

/**
 * Created by micropay on 2/13/19.
 */

public class BiometricUtils {

    public static int callbackCmd = CallbackMask.MORPHO_CALLBACK_IMAGE_CMD.getValue()
            | CallbackMask.MORPHO_CALLBACK_ENROLLMENT_CMD.getValue()
            | CallbackMask.MORPHO_CALLBACK_COMMAND_CMD.getValue()
            | CallbackMask.MORPHO_CALLBACK_CODEQUALITY.getValue()
            | CallbackMask.MORPHO_CALLBACK_DETECTQUALITY.getValue();

}
