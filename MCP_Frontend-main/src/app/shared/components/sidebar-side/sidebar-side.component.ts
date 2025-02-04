import { Component, OnInit, OnDestroy, AfterViewInit } from "@angular/core";
import { NavigationService } from "../../../shared/services/navigation.service";
import { ThemeService } from "../../services/theme.service";
import { Subscription } from "rxjs";
import { ILayoutConf, LayoutService } from "app/shared/services/layout.service";
import { JwtAuthService } from "app/shared/services/auth/jwt-auth.service";
import { SecurityService } from "app/shared/services/security.service";

@Component({
  selector: "app-sidebar-side",
  templateUrl: "./sidebar-side.component.html"
})
export class SidebarSideComponent implements OnInit, OnDestroy, AfterViewInit {
  public menuItems: any[];
  public hasIconTypeMenuItem: boolean;
  public iconTypeMenuTitle: string;
  private menuItemsSub: Subscription;
  public layoutConf: ILayoutConf;
  employeeName: string = "";
  constructor(
    private navService: NavigationService,
    public themeService: ThemeService,
    private layout: LayoutService,
    public securityService: SecurityService,
    public jwtAuth: JwtAuthService
  ) { }

  ngOnInit() {
    this.employeeName = this.securityService.currentUser.fullName;
    this.iconTypeMenuTitle = this.navService.iconTypeMenuTitle;
    this.getMenus();
    // this.menuItemsSub = this.navService.menuItems$.subscribe(menuItem => {
    //   this.menuItems = menuItem;
    //   //Checks item list has any icon type.
    //   this.hasIconTypeMenuItem = !!this.menuItems.filter(
    //     item => item.type === "icon"
    //   ).length;
    // });
    this.layoutConf = this.layout.layoutConf;
  }
  ngAfterViewInit() { }
  ngOnDestroy() {
    if (this.menuItemsSub) {
      this.menuItemsSub.unsubscribe();
    }
  }

  private getMenus() {
    let menus =  sessionStorage.getItem('AccessMenus');
    this.menuItems = JSON.parse(menus);
  }



  toggleCollapse() {
    this.layout.publishLayoutChange({
      sidebarCompactToggle: !this.layoutConf.sidebarCompactToggle
    });
  }
}
