import { Inject, Injectable } from "@angular/core";
import { BehaviorSubject, Observable } from "rxjs";

import { NgxUiLoaderService, NgxUiLoaderConfig } from "ngx-ui-loader";

@Injectable({
  providedIn: "root"
})
export class LoaderService {
  private _config: BehaviorSubject<NgxUiLoaderConfig>;
  config: Observable<NgxUiLoaderConfig>;

  constructor(public ngxUiLoaderService: NgxUiLoaderService) {
    this._config = new BehaviorSubject<NgxUiLoaderConfig>({});
    this.config = this._config.asObservable();
  }

  start(config: any) {
    this._config.next(config);
    this.ngxUiLoaderService.start();
  }

  stop() {
    this.ngxUiLoaderService.stop();
  }
}
