package com.greybox.mediums.services;

import com.greybox.mediums.entities.ServiceCommissionTier;
import com.greybox.mediums.models.CommissionTierData;
import com.greybox.mediums.models.ErrorData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.ServiceCommissionTierRepo;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.MediumException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ProductCommissionTierService {

    @Autowired
    private ServiceCommissionTierRepo serviceCommissionTierRepo;

    public TxnResult find(ServiceCommissionTier request) {
        List<ServiceCommissionTier> customers = serviceCommissionTierRepo.findCommissionTiers(request.getCommissionId());
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public List<ServiceCommissionTier> find(Integer chargeId) {
        List<ServiceCommissionTier> response = serviceCommissionTierRepo.findCommissionTiers(chargeId);
        return response;
    }

    @Transactional
    public TxnResult save(CommissionTierData request) throws MediumException {
        serviceCommissionTierRepo.deleteCurrentTierRecords(request.getCommissionId());
        for (ServiceCommissionTier tier : request.getData()) {

            if (tier.getCommissionAmount() == null)
                throw new MediumException(ErrorData.builder().code("404")
                        .message("Commission amount is missing for tier number " + tier.getTierNo()).build());

            if (tier.getToAmt() == null)
                throw new MediumException(ErrorData.builder().code("404")
                        .message("To amount is missing for tier number " + tier.getTierNo()).build());

            tier.setCommissionId(request.getCommissionId());
            tier.setCreatedBy(request.getCreatedBy());
            tier.setCreateDate(DataUtils.getCurrentDate());
            serviceCommissionTierRepo.save(tier);
        }
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(CommissionTierData request) throws MediumException {
        serviceCommissionTierRepo.deleteCurrentTierRecords(request.getCommissionId());
        for (ServiceCommissionTier tier : request.getData()) {

            if (tier.getCommissionAmount() == null)
                throw new MediumException(ErrorData.builder().code("404")
                        .message("Charge amount is missing for tier number " + tier.getTierNo()).build());

            if (tier.getToAmt() == null)
                throw new MediumException(ErrorData.builder().code("404")
                        .message("To amount is missing for tier number " + tier.getTierNo()).build());

            tier.setCommissionId(request.getCommissionId());
            tier.setCreatedBy(request.getCreatedBy());
            tier.setCreateDate(DataUtils.getCurrentDate());
            serviceCommissionTierRepo.save(tier);
        }
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
