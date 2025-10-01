import {
  ViewChild,
  TemplateRef,
}from "@angular/core";
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from "@angular/forms";
import { RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from "@ngx-translate/core";

// Angular Material Modules
import { MatTableModule, MatTableDataSource } from "@angular/material/table";
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { Globals } from "../../core/services/globals";
import { AuditService } from "./audit.service";
import { ErrorService } from "../../core/services/error.service";
import { IAudit } from "./audit";
import { ISearchField, operatorType } from "./ISearchCriteria";
import { UserService } from "../../user/user-service";
import { IUser } from "./iuser";
import { Observable } from "rxjs";
import { AuthEntities, Entities } from "./entity-history/entities";
import { EntityHistoryService } from "./entity-history/entity-history.service";

enum listProcessingType {
  Replace = "Replace",
  Append = "Append"
}

@Component({
  selector: 'app-audit',
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    TranslateModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatChipsModule,
    MatTooltipModule,
    MatPaginatorModule,
    MatSortModule
  ]
})
export class AuditComponent implements OnInit {
  title: string = "Audit";
  itemsObservable!: Observable<any>;
  errorMessage: string = '';
  // displayedColumns: string[] = [ 'Action','API Path','API Method', 'Operation', 'Entity Name', 'User', 'Time', 'Caller Adddress'];
  displayedColumns: string[] = ['action', 'actor', 'origin', 'timestamp', 'APIPath', 'elements', 'navigatedTo','operation','entityName' ];

  public dataSource :any;
  userList: IUser[] = [];
  entityList: string[] = [];
  IsCreatePermission: boolean = true;
  apiHistory: IAudit[] = [];
  states: string[] = [
    'Alabama',
  ];
  state: string = "All";

  filterFields = [];
  basicFilterForm!: FormGroup;

  constructor(
    private global: Globals,
    private apiHistoryService: AuditService,
    public errorService: ErrorService,
    public userService: UserService,
    private formBuilder: FormBuilder,
    private translate: TranslateService,
    private  entityHistoryService: EntityHistoryService
  ) { }

  ngOnInit() {
    console.log('🔍 AuditComponent initialized!');
    this.manageScreenResizing();
    this.getApiHistory();
    this.entityHistoryService.getAllEntities().subscribe(
      {
        next: value =>{
            this.entityList=value;
        },
        error: err=>{
this.errorMessage;
        }
      }
    );


    this.basicFilterForm = this.formBuilder.group({
      user: [''],
      from: [''],
      to: [''],
      api: [''],
      operation: [''],
      entity:['']

    });

    this.basicFilterForm.get('author')?.valueChanges.subscribe(value => this.onPickerSearch(value));
  }

  getApiHistory() {
    this.isLoadingResults = true;
    this.initializePageInfo();
    this.itemsObservable = this.apiHistoryService.getAll(this.searchValue, this.currentPage * this.pageSize, this.pageSize);
    this.processListObservable(this.itemsObservable, listProcessingType.Replace);
  }

  manageScreenResizing() {
    this.global.isMediumDeviceOrLess$.subscribe(value => {
      // this.isMediumDeviceOrLess = value;
      // if (value)
      //   this.displayedColumns = ['API', 'Operation', 'ProcessTime', 'User'];
      // else
      //   this.displayedColumns = ['API', 'Operation', 'ProcessTime', 'User', 'Time', 'Caller Adddress']

    });
  }

  createSearchString() {
    let searchString: string = "";
    let searchFormValue = this.basicFilterForm.getRawValue();
    if (searchFormValue.author) {
      searchString += "userName[eq]=" + searchFormValue.author;
    }
    if (searchFormValue.user) {
      if (searchString.length > 0) {
        searchString += ";";
      }
      searchString += "actor[eq]=" + searchFormValue.user;
    }
    if (searchFormValue.api) {
      if (searchString.length > 0) {
        searchString += ";";
      }
      searchString += "path[eq]=" + searchFormValue.api;
    }

    if (searchFormValue.operation) {
      if (searchString.length > 0) {
        searchString += ";";
      }
      searchString += "operation[eq]=" + searchFormValue.operation;
    }
    if (searchFormValue.entity) {
      if (searchString.length > 0) {
        searchString += ";";
      }
      searchString += "entityName[eq]=" + searchFormValue.entity;
    }


if (searchFormValue.from || searchFormValue.to) {
  let startingValue = searchFormValue.from ? new Date(searchFormValue.from) : null;
  let endingValue = searchFormValue.to ? new Date(searchFormValue.to) : null;

  let formattedStartingValue = startingValue
    ? startingValue.getFullYear() +
      "-" +
      (startingValue.getMonth() + 1).toString().padStart(2, "0") +
      "-" +
      startingValue.getDate().toString().padStart(2, "0")
    : "";

  let formattedEndingValue = endingValue
    ? endingValue.getFullYear() +
      "-" +
      (endingValue.getMonth() + 1).toString().padStart(2, "0") +
      "-" +
      endingValue.getDate().toString().padStart(2, "0")
    : "";

  if (formattedStartingValue || formattedEndingValue) {
    if (searchString.length > 0) {
      searchString += ";";
    }
    searchString += `eventTime[range]=${formattedStartingValue},${formattedEndingValue}`;
  }
}
    // if (searchFormValue.from) {
    //   if (searchString.length > 0) {
    //     searchString += ";";
    //   }
    //   let from = new Date(searchFormValue.from);
    //   searchString += "requestTime=" + from.getFullYear() + "-" + (from.getMonth() + 1) + "-" + from.getDate();// + " " + from.getHours() + ":" + from.getMinutes() + ":" + from.getSeconds() + "." + from.getMilliseconds();
    // }

    // if (searchFormValue.to) {
    //   if (searchString.length > 0) {
    //     searchString += ";";
    //   }
    //   let to = new Date(searchFormValue.to);
    //   searchString += "responseTime=" + to.getFullYear() + "-" + (to.getMonth() + 1) + "-" + to.getDate();// + " " + to.getHours() + ":" + to.getMinutes() + ":" + to.getSeconds() + "." + to.getMilliseconds();
    // }

    return searchString;
  }

