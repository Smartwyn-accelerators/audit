import { TestBed } from '@angular/core/testing';

import { SessionTrackingService } from './session-tracking.service';

describe('SessionTrackingService', () => {
  let service: SessionTrackingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SessionTrackingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
