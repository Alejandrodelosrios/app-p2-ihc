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

  it('rejects a past care date', () => {
    const pastDate = new Date();
    pastDate.setDate(pastDate.getDate() - 1);
    const dateValue = `${pastDate.getFullYear()}-${String(pastDate.getMonth() + 1).padStart(2, '0')}-${String(pastDate.getDate()).padStart(2, '0')}`;

    component.form.controls.careDate.setValue(dateValue);
    component.form.controls.careDate.markAsTouched();

    expect(component.form.controls.careDate.hasError('pastDate')).toBe(true);
    expect(component.careDateError()).toBe('La fecha no puede ser anterior a hoy');
  });
});
