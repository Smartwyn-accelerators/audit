import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

// Angular Material Modules
import { MatTableModule } from '@angular/material/table';
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

import { IEntityHistory } from './entityHistory';
import { EntityHistoryService } from './entity-history.service';
import { MatTableDataSource } from "@angular/material/table";
import { Observable } from 'rxjs';
import { IUser } from '../iuser';
import { Globals } from '../../../core/services/globals';
import { ErrorService } from '../../../core/services/error.service';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ISearchField, operatorType } from '../ISearchCriteria';
import { AuthEntities, Entities } from './entities';
import { UserService } from '../../../user/user-service';

enum listProcessingType {
  Replace = "Replace",
  Append = "Append"
}

@Component({
  selector: 'app-entity-history',
  templateUrl: './entity-history.component.html',
  styleUrls: ['./entity-history.component.scss'],
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
export class EntityHistoryComponent implements OnInit {
  title!: string;
  entityHistory: IEntityHistory[] = [];
  itemsObservable!: Observable<IEntityHistory[]>;
  errorMessage: string = '';
  displayedColumns: string[] = ['entity', 'cdoId', 'changeType', 'author', 'commitDate', 'propertyName', 'previousValue', 'currentValue'];

  public dataSource: any;
  userList: IUser[] = [];
  entityList = [...Entities, ...AuthEntities];

  filterFields = [];
  basicFilterForm!: FormGroup;

  isLoadingResults: boolean = true;
  currentPage: number = 0;
  pageSize: number = 10;
  lastProcessedOffset: number = -1;
  hasMoreRecords: boolean = true;
  searchValue: string = "";

  isLoadingPickerResults: boolean = true;
  currentPickerPage: number = 0;
  pickerPageSize: number = 30;
  lastProcessedOffsetPicker: number = -1;
  hasMoreRecordsPicker: boolean = true;
  searchValuePicker: ISearchField[] = [];
  pickerItemsObservable!: Observable<any>;

  constructor(
    public global: Globals,
    public entityHistoryService: EntityHistoryService,
    public errorService: ErrorService,
    public userService: UserService,
    public formBuilder: FormBuilder,
    public translate: TranslateService,
  ) { 
    this.initializeForm();
  }

  ngOnInit() {
    this.title = this.translate.instant('MainNav.EntityHistory');
    this.manageScreenResizing();
    this.getEntityHistory();
    this.basicFilterForm.get('author')?.valueChanges.subscribe(value => this.onPickerSearch(value));
  }

  private initializeForm() {
    this.basicFilterForm = this.formBuilder.group({
      author: [''],
      entity: [''],
      from: [''],
      to: ['']
    });
  }

  getEntityHistory() {
    this.isLoadingResults = true;
    this.initializePageInfo();
    this.itemsObservable = this.entityHistoryService.getAll(this.searchValue, this.currentPage * this.pageSize, this.pageSize);
    this.processListObservable(this.itemsObservable, listProcessingType.Replace);
  }

  manageScreenResizing() {
    this.global.isMediumDeviceOrLess$.subscribe((value: boolean) => {
      // this.isMediumDeviceOrLess = value;
      // if (value)
      //   this.displayedColumns = ['entity', 'cdoId', 'commitDate', 'author'];
      // else
      //   this.displayedColumns = ['entity', 'cdoId', 'changeType', 'author', 'commitDate', 'propertyName', 'previousValue', 'currentValue']
    });
  }

  createSearchString() {
    let searchString: string = "";
    let searchFormValue = this.basicFilterForm.getRawValue();
    if (searchFormValue.author) {
      searchString += "author=" + searchFormValue.author;
    }

    if (searchFormValue.from) {
      if (searchString.length > 0) {
        searchString += ";";
      }
      let from = new Date(searchFormValue.from);
      searchString += `from=${from.getFullYear()}-${from.getMonth() + 1}-${from.getDate()} ${from.getHours()}:${from.getMinutes()}:${from.getSeconds()}.${from.getMilliseconds()}`;
    }

    if (searchFormValue.to) {
      if (searchString.length > 0) {
        searchString += ";";
      }
      let to = new Date(searchFormValue.to);
      searchString += `to=${to.getFullYear()}-${to.getMonth() + 1}-${to.getDate()} ${to.getHours()}:${to.getMinutes()}:${to.getSeconds()}.${to.getMilliseconds()}`;
    }

    return searchString;
  }

  applyFilter() {
    this.searchValue = this.createSearchString();
    this.isLoadingResults = true;
    this.initializePageInfo();
    if (!this.basicFilterForm.value.entity) {
      this.itemsObservable = this.entityHistoryService.getAll(
        this.searchValue,
        this.currentPage * this.pageSize,
        this.pageSize
        );
    } else {
      this.itemsObservable = this.entityHistoryService.getByEntity(this.basicFilterForm.value.entity,
        this.searchValue,
        this.currentPage * this.pageSize,
        this.pageSize
        );
    }
    this.processListObservable(this.itemsObservable, listProcessingType.Replace);
  }

  initializePageInfo() {
    this.hasMoreRecords = true;
    this.pageSize = 10;
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
    if (!this.isLoadingResults && this.hasMoreRecords && this.lastProcessedOffset < this.entityHistory.length) {
      this.isLoadingResults = true;
      if (!this.basicFilterForm.value.entity) {
        this.itemsObservable = this.entityHistoryService.getAll(
          this.searchValue,
          this.currentPage * this.pageSize,
          this.pageSize
          );
      } else {
        this.itemsObservable = this.entityHistoryService.getByEntity(this.basicFilterForm.value.entity,
          this.searchValue,
          this.currentPage * this.pageSize,
          this.pageSize
          );
      }
      this.processListObservable(this.itemsObservable, listProcessingType.Append);
    }
  }

  processListObservable(listObservable: Observable<IEntityHistory[]>, type: listProcessingType) {
    listObservable.subscribe(
      entityHistory => {
        this.isLoadingResults = false;
        if (type == listProcessingType.Replace) {
          this.entityHistory = entityHistory;
          this.dataSource = new MatTableDataSource(this.entityHistory);
        }
        else {
          this.entityHistory = this.entityHistory.concat(entityHistory);
          this.dataSource = new MatTableDataSource(this.entityHistory);
        }
        this.updatePageInfo(entityHistory);
      },
      (error: any) => {
        this.errorMessage = <any>error
        this.errorService.showError(this.translate.instant('GENERAL.ERRORS.FETCHING-RESULT'));
      }
    )
  }

  /**
   * Author list processing
   */
  getUsers() {
    this.userService.getAll(this.searchValuePicker, this.currentPickerPage * this.pickerPageSize, this.pickerPageSize).subscribe((items: any) => {
      this.userList = items;
    	this.updatePickerPageInfo(items);
    },
    (error: any) => {
      this.errorMessage = <any>error;
      this.errorService.showError(this.translate.instant('GENERAL.ERRORS.FETCHING-RESULT'));
    })
  }

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
        (items: any) => {
          this.isLoadingPickerResults = false;
          this.userList = this.userList.concat(items);
          this.updatePickerPageInfo(items);
        },
        (error: any) => {
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
}
