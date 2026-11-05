import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { CareRecord } from './care-record';

describe('CareRecord', () => {
  let component: CareRecord;
  let fixture: ComponentFixture<CareRecord>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CareRecord],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(CareRecord);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
