package com.greybox.mediums.services;

import com.greybox.mediums.entities.ServiceCommission;
import com.greybox.mediums.entities.ServiceCommissionTier;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.ServiceCommissionRepo;
import com.greybox.mediums.repository.ServiceCommissionTierRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ProductCommissionService {

    @Autowired
    private ServiceCommissionRepo serviceCommissionRepo;

    @Autowired
    private ServiceCommissionTierRepo serviceCommissionTierRepo;

    public TxnResult find(ServiceCommission request) {
        ServiceCommission customers = serviceCommissionRepo.findServiceCommission(request.getServiceId());
        if (customers == null)
            return TxnResult.builder().code("404").message("No records found").build();
        return TxnResult.builder().message("approved").code("00").data(customers).build();
    }

    public ServiceCommission getServiceCommission(Integer serviceId, BigDecimal principalAmount, BigDecimal chargeAmount) {
        BigDecimal commissionAmount = BigDecimal.ZERO;
        BigDecimal vendorShare = BigDecimal.ZERO;
        ServiceCommission serviceCommission = this.serviceCommissionRepo.findServiceCommission(serviceId);
        if (serviceCommission != null) {
            System.out.println("============== Commission Type: " + serviceCommission.getCommissionType().trim());
            if (serviceCommission.getCommissionType().trim().equalsIgnoreCase("Amount")) {
                commissionAmount = (serviceCommission.getAmount() == null) ? BigDecimal.ZERO : serviceCommission.getAmount();
                vendorShare = (serviceCommission.getVendorShare() == null) ? BigDecimal.ZERO : serviceCommission.getVendorShare();
            } else if (serviceCommission.getCommissionType().trim().equalsIgnoreCase("Percentage")) {
                if (serviceCommission.getCalculationBasis() == null) {
                    commissionAmount = chargeAmount.multiply(((serviceCommission.getAmount() == null) ? BigDecimal.ZERO :
                            serviceCommission.getAmount()).divide(new BigDecimal(100), RoundingMode.HALF_UP));
                    vendorShare = chargeAmount.multiply(((serviceCommission.getVendorShare() == null) ? BigDecimal.ZERO :
                            serviceCommission.getVendorShare()).divide(new BigDecimal(100), RoundingMode.HALF_UP));
                } else if (serviceCommission.getCalculationBasis().equals("TRANS_AMOUNT")) {
                    commissionAmount = principalAmount.multiply(((serviceCommission.getAmount() == null) ? BigDecimal.ZERO :
                            serviceCommission.getAmount()).divide(new BigDecimal(100)));
                    vendorShare = principalAmount.multiply(((serviceCommission.getVendorShare() == null) ? BigDecimal.ZERO :
                            serviceCommission.getVendorShare()).divide(new BigDecimal(100)));
                } else {
                    commissionAmount = chargeAmount.multiply(((serviceCommission.getAmount() == null) ? BigDecimal.ZERO :
                            serviceCommission.getAmount()).divide(new BigDecimal(100), RoundingMode.HALF_UP));
                    vendorShare = chargeAmount.multiply(((serviceCommission.getVendorShare() == null) ? BigDecimal.ZERO :
                            serviceCommission.getVendorShare()).divide(new BigDecimal(100), RoundingMode.HALF_UP));
                }
            } else {
                List<ServiceCommissionTier> serviceChargeTier = this.serviceCommissionTierRepo.findCommissionTiers(serviceCommission.getCommissionId());
                for (ServiceCommissionTier tier : serviceChargeTier) {
                    System.out.println("Tier Logic From Amount: " + tier.getFromAmt() + " To amount " + tier.getToAmt());
                    if (principalAmount.compareTo(tier.getFromAmt()) >= 0 && principalAmount.compareTo(tier.getToAmt()) <= 0) {
                        commissionAmount = (tier.getCommissionAmount() == null) ? BigDecimal.ZERO : tier.getCommissionAmount();
                        vendorShare = (tier.getVendorShare() == null) ? BigDecimal.ZERO : tier.getVendorShare();
                        break;
                    }
                }
            }
        }
        ServiceCommission commission = new ServiceCommission();
        commission.setAmount(commissionAmount);
        commission.setVendorShare(vendorShare);
        return commission;
    }

    public ServiceCommission findByServiceId(Integer serviceId) {
        ServiceCommission customers = serviceCommissionRepo.findServiceCommission(serviceId);
        return customers;
    }

    public TxnResult save(ServiceCommission request) {
        serviceCommissionRepo.save(request);
        return TxnResult.builder().message("approved").code("00").data(request).build();
    }

    public TxnResult update(ServiceCommission request) {
        serviceCommissionRepo.save(request);
        return TxnResult.builder().message("approved").code("00").data(request).build();
    }
}
