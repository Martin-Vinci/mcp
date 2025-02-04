package com.greybox.mediums.models;

import com.greybox.mediums.entities.AccessMenu;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserAccessRoles {
    List<MainMenu> menuList;
    List<AccessMenu> screenAccessRights;
}
