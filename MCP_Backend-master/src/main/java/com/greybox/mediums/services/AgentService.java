package com.greybox.mediums.services;

import com.greybox.mediums.entities.Account;
import com.greybox.mediums.entities.AgentsRef;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.AccountRepo;
import com.greybox.mediums.repository.AgentsRefRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class AgentService {

    @Autowired
    private AgentsRefRepo userRepo;

    @Autowired
    private AccountRepo accountRepo;

    public TxnResult find(AgentsRef request) {
        List<AgentsRef> customers = userRepo.findAgents();
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult findAgentAccounts(Account request) {
        List<Account> customers = accountRepo.findAgentAccountsByAgentId(request.getAgentId());
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    @Transactional
    public TxnResult save(AgentsRef request) {
        Integer agentCode = userRepo.findMaxAgentCode();
        agentCode = agentCode == null ? 1000 : agentCode + 1;
        request.setAgentCode(agentCode);
        AgentsRef response = userRepo.save(request);

        Account account = new Account();
        account.setAcctNo(request.getCommAcctNo());
        account.setEntityCode("COMMISSION");
        account.setEntityId(response.getAgentId());
        account.setAgentId(response.getAgentId());
        account.setStatus("Active");
        account.setCreatedBy(request.getCreatedBy());
        account.setCreateDt(request.getCreateDt());
        account.setLedgerBal(new BigDecimal(0));
        accountRepo.save(account);

        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(AgentsRef request) {
        userRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
