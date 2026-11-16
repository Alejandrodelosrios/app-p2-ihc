package com.ihc.mascotas.backend.account.application;

/**
 * Lo que pasaría si la persona elimina su cuenta, calculado con sus datos reales
 * para que el aviso de confirmación sea honesto y no un texto genérico.
 *
 * @param activePets             mascotas propias que se desactivan
 * @param assignedToMePending    cuidados pendientes que otros le asignaron: se le revoca el acceso
 * @param sharedWithOthersPending cuidados pendientes que creó y asignó a otras personas: siguen
 *                               visibles para ellas, pero nadie podrá editarlos
 * @param unsharedPendingDeleted cuidados pendientes que creó sin asignar a nadie: se eliminan
 * @param historyEntries         registros de historial suyos: se conservan con su nombre como autor
 */
public record DeletionSummary(
        int activePets,
        int assignedToMePending,
        int sharedWithOthersPending,
        int unsharedPendingDeleted,
        int historyEntries) {
}