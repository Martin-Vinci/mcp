package com.greybox.mediums.services;

import com.greybox.mediums.entities.AccessMenu;
import com.greybox.mediums.entities.AccessMenuRight;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.AccessMenuRepo;
import com.greybox.mediums.repository.AccessMenuRightRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccessMenuRightService {

    @Autowired
    private AccessMenuRightRepo accessMenuRightRepo;
    @Autowired
    private AccessMenuRepo accessMenuRepo;

    public TxnResult findAssignedAccessMenu(Integer memberTypeId) {
        List<AccessMenu> customers = accessMenuRepo.findAssignedAccessMenu(memberTypeId);
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult findUnAssignedAccessMenu(AccessMenuRight request) {
        List<AccessMenu> customers = accessMenuRepo.findUnAssignedAccessMenu(request.getUserTypeId());
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult assignAccessRight(AccessMenuRight[] request) {
        //request.setCreateDt(DateUtils.getCurrentTimeStamp());
        for (AccessMenuRight accessMenuRight : request) {
            accessMenuRightRepo.save(accessMenuRight);
        }
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    @Transactional
    public TxnResult revokeAccessRight(AccessMenuRight[] request) {
        //request.setCreateDt(DateUtils.getCurrentTimeStamp());

        for (AccessMenuRight accessMenuRight : request) {
            accessMenuRepo.deleteMemberAccessType(accessMenuRight.getMenuId(), accessMenuRight.getUserTypeId());
        }


        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
