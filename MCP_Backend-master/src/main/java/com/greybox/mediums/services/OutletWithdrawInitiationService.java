package com.greybox.mediums.services;

import com.greybox.mediums.entities.MobileUser;
import com.greybox.mediums.entities.OutletWithdrawInitiation;
import com.greybox.mediums.models.CashOutRequest;
import com.greybox.mediums.models.CashOutResponse;
import com.greybox.mediums.models.ErrorData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.MobileUserRepo;
import com.greybox.mediums.repository.OutletWithdrawInitiationRepo;
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
public class OutletWithdrawInitiationService {
    @Autowired
    private OutletWithdrawInitiationRepo outletWithdrawInitiationRepo;

    @Autowired
    private MobileUserRepo mobileUserRepo;

    public TxnResult initiateCashout(CashOutRequest request) throws NoSuchAlgorithmException, MediumException {
        OutletWithdrawInitiation cashoutInitiation = new OutletWithdrawInitiation();
        MobileUser mobileUser = mobileUserRepo.findMobileUserByPhoneNo(request.getCustomerPhone());
        if (mobileUser == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid mobile phone specified").build());
        cashoutInitiation.setOutletName(mobileUser.getCustomerName());
        cashoutInitiation.setDateCreated(DataUtils.getCurrentTimeStamp());
        cashoutInitiation.setAmount(request.getAmount());
        cashoutInitiation.setOutletPhone(request.getCustomerPhone());
        cashoutInitiation.setOutletAccount(request.getCustomerAccount());
        cashoutInitiation.setOutletCode(request.getOutletCode());
        cashoutInitiation.setApproved(false);
        Timestamp expiryTime = DataUtils.getCurrentTimeStamp();
        expiryTime.setTime(expiryTime.getTime() + TimeUnit.MINUTES.toMillis(5));
        cashoutInitiation.setExpiryDate(expiryTime);
        DateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        String withdrawCode = dateFormat.format(Calendar.getInstance().getTime()) + "" + StringUtil.generateRandomNumber(4);
        cashoutInitiation.setWithdrawCode(withdrawCode);
        outletWithdrawInitiationRepo.save(cashoutInitiation);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return TxnResult.builder().message("approved").
                code("00").data(CashOutResponse.builder()
                        .accountNo(cashoutInitiation.getOutletAccount())
                        .otpCode(withdrawCode)
                        .amount(cashoutInitiation.getAmount())
                        .codeStatus("PENDING")
                        .customerName(cashoutInitiation.getOutletName())
                        .phoneNo(cashoutInitiation.getOutletPhone())
                        .expiryDate(dateFormat.format(cashoutInitiation.getExpiryDate()))
                        .build()).build();
    }

    public TxnResult validateCashoutOTP(CashOutRequest request) throws MediumException {
        OutletWithdrawInitiation data = outletWithdrawInitiationRepo.findOutletWithdrawCodeDetails(request.getWithdrawCode(),
                request.getWithdrawOutletCode());
        if (data == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid OTP code or Outlet code specified").build());

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
                        .accountNo(data.getOutletAccount())
                        .amount(data.getAmount())
                        .codeStatus(status)
                        .customerName(data.getOutletName())
                        .phoneNo(data.getOutletPhone())
                        .expiryDate(dateFormat.format(data.getExpiryDate()))
                        .build()).build();
    }
}
