/**
 * Texto del diálogo de confirmación al eliminar una mascota.
 * Describe lo que realmente pasa (borrado lógico en el backend): la mascota
 * desaparece de "Mis mascotas", pero sus cuidados e historial se conservan.
 */
export function deletePetConfirmMessage(petName: string): string {
  return [
    `¿Eliminar a ${petName}?`,
    '',
    '• Dejará de aparecer en "Mis mascotas" y no podrás registrarle cuidados nuevos.',
    '• Sus cuidados e historial se conservan.',
    '• No podrás recuperarla desde la app.',
  ].join('\n');
}
