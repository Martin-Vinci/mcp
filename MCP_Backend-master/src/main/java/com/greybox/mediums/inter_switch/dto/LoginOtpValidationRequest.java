package com.greybox.mediums.inter_switch.dto;

public class LoginOtpValidationRequest extends ClientTerminalRequest{

	private String otp;
	public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}
}
