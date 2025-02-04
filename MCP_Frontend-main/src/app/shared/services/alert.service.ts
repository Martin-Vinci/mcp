import { Injectable } from "@angular/core";
import Swal from "sweetalert2";

@Injectable({ providedIn: 'root' })
export class AlertService {

    constructor(
        // public modalService: ModalService
    ) {

    }

    displayError(message: string) {
        Swal.fire({
            icon: 'error',
            text: message,
          })
    }

    displayInfo(message: string) {
        Swal.fire({
            icon: 'info',
            text: message,
          })
    }

    displaySuccess(message: string) {
        Swal.fire({
            icon: 'success',
            text: message,
          })
    }
}