export type AnalyticsEventName =
  | 'cuidado_abierto'
  | 'cuidado_completado'
  | 'registro_abierto'
  | 'registro_completado'
  | 'login_intentado'
  | 'login_completado'
  | 'recuperacion_solicitada'
  | 'contrasena_restablecida'
  | 'mascota_formulario_abierto'
  | 'mascota_creada'
  | 'mascota_edicion_abierta'
  | 'mascota_actualizada'
  | 'mascota_eliminada'
  | 'cuidado_edicion_abierta'
  | 'cuidado_actualizado'
  | 'cuidado_eliminado'
  | 'cuidado_reprogramado'
  | 'cuidado_realizado'
  | 'cuidado_asignado'
  | 'recurrencia_configurada'
  | 'eliminar_cuenta_inicio'
  | 'eliminar_cuenta_exito'
  | 'datos_descargados';

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