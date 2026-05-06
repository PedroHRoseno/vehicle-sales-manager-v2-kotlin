-- Campos e tabelas para catálogo público
-- - vehicles.published: controla exibição no catálogo (default false)
-- - vehicles.description: texto opcional
-- - vehicle_images: URLs de fotos por veículo

-- published: criar (nullable), preencher existentes, depois setar default + NOT NULL
ALTER TABLE vehicles
  ADD COLUMN IF NOT EXISTS published boolean;

UPDATE vehicles
SET published = false
WHERE published IS NULL;

ALTER TABLE vehicles
  ALTER COLUMN published SET DEFAULT false;

ALTER TABLE vehicles
  ALTER COLUMN published SET NOT NULL;

ALTER TABLE vehicles
  ADD COLUMN IF NOT EXISTS description varchar(1000);

CREATE TABLE IF NOT EXISTS vehicle_images (
  vehicle_license_plate varchar(255) NOT NULL,
  image_url varchar(1000) NOT NULL
);

-- Garantir integridade com FK (se já existir, não recria)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE constraint_type = 'FOREIGN KEY'
      AND table_name = 'vehicle_images'
      AND constraint_name = 'fk_vehicle_images_vehicle'
  ) THEN
    ALTER TABLE vehicle_images
      ADD CONSTRAINT fk_vehicle_images_vehicle
      FOREIGN KEY (vehicle_license_plate)
      REFERENCES vehicles (license_plate)
      ON DELETE CASCADE;
  END IF;
END $$;

