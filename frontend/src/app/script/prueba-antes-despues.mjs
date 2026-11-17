// Corre el test de "eliminar mascota" con el texto ANTES y con el texto DESPUÉS.
// Uso (desde la carpeta frontend):  node scripts/prueba-antes-despues.mjs
// Restaura siempre el archivo corregido al terminar, aunque algo falle.
import { readFileSync, writeFileSync } from 'node:fs';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const messageFile = join(root, 'src/app/pages/my-pets/delete-pet-message.ts');
const specPath = 'src/app/pages/my-pets/delete-pet-message.spec.ts';
const testCmd = process.env.TEST_CMD ?? `npx ng test --watch=false --include ${specPath}`;

const ANTES = `export function deletePetConfirmMessage(petName: string): string {
  return \`¿Eliminar a \${petName}? Esta acción no se puede deshacer.\`;
}
`;

const despues = readFileSync(messageFile, 'utf8');

function run(label, source) {
  writeFileSync(messageFile, source);
  console.log(`\n================ ${label} ================\n`);
  const result = spawnSync(testCmd, { cwd: root, shell: true, stdio: 'inherit' });
  return result.status === 0;
}

let antesPasa, despuesPasa;
try {
  antesPasa = run('ANTES (texto original)', ANTES);
  despuesPasa = run('DESPUÉS (mensaje corregido)', despues);
} finally {
  writeFileSync(messageFile, despues);
}

console.log('\n================ RESUMEN ================');
console.log(`ANTES:   ${antesPasa ? 'pasa (inesperado)' : 'FALLA (esperado)'}`);
console.log(`DESPUÉS: ${despuesPasa ? 'PASA (esperado)' : 'falla (revisar)'}`);
process.exit(!antesPasa && despuesPasa ? 0 : 1);
