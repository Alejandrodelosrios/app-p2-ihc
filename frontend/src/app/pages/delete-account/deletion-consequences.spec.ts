import { DeletionSummary } from '../../models/account.model';
import { deletionConsequences } from './deletion-consequences';

const empty: DeletionSummary = {
  activePets: 0,
  assignedToMePending: 0,
  sharedWithOthersPending: 0,
  unsharedPendingDeleted: 0,
  historyEntries: 0,
};

describe('consecuencias de eliminar la cuenta', () => {
  it('siempre avisa que no se podrá iniciar sesión, que se borran los datos y que no se deshace', () => {
    const text = deletionConsequences(empty).join(' ');
    expect(text).toMatch(/no podrás volver a iniciar sesión/i);
    expect(text).toMatch(/correo y tu contraseña se borran/i);
    expect(text).toMatch(/nombre se conserva/i);
    expect(text).toMatch(/no se puede deshacer/i);
  });

  it('no menciona lo que no le aplica a la persona', () => {
    const text = deletionConsequences(empty).join(' ');
    expect(text).not.toMatch(/mascota/i);
    expect(text).not.toMatch(/historial/i);
    expect(text).not.toMatch(/cuidado/i);
  });

  it('usa los números reales de la persona', () => {
    const lines = deletionConsequences({
      activePets: 2,
      assignedToMePending: 3,
      sharedWithOthersPending: 1,
      unsharedPendingDeleted: 4,
      historyEntries: 5,
    });
    const text = lines.join(' ');
    expect(text).toContain('Tus 2 mascotas');
    expect(text).toContain('3 cuidados pendientes que te asignaron');
    expect(text).toContain('1 cuidado pendiente que creaste y asignaste a otra persona');
    expect(text).toContain('4 cuidados pendientes que creaste sin asignar');
    expect(text).toContain('Tus 5 registros del historial');
  });

  it('conjuga bien en singular', () => {
    const text = deletionConsequences({
      activePets: 1,
      assignedToMePending: 1,
      sharedWithOthersPending: 0,
      unsharedPendingDeleted: 1,
      historyEntries: 1,
    }).join(' ');
    expect(text).toContain('Tu mascota se desactiva');
    expect(text).toContain('sigue con la persona que lo creó');
    expect(text).toContain('se elimina');
    expect(text).toContain('Tu registro del historial se conserva');
  });

  it('explica la revocación de accesos y la conservación del historial', () => {
    const text = deletionConsequences({ ...empty, assignedToMePending: 2, historyEntries: 2 }).join(' ');
    expect(text).toMatch(/Pierdes el acceso/);
    expect(text).toMatch(/se conservan con tu nombre como autor/);
  });
});