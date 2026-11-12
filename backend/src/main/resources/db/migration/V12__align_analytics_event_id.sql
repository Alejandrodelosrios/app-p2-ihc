ALTER TABLE eventos
  ALTER COLUMN id TYPE UUID
  USING md5(id::text)::uuid;
