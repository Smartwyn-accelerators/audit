import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ChangeDetectorRef } from '@angular/core';

import { EntityHistoryComponent } from './entity-history.component';
import { EntityHistoryService } from './entity-history.service';
import { IEntityHistory } from './entityHistory'
import { TestingModule } from 'src/testing/utils';
import { ListFiltersComponent, operatorType, ISearchField } from 'src/app/common/shared';
import { of, Observable } from 'rxjs';
import { IUser } from 'src/app/admin/user-management/user/index';

describe('EntityHistoryComponent', () => {
	let component: EntityHistoryComponent;
	let fixture: ComponentFixture<EntityHistoryComponent>;
	let entityHistoryService: EntityHistoryService;
	let data: IEntityHistory[] = [
		{
			"changeType": "SetChange",
			"globalId": {
				"entity": "com.nfinity.fastcode.domain.Authorization.users.UsersEntity",
				"cdoId": 3
			},
			"commitMetadata": {
				"author": "unauthenticated",
				"properties": [],
				"commitDate": "2018-12-05T11:49:31.869",
				"id": 142.00
			},
			"property": "permissions"
		},
		{
			"changeType": "SetChange",
			"globalId": {
				"entity": "com.nfinity.fastcode.domain.Authorization.users.UsersEntity",
				"cdoId": 2
			},
			"commitMetadata": {
				"author": "unauthenticated",
				"properties": [],
				"commitDate": "2018-12-05T11:49:31.804",
				"id": 141.00
			},
			"property": "permissions"
		}
	]

	let userData: IUser[] = [
		{
			userName: "sample"
		}
	];

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [EntityHistoryComponent, ListFiltersComponent],
			imports: [TestingModule],
			providers: [
				EntityHistoryService,
				ChangeDetectorRef,
			],
		})
			.compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(EntityHistoryComponent);
		entityHistoryService = TestBed.inject(EntityHistoryService);
		component = fixture.componentInstance;
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it('should list entity history', async () => {
		spyOn(component.entityHistoryService, 'getAll').and.returnValue(of(data));
		fixture.detectChanges();
		expect(component.entityHistory.length).toEqual(data.length);
	});

	it('should filter array based on criteria', async () => {
		const filteredArray = [data[0]];
		const sampleSearch = "sampleSearchString";
		component.initializePageInfo();
		spyOn(component.entityHistoryService, 'getAll').and.returnValue(of(filteredArray));
		spyOn(component, 'createSearchString').and.returnValue(sampleSearch);
		fixture.detectChanges();

		component.applyFilter();
		expect(component.entityHistoryService.getAll).toHaveBeenCalledWith(
			sampleSearch,
			0,
			component.pageSize
		)
		expect(component.entityHistory).toEqual(filteredArray);

	});

	it('should load more items onTableScroll when no association is selected', async () => {
		component.initializePageInfo();
		component.isLoadingResults = false;
		spyOn(component.entityHistoryService, 'getAll').and.returnValue(of(data));
		fixture.detectChanges();

		const itemsLength = component.entityHistory.length;
		component.onTableScroll();

		expect(component.entityHistory.length).toEqual(data.length + itemsLength);
	});

	it('should get the list of associated parent', async () => {
		spyOn(component, "initializePickerPageInfo").and.callFake(() => {
			component.hasMoreRecordsPicker = true;
			component.pickerPageSize = 30;
			component.lastProcessedOffsetPicker = -1;
			component.currentPickerPage = 0;
		});
		spyOn(component, 'updatePickerPageInfo').and.returnValue();
		spyOn(component.userService, 'getAll').and.returnValue(of(userData));

		component.getUsers();
		expect(component.updatePickerPageInfo).toHaveBeenCalled();
		expect(component.userList).toEqual(userData);
	});

	it('should show error while fetching the list of users', async () => {
		spyOn(component, "initializePickerPageInfo").and.callFake(() => {
			component.hasMoreRecordsPicker = true;
			component.pickerPageSize = 30;
			component.lastProcessedOffsetPicker = -1;
			component.currentPickerPage = 0;
		});
		spyOn(component.errorService, 'showError').and.returnValue();
		spyOn(component.userService, 'getAll').and.returnValue(Observable.create((observer:any) => {
			observer.error(new Error("an error occurred"))
		}));
		component.getUsers();

		expect(component.errorService.showError).toHaveBeenCalled();
	});

	it('should load more data for the list of users when scrolled', async () => {
		component.hasMoreRecordsPicker = true;
		component.pickerPageSize = 30;
		component.lastProcessedOffsetPicker = -1;
		component.currentPickerPage = 0;
		component.isLoadingPickerResults = false;

		spyOn(component.userService, 'getAll').and.returnValue(of(userData));
		spyOn(component, 'updatePickerPageInfo').and.returnValue();
		fixture.detectChanges();

		component.getUsers();
		component.onPickerScroll();

		expect(component.userList.length).toEqual(userData.length * 2);
		expect(component.isLoadingPickerResults).toEqual(false);
		expect(component.updatePickerPageInfo).toHaveBeenCalled();
	});

	it('should not load more data for the userlist when scrolled', async () => {
		component.hasMoreRecordsPicker = true;
		component.pickerPageSize = 30;
		component.lastProcessedOffsetPicker = -1;
		component.currentPickerPage = 0;
		component.isLoadingPickerResults = false;

		spyOn(component.userService, 'getAll').and.returnValue(Observable.create((observer:any) => {
			observer.error(new Error("an error occurred"))
		}));
		spyOn(component.errorService, 'showError').and.returnValue();

		fixture.detectChanges();
		component.onPickerScroll();

		expect(component.errorService.showError).toHaveBeenCalled();
	});

	it('should not call getAll method of user service if first condition is not true', async () => {
		component.isLoadingPickerResults = true;
		spyOn(component.userService, 'getAll').and.returnValue(of([]));
		component.onPickerScroll();

		expect(component.userService.getAll).toHaveBeenCalledTimes(0);
	});

	it('should load more data for the list of users when some input is entered', async () => {
		let searchValue: string = "test";
		let searchField: ISearchField = {
			fieldName: "userName",
			operator: operatorType.Contains,
			searchValue: searchValue ? searchValue : ""
		}

		spyOn(component, 'getUsers').and.returnValue();

		component.onPickerSearch(searchValue);

		expect(component.getUsers).toHaveBeenCalled();
		expect(component.searchValuePicker).toEqual([searchField]);
	});

	it('should initialize picker page info', async () => {
		component.initializePickerPageInfo();
		expect(component.hasMoreRecordsPicker).toEqual(true);
		expect(component.pickerPageSize).toEqual(30);
		expect(component.lastProcessedOffsetPicker).toEqual(-1);
		expect(component.currentPickerPage).toEqual(0);
	});

	it('should update picker page info for data with length greater than zero', async () => {
		component.hasMoreRecordsPicker = true;
		component.lastProcessedOffsetPicker = -1;
		component.currentPickerPage = 0;

		component.updatePickerPageInfo(data);
		expect(component.hasMoreRecordsPicker).toEqual(true);
		expect(component.lastProcessedOffsetPicker).toEqual(-1 + data.length);
		expect(component.currentPickerPage).toEqual(1);
	});

	it('should update picker page info for data with zero length', async () => {
		component.hasMoreRecordsPicker = true;
		component.lastProcessedOffsetPicker = -1;
		component.currentPickerPage = 0;

		component.updatePickerPageInfo([]);
		expect(component.hasMoreRecordsPicker).toEqual(false);
		expect(component.lastProcessedOffsetPicker).toEqual(-1);
		expect(component.currentPickerPage).toEqual(0);
	});

	it('should create search string search form values', async () => {
		fixture.detectChanges();
		const d = new Date("09-11-2020 19:00:00"); // mm-dd-yyy
		component.basicFilterForm.patchValue({
			author: "testAuthor",
			from: d,
			to: d
		});

		const searchString = component.createSearchString();
		expect(searchString).toEqual("author=testAuthor;from=2020-9-11 19:0:0.0;to=2020-9-11 19:0:0.0");
	});
});
