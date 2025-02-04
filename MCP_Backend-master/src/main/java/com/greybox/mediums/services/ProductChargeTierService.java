package com.greybox.mediums.services;

import com.greybox.mediums.entities.ServiceChargeTier;
import com.greybox.mediums.models.ChargeTierData;
import com.greybox.mediums.models.ErrorData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.ServiceChargeTierRepo;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.MediumException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ProductChargeTierService {

    @Autowired
    private ServiceChargeTierRepo serviceChargeTierRepo;

    public TxnResult find(ServiceChargeTier request) {
        List<ServiceChargeTier> customers = serviceChargeTierRepo.findChargeTiers(request.getChargeId());
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public List<ServiceChargeTier> find(Integer chargeId) {
        List<ServiceChargeTier> response = serviceChargeTierRepo.findChargeTiers(chargeId);
       return response;
    }



    @Transactional
    public TxnResult save(ChargeTierData request) throws MediumException {
        serviceChargeTierRepo.deleteCurrentTierRecords(request.getChargeId());
        for (ServiceChargeTier tier : request.getData()) {

            if (tier.getChargeAmt() == null)
                throw new MediumException(ErrorData.builder().code("404")
                        .message("Charge amount is missing for tier number " + tier.getTierNo()).build());

            if (tier.getChargeAmt() == null)
                throw new MediumException(ErrorData.builder().code("404")
                        .message("To amount is missing for tier number " + tier.getTierNo()).build());

            tier.setChargeId(request.getChargeId());
            tier.setCreatedBy(request.getCreatedBy());
            tier.setCreateDate(DataUtils.getCurrentDate());
            serviceChargeTierRepo.save(tier);
        }
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(ChargeTierData request) {
        for (ServiceChargeTier tier : request.getData()) {
            serviceChargeTierRepo.save(tier);
        }
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
