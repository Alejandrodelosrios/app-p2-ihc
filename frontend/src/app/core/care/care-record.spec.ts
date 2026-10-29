import { TestBed } from '@angular/core/testing';

import { CareRecordService } from './care-record';

describe('CareRecordService', () => {
  let service: CareRecordService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CareRecordService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
