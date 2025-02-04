import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatButton } from '@angular/material/button';
import { MatProgressBar } from '@angular/material/progress-bar';
import { Router } from '@angular/router';
import { AlertService } from 'app/shared/services/alert.service';
import { SecurityService } from 'app/shared/services/security.service';
import { first } from 'rxjs/operators';
@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit {
  isRobotVerified = false;
  submitted = false;
  loading = false;
  siteKey: string;
  loginForm: FormGroup;
  constructor(private formBuilder: FormBuilder,
    private alertService: AlertService,
    private router: Router,
    private securityDAO: SecurityService) {
    this.siteKey = '6LdYv7gaAAAAAFzPkIrA7RZu1KZJo5bHHS3Dug35';
  }

  ngOnInit(): void {
    this.createForm();
  }

  createForm() {
    this.loginForm = this.formBuilder.group({
      emailAddress: [null, Validators.required]
    });
  }

  get f() {
    return this.loginForm.controls;
  }

  onSubmit() {
    this.submitted = true;
    if (this.loginForm.invalid) {
      return;
    }
    this.loading = true;
    this.securityDAO.resetUserPassword(this.loginForm.value)
      .pipe(first())
      .subscribe(
        data => {
          this.loading = false;
          if (data.code !== "00") {
            this.alertService.displayError(data.message);
            return;
          }
          this.alertService.displayInfo(data.message);
          this.closeDetails();
        },
        error => {
          this.alertService.displayError(error);
          this.loading = false;
        });
  }

  closeDetails() {
    this.router.navigate(['//sessions/signin']);
  }

  handleSuccess(e) {
    this.isRobotVerified = true;
  }

}
