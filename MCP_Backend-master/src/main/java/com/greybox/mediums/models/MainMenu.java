package com.greybox.mediums.models;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class MainMenu {
    private String name;
    private String type;
    private String tooltip;
    private String icon;
    private String state;
    private List<SubMenu> sub;
}
