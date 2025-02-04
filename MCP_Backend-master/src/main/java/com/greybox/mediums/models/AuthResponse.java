package com.greybox.mediums.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.greybox.mediums.entities.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse extends User {
    private String processDate;
    private String securityRoleDesc;
    private String token;
    private Integer licenseDays;
}
