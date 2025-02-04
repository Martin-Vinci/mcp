package com.greybox.mediums.services;

import com.greybox.mediums.entities.CashoutInitiation;
import com.greybox.mediums.entities.MobileUser;
import com.greybox.mediums.models.*;
import com.greybox.mediums.repository.CashoutInitiationRepo;
import com.greybox.mediums.repository.MobileUserRepo;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.MediumException;
import com.greybox.mediums.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

@Service
public class CashOutInitiationService {
    @Autowired
    private CashoutInitiationRepo cashoutInitiationRepo;

    @Autowired
    private MobileUserRepo mobileUserRepo;

    public TxnResult initiateCashout(CashOutRequest request) throws NoSuchAlgorithmException, MediumException {
        CashoutInitiation cashoutInitiation = new CashoutInitiation();
        MobileUser mobileUser = mobileUserRepo.findMobileUserByPhoneNo(request.getCustomerPhone());
        if (mobileUser == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid mobile phone specified").build());
        cashoutInitiation.setCustomerName(mobileUser.getCustomerName());
        cashoutInitiation.setDateCreated(DataUtils.getCurrentTimeStamp());
        cashoutInitiation.setAmount(request.getAmount());
        cashoutInitiation.setCustomerPhone(request.getCustomerPhone());
        cashoutInitiation.setCustomerAccount(request.getCustomerAccount());
        cashoutInitiation.setOutletCode(request.getOutletCode());
        cashoutInitiation.setApproved(false);
        Timestamp expiryTime = DataUtils.getCurrentTimeStamp();
        expiryTime.setTime(expiryTime.getTime() + TimeUnit.MINUTES.toMillis(5));
        cashoutInitiation.setExpiryDate(expiryTime);
        DateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        String withdrawCode = dateFormat.format(Calendar.getInstance().getTime()) + "" + StringUtil.generateRandomNumber(4);
        cashoutInitiation.setWithdrawCode(withdrawCode);

        cashoutInitiationRepo.save(cashoutInitiation);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return TxnResult.builder().message("approved").
                code("00").data(CashOutResponse.builder()
                        .accountNo(cashoutInitiation.getCustomerAccount())
                        .otpCode(withdrawCode)
                        .amount(cashoutInitiation.getAmount())
                        .codeStatus("PENDING")
                        .customerName(cashoutInitiation.getCustomerName())
                        .phoneNo(cashoutInitiation.getCustomerPhone())
                        .expiryDate(dateFormat.format(cashoutInitiation.getExpiryDate()))
                        .build()).build();
    }

    public TxnResult validateCashoutOTP(CashOutRequest request) throws MediumException {
        request.setCustomerPhone(StringUtil.formatPhoneNumber(request.getCustomerPhone()));
        CashoutInitiation data = cashoutInitiationRepo.findWithdrawCodeDetails(request.getWithdrawCode(),
                StringUtil.formatPhoneNumber(request.getCustomerPhone()));

        if (data == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid OTP code or customer phone number specified").build());

        long milliseconds = data.getExpiryDate().getTime() - DataUtils.getCurrentTimeStamp().getTime();
        int seconds = (int) milliseconds / 1000;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = (seconds % 3600) % 60;
        String status = seconds < 0 ? "EXPIRED" : "PENDING";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (status.equals("EXPIRED"))
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Withdraw code is expired").build());

        return TxnResult.builder().message("approved").
                code("00").data(CashOutResponse.builder()
                        .accountNo(data.getCustomerAccount().trim())
                        .amount(data.getAmount())
                        .codeStatus(status)
                        .customerName(data.getCustomerName().trim())
                        .phoneNo(data.getCustomerPhone().trim())
                        .expiryDate(dateFormat.format(data.getExpiryDate()))
                        .build()).build();
    }

    public TxnResult save(CashoutInitiation request) throws MediumException {
        MobileUser mobileUser = mobileUserRepo.findMobileUserByPhoneNo(request.getCustomerPhone());
        if (mobileUser == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid mobile phone specified").build());
        request.setCustomerName(mobileUser.getCustomerName());

        cashoutInitiationRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
    public TxnResult update(CashoutInitiation request) {
        cashoutInitiationRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
