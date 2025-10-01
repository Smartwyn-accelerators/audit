//
// ===== File globals.ts    
//
'use strict';
import { Injectable } from "@angular/core";
import { BreakpointObserver } from '@angular/cdk/layout';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
@Injectable({
  providedIn: 'root'
})
export class Globals {

  languages: Array<String> = [];
  default_language: string = "";
  constructor(private breakpointObserver: BreakpointObserver) {
    this.isSmallDevice$ = this.breakpointObserver.observe(['(max-width: 768px)'])
      .pipe(
        map(result => result.matches)
      );
    this.isMediumDevice$ = this.breakpointObserver.observe(['(min-width: 768px)  and (max-width: 1024px)'])
      .pipe(
        map(result => result.matches)
      );
    this.isMediumDeviceOrLess$ = this.breakpointObserver.observe(['(min-width: 0px)  and  (max-width: 1024px)'])
      .pipe(
        map(result => result.matches)
      );
    this.isLargeDevice$ = this.breakpointObserver.observe(['(min-width: 1024px) '])
      .pipe(
        map(result => result.matches)
      );
  }
  smaillDeviceBreakpoint!: '768px';
  mediumDeviceBreakpoint!: '1024px';
  isSmallDevice$!: Observable<boolean>;
  isMediumDevice$!: Observable<boolean>;
  isMediumDeviceOrLess$!: Observable<boolean>;
  isLargeDevice$!: Observable<boolean>;
}