  applyFilter() {
    this.searchValue = this.createSearchString();
    this.isLoadingResults = true;
    this.initializePageInfo();
    this.itemsObservable = this.apiHistoryService.getAll(
      this.searchValue,
      this.currentPage * this.pageSize,
      this.pageSize,
    )
    
    this.processListObservable(this.itemsObservable, listProcessingType.Replace);
  }

  clearFilters() {
    this.basicFilterForm.reset();
    this.searchValue = '';
    this.isLoadingResults = true;
    this.initializePageInfo();
    this.itemsObservable = this.apiHistoryService.getAll(
      this.searchValue,
      this.currentPage * this.pageSize,
      this.pageSize,
    )
    
    this.processListObservable(this.itemsObservable, listProcessingType.Replace);
  }

  isLoadingResults = true;

  currentPage!: number;
  pageSize!: number;
  lastProcessedOffset!: number;
  hasMoreRecords!: boolean;
  searchValue: string = "";

  initializePageInfo() {
    this.hasMoreRecords = true;
    this.pageSize = 1000;
    this.lastProcessedOffset = -1;
    this.currentPage = 0;
  }

  //manage pages for virtual scrolling
  updatePageInfo(data: any) {
    if (data.length > 0) {
      this.currentPage++;
      this.lastProcessedOffset += data.length;
    }
    else {
      this.hasMoreRecords = false;
    }
  }

  onTableScroll() {
    if (!this.isLoadingResults && this.hasMoreRecords && this.lastProcessedOffset < this.apiHistory.length) {
      this.isLoadingResults = true;
      this.itemsObservable = this.apiHistoryService.getAll(this.searchValue, this.currentPage * this.pageSize, this.pageSize);
      this.processListObservable(this.itemsObservable, listProcessingType.Append);
    }
  }

  processListObservable(listObservable: Observable<IAudit[]>, type: listProcessingType) {
    listObservable.subscribe(
      apiHistory => {
        this.isLoadingResults = false;
        if (type == listProcessingType.Replace) {
          this.apiHistory = apiHistory;
          this.dataSource = new MatTableDataSource(this.apiHistory);
        }
        else {
          this.apiHistory = this.apiHistory.concat(apiHistory);
          this.dataSource = new MatTableDataSource(this.apiHistory);
        }
        this.updatePageInfo(apiHistory);
      },
      error => {
        this.errorMessage = <any>error
        this.errorService.showError(this.translate.instant('GENERAL.ERRORS.FETCHING-RESULT'));
      }
    )
  }


  /**
   * Author list processing
   */

  getUsers() {
    this.userService.getAll(this.searchValuePicker, this.currentPickerPage * this.pickerPageSize, this.pickerPageSize).subscribe(items => {
      console.log('MyItems :', items);
      this.userList = items;
    })
  }

  isLoadingPickerResults = true;

  currentPickerPage!: number;
  pickerPageSize!: number;
  lastProcessedOffsetPicker!: number;
  hasMoreRecordsPicker!: boolean;

  searchValuePicker: ISearchField[] = [];
  pickerItemsObservable!: Observable<any>;

  /**
   * Initializes/Resets paging information of user data list 
   * showing in autocomplete options.
   */
  initializePickerPageInfo() {
    this.hasMoreRecordsPicker = true;
    this.pickerPageSize = 30;
    this.lastProcessedOffsetPicker = -1;
    this.currentPickerPage = 0;
  }

  /**
   * Manages paging for virtual scrolling for user data list 
   * showing in autocomplete options.
   * @param data Item data from the last service call.
   */
  updatePickerPageInfo(data: any) {
    if (data.length > 0) {
      this.currentPickerPage++;
      this.lastProcessedOffsetPicker += data.length;
    }
    else {
      this.hasMoreRecordsPicker = false;
    }
  }

  /**
   * Loads more user data when 
   * list is scrolled to the bottom (virtual scrolling).
   */
  onPickerScroll() {
    if (!this.isLoadingPickerResults && this.hasMoreRecordsPicker && this.lastProcessedOffsetPicker < this.userList.length) {
      this.isLoadingPickerResults = true;
      this.userService.getAll(this.basicFilterForm.get('author')?.value, this.currentPickerPage * this.pickerPageSize, this.pickerPageSize).subscribe(
        items => {
          this.isLoadingPickerResults = false;
          this.userList = this.userList.concat(items);
          this.updatePickerPageInfo(items);
        },
        error => {
          this.errorMessage = <any>error;
          this.errorService.showError(this.translate.instant('GENERAL.ERRORS.FETCHING-RESULT'));
        }
      );
    }
  }

  /**
   * Loads the user data meeting given criteria.
   * @param searchValue Filters to be applied.
   */
  onPickerSearch(searchValue: string) {

    let searchField: ISearchField = {
      fieldName: "userName",
      operator: operatorType.Contains,
      searchValue: searchValue ? searchValue : ""
    }
    this.searchValuePicker = [searchField];
    this.getUsers();
  }

  showDetails(){
    alert('ok');
  }

}