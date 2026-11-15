import { deletePetConfirmMessage } from './delete-pet-message';

describe('mensaje de confirmación al eliminar una mascota', () => {
  const message = deletePetConfirmMessage('Luna');

  it('nombra a la mascota que se va a eliminar', () => {
    expect(message).toContain('Luna');
  });

  it('avisa que los cuidados y el historial se conservan', () => {
    expect(message).toMatch(/cuidados/i);
    expect(message).toMatch(/historial/i);
    expect(message).toMatch(/conserv/i);
  });

  it('avisa que ya no se podrán registrar cuidados nuevos', () => {
    expect(message).toMatch(/cuidados nuevos/i);
  });

  it('es honesto: dice que no se puede recuperar desde la app', () => {
    expect(message).toMatch(/recuperar/i);
  });
});
