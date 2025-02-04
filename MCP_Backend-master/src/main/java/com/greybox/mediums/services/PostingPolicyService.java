package com.greybox.mediums.services;

import com.greybox.mediums.entities.ServicePostingAccountsRef;
import com.greybox.mediums.entities.ServicePostingAmountRef;
import com.greybox.mediums.entities.ServicePostingDetail;
import com.greybox.mediums.models.ErrorData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.ServicePostingAccountsRefRepo;
import com.greybox.mediums.repository.ServicePostingAmountRefRepo;
import com.greybox.mediums.repository.ServicePostingDetailRepo;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.MediumException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostingPolicyService {

    @Autowired
    private ServicePostingDetailRepo servicePostingDetailRepo;
    @Autowired
    private ServicePostingAmountRefRepo servicePostingAmountRefRepo;
    @Autowired
    private ServicePostingAccountsRefRepo servicePostingAccountsRefRepo;

    public TxnResult findPostingPolicyAmountTypes() throws MediumException {
        List<ServicePostingAmountRef> customers = this.servicePostingAmountRefRepo.findPostingPolicyAmountTypes();
        if (customers == null) {
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Missing posting policies specified for the selected transaction code").build());
        }
        return TxnResult.builder().message("approved")
                .code("00").data(customers).build();
    }

    public TxnResult findPostingPolicyAccountTypes() throws MediumException {
        List<ServicePostingAccountsRef> customers = this.servicePostingAccountsRefRepo.findPostingPolicyAccountTypes();
        if (customers == null) {
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Missing posting policies specified for the selected transaction code").build());
        }
        return TxnResult.builder().message("approved")
                .code("00").data(customers).build();
    }

    public TxnResult find(Integer serviceId) throws MediumException {
        List<ServicePostingDetail> customers = this.servicePostingDetailRepo.findServicePostingPolicies(serviceId);
        if (customers == null) {
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Missing posting policies specified for the selected transaction code").build());
        }
        return TxnResult.builder().message("approved")
                .code("00").data(customers).build();
    }

    public TxnResult save(ServicePostingDetail request) {
        request.setCreateDt(DataUtils.getCurrentDate());
        this.servicePostingDetailRepo.save(request);
        return TxnResult.builder().message("approved")
                .code("00").data(request).build();
    }

    public TxnResult update(ServicePostingDetail request) {
        if (request.getAmountType() == null)
            return TxnResult.builder().code("-99").message("Missing Amount type").build();
        if (request.getAmountType().equals("TRANS_AMOUNT_AGENT_SHARE") &&
                request.getTranAmtAgentShare() == null) {
            return TxnResult.builder().code("-99").message("Percent share for Agent share must be specified").build();
        }
        if (request.getAmountType().equals("TRANS_AMOUNT_BANK_SHARE") &&
                request.getTranAmtBankShare() == null) {
            return TxnResult.builder().code("-99").message("Percent share for Bank share must be specified").build();
        }
        if (request.getAmountType().equals("TRANS_AMOUNT_VENDOR_SHARE") &&
                request.getTranAmtVendorShare() == null) {
            return TxnResult.builder().code("-99").message("Percent share for Vendor share must be specified").build();
        }
        this.servicePostingDetailRepo.save(request);
        return TxnResult.builder().message("approved")
                .code("00").data(request).build();
    }

    @Transactional
    public TxnResult deleteServicePostingDetail(ServicePostingDetail request) {
        request.setCreateDt(DataUtils.getCurrentDate());
        this.servicePostingDetailRepo.deleteServicePostingDetail(request.getPostingDetailId());
        return TxnResult.builder().message("approved")
                .code("00").data(request).build();
    }
}
