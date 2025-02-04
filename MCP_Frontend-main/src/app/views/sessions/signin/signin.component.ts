import { Component, OnInit } from "@angular/core";
import {
  FormGroup,
  FormBuilder,
  Validators
} from "@angular/forms";
import { matxAnimations } from "app/shared/animations/matx-animations";
import { Router } from "@angular/router";
import { first } from "rxjs/operators";
import { AuthenticationResponseData, AuthenticationRequestData } from "app/shared/models/authentication-data";
import { ResetPwdData } from "app/shared/models/reset-password-data";
import { AlertService } from "app/shared/services/alert.service";
import { SecurityService } from "app/shared/services/security.service";
import Swal from "sweetalert2";
import { APIResponse } from "app/shared/models/api-response";
import { UserAccessRoles } from "app/shared/models/menu-access-right";

@Component({
  selector: "app-signin",
  templateUrl: "./signin.component.html",
  styleUrls: ["./signin.component.scss"],
  animations: matxAnimations
})
export class SigninComponent implements OnInit {
  form: FormGroup;
  hide = true;
  autoComplete = 'off';
  loading = [false, false];
  submitted = false;
  returnUrl: string;
  ipAddress = "127.0.0.1";
  authData: AuthenticationResponseData;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private accountService: SecurityService,
    private alertService: AlertService) { }

  ngOnInit() {
    this.form = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
      ipAddress: [this.ipAddress]
    });
  }

  validation_messages = {
    username: [{ type: 'required', message: 'Username is required' }],
    password: [{ type: 'required', message: 'Password is required' }],
  };



  // convenience getter for easy access to form fields
  get f() {
    return this.form.controls;
  }
  onSubmit() {
    this.submitted = true;
    if (this.form.invalid) {
      return;
    }

    this.loading[0] = true;
    this.accountService.loginUser(this.form.value)
      .pipe(first())
      .subscribe(
        data => {
          if (data.code !== "00") {
            if (data.code == "-222222") {
              this.f.password.setValue(null);
              this.displaySessionMessage();
              return;
            }

            this.alertService.displayError(data.message);
            this.loading[0] = false;
            localStorage.clear();
            this.f.password.setValue(null);
            return;
          }

          this.authData = JSON.parse(JSON.stringify(data.data));
          if (this.authData.status == "L") {
            this.loading[0] = false;
            this.userLockedDialog();
            return;
          } else if (this.authData.status == "I") {
            this.loading[0] = false;
            this.f.password.setValue(null);
            this.alertService.displayInfo("User has inactive status, contact systems administrator.");
            return;
          }
          // else if (this.authData.isPasswordExpired == "Y") {
          //   this.loading[0] = false;
          //   this.alertService.displayInfo("Your password has expired. You are required to change your password to continue.");
          //   this.passowrdChangeDialog();
          //   return;
          // }
          else if (this.authData.pwdEnhancedFlag == "N") {
            this.loading[0] = false;
            this.alertService.displayInfo("First time password detected. Please change to proceed.");
            this.passowrdChangeDialog();
            return;
          }
          // if (this.authData.isAdmin == "Y") {
          //   localStorage.setItem('loggedInModule', "globalAdmin");
          //   this.router.navigate(['/globalAdmin']);
          //   return;
          // }

          // if (this.authData.licenseDays <= 30) {
          //   this.confirmLicenseExpiry(this.authData);
          //   return;
          // }


          this.accountService.getMenus(this.authData.userRoleId).subscribe((data: APIResponse) => {
            if (data.code !== "00") {
              this.loading[0] = false;
              this.alertService.displayError("Unable to determine system access rights");
              return;
            }
            this.loading[0] = false;
            let x = new UserAccessRoles;
            x = data.data;
            sessionStorage.setItem('AccessMenus', JSON.stringify(x.menuList).trim());
            sessionStorage.setItem('ScreenAccessRights', JSON.stringify(x.screenAccessRights).trim());


            this.router.navigate(['/dashboard/analytics']);
          })

        },
        error => {
          this.alertService.displayError(error);
          this.loading[0] = false;
        });
  }

  userLockedDialog() {
    this.alertService.displayInfo("This user account has been locked due to failed login attempts. Please contact system admin.");
  }

  passwordExpiryDialog() {
    Swal.fire({
      title: 'Password Expiry Message',
      text: "Your password has expired. You are required to change your password to continue.",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Yes, delete it!'
    }).then((isChangeSelected) => {
      if (isChangeSelected.value) {
        this.passowrdChangeDialog();
      } else {
        return;
      }
    });
  }

  passowrdChangeDialog() {
    this.router.navigate(['/sessions/change-password']);
  }

  passwordResetDialog() {
    let resetPwdData = new ResetPwdData();
    resetPwdData.userName = this.f.username.value;
    resetPwdData.isAdmin = false;
    this.router.navigate(['/sessions/forgot-password']);
  }

  onKeyUP(inPutValue: string): void {
    this.f.username.setValue(inPutValue.toUpperCase());
  }


  goToPasswordReset() {
    this.passwordResetDialog();
  }


  confirmLicenseExpiry(authData: AuthenticationResponseData): void {
    Swal.fire({
      title: 'License',
      text: 'Your license will expire in ' + authData.licenseDays + ' days. Contact your systems administrator for renewal.',
      icon: 'warning',
      showCancelButton: false,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Yes, delete it!'
    }).then((isChangeSelected) => {
      if (isChangeSelected.value) {
        this.passowrdChangeDialog();
      } else {
        return;
      }
    });
  }

  playStartUpSound() {
    let audio = new Audio();
    audio.src = "../../../assets/audio/vista.mp3";
    audio.crossOrigin = 'anonymous';
    audio.load();
    audio.play();
  }

  displaySessionMessage() {
    const message = 'User is already logged in eQuiWeb, Clear current session to proceed.';
    Swal.fire({
      title: 'Session Error',
      text: message,
      icon: 'warning',
      showCancelButton: false,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Yes, delete it!'
    }).then((isChangeSelected) => {
      if (isChangeSelected.value) {
        //this.confirmToResetSessions();
      } else {
        return;
      }
    });
  }
}
