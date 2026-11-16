export type DataFormat = 'json' | 'csv';

/** Lo que pasaría al eliminar la cuenta, calculado en el servidor con los datos reales. */
export interface DeletionSummary {
  activePets: number;
  assignedToMePending: number;
  sharedWithOthersPending: number;
  unsharedPendingDeleted: number;
  historyEntries: number;
}
