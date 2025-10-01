import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuditLibrary } from './audit-library';

describe('AuditLibrary', () => {
  let component: AuditLibrary;
  let fixture: ComponentFixture<AuditLibrary>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuditLibrary]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AuditLibrary);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
