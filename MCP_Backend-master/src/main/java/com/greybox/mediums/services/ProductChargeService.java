package com.greybox.mediums.services;

import com.greybox.mediums.entities.ServiceCharge;
import com.greybox.mediums.entities.ServiceChargeTier;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.ServiceChargeRepo;
import com.greybox.mediums.repository.ServiceChargeTierRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductChargeService {

    @Autowired
    private ServiceChargeRepo serviceChargeRepo;

    @Autowired
    private ServiceChargeTierRepo serviceChargeTierRepo;

    public TxnResult find(ServiceCharge request) {
        ServiceCharge customers = serviceChargeRepo.findServiceCharge(request.getServiceId());
        if (customers == null)
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public BigDecimal getServiceCharge(Integer serviceId, BigDecimal principalAmount, BigDecimal excisePercentage) {
        BigDecimal chargeAmount = BigDecimal.ZERO;
        BigDecimal exciseAmount;
        ServiceCharge serviceCharge = serviceChargeRepo.findServiceCharge(serviceId);
        if (serviceCharge != null) {
            if (serviceCharge.getChargeType().trim().equalsIgnoreCase("Amount")) {
                chargeAmount = serviceCharge.getAmount() == null ? BigDecimal.ZERO : serviceCharge.getAmount();
            } else if (serviceCharge.getChargeType().trim().equalsIgnoreCase("Percentage")) {
                chargeAmount = (principalAmount.multiply((serviceCharge.getAmount() == null ? BigDecimal.ZERO : serviceCharge.getAmount()).divide(new BigDecimal(100))));
            } else {
                List<ServiceChargeTier> serviceChargeTier = serviceChargeTierRepo.findChargeTiers(serviceCharge.getChargeId());
                for (ServiceChargeTier tier : serviceChargeTier) {
                    if (principalAmount.compareTo(tier.getFromAmt()) >= 0 && principalAmount.compareTo(tier.getToAmt()) <= 0) {
                        chargeAmount = tier.getChargeAmt() == null ? BigDecimal.ZERO : tier.getChargeAmt();
                        break;
                    }
                }
            }
        }
        return chargeAmount;
    }


    public ServiceCharge findByServiceId(Integer serviceId) {
        ServiceCharge customers = serviceChargeRepo.findServiceCharge(serviceId);
        return customers;
    }


    public TxnResult save(ServiceCharge request) {
        serviceChargeRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(ServiceCharge request) {
        serviceChargeRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
