package com.greybox.mediums.services;

import com.greybox.mediums.entities.*;
import com.greybox.mediums.models.*;
import com.greybox.mediums.repository.MobileUserRepo;
import com.greybox.mediums.repository.ReportRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ReportRepo reportRepo;
    @Autowired
    private MobileUserRepo mobileUserRepo;


    public TxnResult findTransactions(TransactionRef request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        System.out.println(dateFormat.format(new Date()) + " =========== Entering service for transaction execution");
        List<TransactionRef> customers = reportRepo.findTransactions(request);
        System.out.println(dateFormat.format(new Date()) + " =========== Transaction report execution completed. Proceeding to return response to client");
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult findActiveAgents(MobileUser request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        System.out.println(dateFormat.format(new Date()) + " =========== Entering service for transaction execution");
        List<MobileUser> customers = reportRepo.findActiveAgents(request);
        System.out.println(dateFormat.format(new Date()) + " =========== Transaction report execution completed. Proceeding to return response to client");
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }


    public TxnResult findTransactionSummary(SearchCriteria request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        System.out.println(dateFormat.format(new Date()) + " =========== Entering service for transaction execution");
        List<TransactionSummary> customers = this.reportRepo.findTransactionSummary(request);
        System.out.println(dateFormat.format(new Date()) + " =========== Transaction report execution completed. Proceeding to return response to client");
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved")
                .code("00").data(customers).build();
    }


    public TxnResult findTransactionBands(TransactionBand request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        System.out.println(dateFormat.format(new Date()) + " =========== Entering service for transaction execution");
        List<TransactionBand> customers = reportRepo.findTransactionBands(request);
        System.out.println(dateFormat.format(new Date()) + " =========== Transaction report execution completed. Proceeding to return response to client");
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult findDashboardStatistics(SearchCriteria request) {
        List<DashboardCardData> dataList = new ArrayList<>();
        request.setCategories("AGENT");
        Integer newAgents = reportRepo.findNewUsers(request);
        request.setCategories("OUTLET");
        Integer newOutlets = reportRepo.findNewUsers(request);
        BigDecimal bankCommission = reportRepo.findBankCommission(request);
        BigDecimal bankExpenses = reportRepo.findBankExpenses(request);

        dataList.add(DashboardCardData.builder()
                .icon("people")
                .title("New Agents - Current Month")
                .amount(BigDecimal.valueOf(newAgents))
                .color("primary").build());
        dataList.add(DashboardCardData.builder()
                .icon("attach_money")
                .title("New Outlets - Current Month")
                .amount(BigDecimal.valueOf(newOutlets))
                .color("warn").build());
        dataList.add(DashboardCardData.builder()
                .icon("store")
                .title("Micropay Commission-Current Month")
                .amount(bankCommission)
                .color("accent").build());
        dataList.add(DashboardCardData.builder()
                .icon("shopping_cart")
                .title("Total Expenses - Current Month")
                .amount(bankExpenses)
                .color("default").build());
        if (dataList == null || dataList.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(dataList).build();
    }

    public TxnResult findAgentFloatLevels(SearchCriteria request) {
        request.setCategories("FLOAT_ACCOUNT");
        List<MobileUserAccount> accountList = reportRepo.findUserBalances(request);
        if (accountList == null || accountList.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        Integer low = 0, medium = 0, high = 0;
        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).getBalanceLevels().equals("LOW"))
                low++;
            else if (accountList.get(i).getBalanceLevels().equals("MEDIUM"))
                medium++;
            else if (accountList.get(i).getBalanceLevels().equals("HIGH"))
                high++;
        }
        String[] lables = {"LOW", "MEDIUM", "HIGH"};
        Integer[] values = {low, medium, high};
        return TxnResult.builder().message("approved").
                code("00").data(DashboardPieChartData.builder().lables(lables).values(values).build()).build();
    }

    public TxnResult findCustomerAccountBalanceLevels(SearchCriteria request) {
        request.setCategories("CUSTOMER");
        List<MobileUserAccount> accountList = reportRepo.findUserBalances(request);
        if (accountList == null || accountList.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        Integer low = 0, medium = 0, high = 0;
        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).getBalanceLevels().equals("LOW"))
                low++;
            else if (accountList.get(i).getBalanceLevels().equals("MEDIUM"))
                medium++;
            else if (accountList.get(i).getBalanceLevels().equals("HIGH"))
                high++;
        }
        String[] lables = {"LOW", "MEDIUM", "HIGH"};
        Integer[] values = {low, medium, high};
        return TxnResult.builder().message("approved").
                code("00").data(DashboardPieChartData.builder().lables(lables).values(values).build()).build();
    }

    public TxnResult findActiveCustomerCategories(SearchCriteria request) {
        List<MobileUser> accountList = mobileUserRepo.findActiveCustomers();
        if (accountList == null || accountList.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        Integer male = 0, female = 0, others = 0;
        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).getGender() == null)
                others++;
            else if (accountList.get(i).getGender().equals("M"))
                male++;
            else if (accountList.get(i).getGender().equals("F"))
                female++;
            else
                others++;
        }
        String[] lables = {"Male", "Female", "Others"};
        Integer[] values = {male, female, others};
        return TxnResult.builder().message("approved").
                code("00").data(DashboardPieChartData.builder().lables(lables).values(values).build()).build();
    }

    public TxnResult findActiveAgentsByTransactions(SearchCriteria request) {
        List<DashboardActiveOutlet> accountList = reportRepo.findActiveAgentsByTransactions(request);
        if (accountList == null || accountList.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(
                        accountList
                ).build();
    }

    public TxnResult findUserAccountsByCategoryAndFloatLevels(SearchCriteria request) {
        List<MobileUserAccount> accountList = reportRepo.findUserBalances(request);
        if (accountList == null || accountList.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(accountList).build();
    }

    public TxnResult findMobileUsersByGender(SearchCriteria request) {
        List<MobileUser> accountList;
        if (request.getScope().equals("MALE"))
            accountList = mobileUserRepo.findActiveCustomerByGender("M");
        else if (request.getScope().equals("FEMALE"))
            accountList = mobileUserRepo.findActiveCustomerByGender("F");
        else
            accountList = mobileUserRepo.findActiveCustomerWithoutGender();

        if (accountList == null || accountList.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(accountList).build();
    }

    public TxnResult findCustomers(MobileUser request) {
        List<MobileUser> customers = reportRepo.findCustomers(request);
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult findTransactionVouchers(TransactionVoucher request) {
        List<TransactionVoucher> customers = reportRepo.findTransactionVouchers(request);
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult findUsers(User request) {
        List<User> customers = reportRepo.findUsers(request);
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }


    public TxnResult findAgentWithHoldingTax(MobileUser request) {
        List<MobileUser> customers = reportRepo.findAgentWithHoldingTax(request);
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }



}
