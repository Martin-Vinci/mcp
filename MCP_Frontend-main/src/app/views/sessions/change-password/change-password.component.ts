import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ConfirmPasswordValidator } from 'app/shared/helpers/confirm-password-validator';
import { PasswordStrengthValidator } from 'app/shared/helpers/password-strength-validator';
import { AlertService } from 'app/shared/services/alert.service';
import { SecurityService } from 'app/shared/services/security.service';
import { first } from 'rxjs/operators';



@Component({
  selector: 'ngx-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent implements OnInit {
  isRobotVerified = false;
  submitted = false;
  loading = false;
  resetId: number;
  userId: number; 
  userEmail: string;
  loginForm: FormGroup;
  constructor(private formBuilder: FormBuilder,
    private alertService: AlertService,
    private route: ActivatedRoute,
    private router: Router,
    private securityService: SecurityService) {
  }

  ngOnInit(): void {
    this.resetId = this.route.snapshot.params['id'];
    this.createForm();
  }

  createForm() {
    this.loginForm = this.formBuilder.group({
      newPassword: [null, Validators.compose([Validators.required, Validators.minLength(8), PasswordStrengthValidator])],
      confirmPassword: [null, Validators.required],
      oldPassword: [null, Validators.required],
      sysUserId: this.securityService.currentUser.employeeId,
      userName: this.securityService.currentUser.userName,
      resetId: null,
    },
      {
        validator: ConfirmPasswordValidator("newPassword", "confirmPassword")
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
    this.securityService.submitPasswordChange(this.loginForm.value)
      .pipe(first())
      .subscribe(
        data => {
          this.loading = false;
          if (data.code !== "00") {
            this.alertService.displayError(data.message);
            return;
          }
          this.closeDetails();
        },
        error => {
          this.alertService.displayError(error);
          this.loading = false;
        });
  }

  closeDetails() {
    this.router.navigate(['/sessions/signin']);
  }

  handleSuccess(e) {
    this.isRobotVerified = true;
  }

}
