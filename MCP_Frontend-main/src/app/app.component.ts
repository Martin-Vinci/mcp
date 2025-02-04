import { Component, OnInit, AfterViewInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Router, NavigationEnd, ActivatedRoute } from '@angular/router';

import { RoutePartsService } from "./shared/services/route-parts.service";
// import { ThemeService } from './shared/services/theme.service';

import { filter, first } from 'rxjs/operators';
import { SecurityService } from './shared/services/security.service';
import { BnNgIdleService } from 'bn-ng-idle';
import { AlertService } from './shared/services/alert.service';
import { RouterServiceService } from './shared/services/router-service.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, AfterViewInit {
  appTitle = 'Micropay Core Agent Banking';
  pageTitle = '';

  constructor(
    public title: Title,
    private router: Router,
    private activeRoute: ActivatedRoute,
    private routePartsService: RoutePartsService,
    private accountService: SecurityService,
    private bnIdle: BnNgIdleService,
    private alert: AlertService,
    public navigation: RouterServiceService,
  ) {
    this.navigation.startSaveHistory();
  }

  ngOnInit() {
    this.changePageTitle();
    this.startSessionMonitor();
  }
  ngAfterViewInit() {
  }
  changePageTitle() {
    this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe((routeChange) => {
      var routeParts = this.routePartsService.generateRouteParts(this.activeRoute.snapshot);
      if (!routeParts.length)
        return this.title.setTitle(this.appTitle);
      // Extract title from parts;
      this.pageTitle = routeParts
        .reverse()
        .map((part) => part.title)
        .reduce((partA, partI) => { return `${partA} > ${partI}` });
      this.pageTitle += ` | ${this.appTitle}`;
      this.title.setTitle(this.pageTitle);
    });
  }


  startSessionMonitor() {
    // 600 = 10 Minutes Idle TimeOut
    this.bnIdle.startWatching(300).subscribe((isTimedOut: boolean) => {
      if (isTimedOut) {
        const user = this.accountService.currentUser;
        if (user) {
          this.logoutUser();
          this.alert.displayInfo('Your Session has expired. please sign in again');
        }
      }
    });
  }

  logoutUser() {
    this.accountService.logoutUser()
      .pipe(first())
      .subscribe(
        data => {
        },
        error => {
          //this.alertService.displayError(error);
        });
  }




}
