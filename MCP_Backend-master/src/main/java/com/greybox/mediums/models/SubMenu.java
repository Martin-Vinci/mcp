package com.greybox.mediums.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubMenu {
    private String name;
    private String state;
}
