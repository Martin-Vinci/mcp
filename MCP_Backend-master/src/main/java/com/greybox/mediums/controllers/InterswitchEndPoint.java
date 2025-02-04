package com.greybox.mediums.controllers;


import com.greybox.mediums.inter_switch.PaymentsService;
import com.greybox.mediums.inter_switch.dto.PaymentRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interswitch")
public class InterswitchEndPoint {

    private PaymentsService paymentsService;

    public InterswitchEndPoint(PaymentsService paymentsService) {
        this.paymentsService = paymentsService;
    }

    @PostMapping("/validation")
    public String validateCustomer(@RequestBody PaymentRequest request) throws Exception {
        return paymentsService.validateCustomer(request);
    }

    @PostMapping("/pay")
    public String doPayment(@RequestBody PaymentRequest registrationDetail) throws Exception {
        return paymentsService.makePayment(registrationDetail);
    }

    @PostMapping("/getCategories")
    public String getCategories() throws Exception {
        return paymentsService.getCategories();
    }
    @PostMapping("/getCategoryBillers")
    public String getCategoryBillers(long categoryId) throws Exception {
        return paymentsService.getCategoryBillers(categoryId);
    }
    @PostMapping("/getPaymentItems")
    public String getPaymentItems(long billerId) throws Exception {
        return paymentsService.getPaymentItems(billerId);
    }

}
