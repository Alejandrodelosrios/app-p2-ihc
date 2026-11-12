export type AnalyticsEventName = 'cuidado_abierto' | 'cuidado_completado';

export interface AnalyticsEvent {
  id: string;
  name: AnalyticsEventName;
  props: Record<string, string>;
  sessionId: string | null;
  occurredAt: string;
}

export interface AnalyticsSummary {
  started: number;
  completed: number;
  conversionPercent: number;
  recent: AnalyticsEvent[];
}
