export class MenuAccessRight {
    menuId: number;
    description: string;
    accessScope: string;
    menuCode: string;
}


export class RouteInfo {
    name: String;
    type: String;
    tooltip: String;
   icon: String;
   state: String;
   sub: SubMenu[];
}

export class SubMenu {    
    name: String;    
    state: String;
}

export class UserAccessRoles {
    menuList: RouteInfo[];
    screenAccessRights: MenuAccessRight[];
}