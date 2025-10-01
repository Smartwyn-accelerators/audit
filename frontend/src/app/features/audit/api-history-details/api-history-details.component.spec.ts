import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ApiHistoryDetailsComponent } from './api-history-details.component';

describe('ApiHistoryDetailsComponent', () => {
  let component: ApiHistoryDetailsComponent;
  let fixture: ComponentFixture<ApiHistoryDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ApiHistoryDetailsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApiHistoryDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
