import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class StorageService {
  private entity: BehaviorSubject<any>;
  private product: BehaviorSubject<any>;
  constructor() {
    this.entity = new BehaviorSubject<any>(JSON.parse(sessionStorage.getItem('selectedEntity')));
    this.product = new BehaviorSubject<any>(JSON.parse(sessionStorage.getItem('selectedProduct')));
  }
  setEntity(data: any) {
    sessionStorage.setItem('selectedEntity', JSON.stringify(data));
    this.entity.next(data);
  }
  getEntity(): any {
    return this.entity.value;
  }

  closeEntity() {
    sessionStorage.removeItem("selectedEntity");
    this.entity.next(null);
  }

  setSelectedProduct(data: any) {
    sessionStorage.setItem('selectedProduct', JSON.stringify(data));
    this.product.next(data);
  }

  public getSelectedProduct(): any {
    return this.product.value;
  }

}
