package com.greybox.mediums.services;

import com.greybox.mediums.entities.Account;
import com.greybox.mediums.entities.AgentOutlet;
import com.greybox.mediums.models.ErrorData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.AccountRepo;
import com.greybox.mediums.repository.AgentOutletRepo;
import com.greybox.mediums.utils.MediumException;
import com.greybox.mediums.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class AgentOutletService {

    @Autowired
    private AgentOutletRepo agentOutletRepo;

    @Autowired
    private AccountRepo accountRepo;

    public TxnResult find(AgentOutlet request) {
        List<AgentOutlet> customers = agentOutletRepo.findOutlets(request.getAgentId());
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    @Transactional
    public TxnResult save(AgentOutlet request) throws MediumException {
        Integer outletCode = agentOutletRepo.findOutletCount(request.getAgentId());
        outletCode = outletCode == null ? 100 : outletCode + 1;
        Integer agentCode = agentOutletRepo.findAssociatedAgentCode(request.getAgentId());

        if (agentCode == null)
            throw new MediumException(ErrorData.builder().code("404")
                    .message("Invalid agent id specified").build());


        String outletNo = agentCode + StringUtil.padLeftZeros(outletCode.toString(), 3);
        request.setOutletNo(outletNo);

        AgentOutlet response = agentOutletRepo.save(request);
        Account account = new Account();
        account.setAcctNo(request.getFloatAcctNo());
        account.setEntityCode("OUTLET_FLOAT");
        account.setEntityId(response.getOutletId());
        account.setAgentId(request.getAgentId());
        account.setStatus("Active");
        account.setCreatedBy(request.getCreatedBy());
        account.setCreateDt(request.getCreateDt());
        account.setLedgerBal(new BigDecimal(0));
        accountRepo.save(account);

        agentOutletRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(AgentOutlet request) {
        agentOutletRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
