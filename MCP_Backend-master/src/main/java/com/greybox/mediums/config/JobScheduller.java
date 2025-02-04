package com.greybox.mediums.config;

import com.greybox.mediums.inter_switch.KeyExchangeService;
import com.greybox.mediums.inter_switch.dto.KeyExchangeResponse;
import com.greybox.mediums.inter_switch.dto.PhoenixResponseCodes;
import com.greybox.mediums.inter_switch.dto.SystemResponse;
import com.greybox.mediums.inter_switch.utils.Constants;
import com.greybox.mediums.services.BillerService;
import com.greybox.mediums.services.CenteTrustService;
import com.greybox.mediums.services.MobileUserService;
import com.greybox.mediums.services.TransactionService;
import com.greybox.mediums.services.VoucherTransactionService;
import com.greybox.mediums.utils.Logger;
import java.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobScheduller {
    @Autowired
    VoucherTransactionService voucherTransactionService;
    @Autowired
    MobileUserService mobileUserService;
    @Autowired
    BillerService billerService;
    @Autowired
    CenteTrustService centeTrustService;
    @Autowired
    TransactionService transactionService;

    private KeyExchangeService keyExchangeService;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public JobScheduller(KeyExchangeService keyExchangeService) {
        this.keyExchangeService = keyExchangeService;
    }

    @Scheduled(
            initialDelay = 1000L,
            fixedRate = 600000L
    )
    public void postPendingCenteTransactions() {
        this.centeTrustService.postPendingCenteTransactions();
    }

    @Scheduled(
            initialDelay = 1000L,
            fixedRate = 10000L
    )
    public void processPendingTransactions() {
        this.transactionService.processPendingTransactions();
    }

    @Scheduled(
            initialDelay = 1000L,
            fixedRate = 600000L
    )
    public void processInvoiceStatement() {
        this.voucherTransactionService.processVoucherExpiryService();
        this.mobileUserService.processAccountBalanceUpdate();
        this.billerService.syncInterswitchPaymentItems();
    }

    @Scheduled(
            initialDelay = 1000L,
            fixedRate = 21600000L
    )
    public void processKeyExchange() {
        try {
            SystemResponse<KeyExchangeResponse> exchangeKeys = this.keyExchangeService.doKeyExchange();
            if (exchangeKeys.getResponseCode().equals(PhoenixResponseCodes.APPROVED.CODE)) {
                Constants.exchangeKeys = exchangeKeys;
                return;
            }

            Logger.logError("======================== Inters-witch Key Exchange Failed. ERROR: " + exchangeKeys);
        } catch (Exception var2) {
            Logger.logError("======================== Inters-witch Key Exchange Failed with Error: ");
            Logger.logError(var2);
        }

    }
}
