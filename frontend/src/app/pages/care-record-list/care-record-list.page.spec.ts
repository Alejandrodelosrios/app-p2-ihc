import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { CareRecordListPage } from './care-record-list.page';

describe('CareRecordListPage', () => {
  let component: CareRecordListPage;
  let fixture: ComponentFixture<CareRecordListPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CareRecordListPage],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(CareRecordListPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
