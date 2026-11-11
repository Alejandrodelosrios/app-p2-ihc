import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { AnalyticsService } from './analytics.service';

describe('AnalyticsService', () => {
  let service: AnalyticsService;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AnalyticsService);
    http = TestBed.inject(HttpTestingController);
    vi.spyOn(console, 'log').mockImplementation(() => {});
  });

  afterEach(() => http.verify());

  it('registra cuidado_abierto una vez con las propiedades esperadas', () => {
    service.track('cuidado_abierto', { modo: 'nuevo' });

    const request = http.expectOne(`${environment.apiUrl}/events`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body.event).toBe('cuidado_abierto');
    expect(request.request.body.props).toEqual({ modo: 'nuevo' });
    request.flush(null);
  });

  it('registra cuidado_completado una vez con las propiedades esperadas', () => {
    service.track('cuidado_completado', { recurrente: 'true', asignado: 'false' });

    const request = http.expectOne(`${environment.apiUrl}/events`);
    expect(request.request.body.event).toBe('cuidado_completado');
    expect(request.request.body.props).toEqual({ recurrente: 'true', asignado: 'false' });
    request.flush(null);
  });

  it('no envía datos personales', () => {
    service.track('cuidado_completado', { recurrente: 'false', asignado: 'false' });

    const request = http.expectOne(`${environment.apiUrl}/events`);
    const sent = JSON.stringify(request.request.body);
    expect(sent).not.toContain('@');
    expect(Object.keys(request.request.body).sort()).toEqual(['event', 'props', 'sessionId']);
    request.flush(null);
  });

  it('no rompe la app si el registro falla', () => {
    expect(() => service.track('cuidado_abierto', { modo: 'nuevo' })).not.toThrow();

    http.expectOne(`${environment.apiUrl}/events`).flush('error', { status: 500, statusText: 'Error' });
  });
});
