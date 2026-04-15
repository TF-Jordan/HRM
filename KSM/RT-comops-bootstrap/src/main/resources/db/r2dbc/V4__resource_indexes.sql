CREATE INDEX IF NOT EXISTS idx_resource_material_resource_org_agency
  ON resource.material_resource (tenant_id, organization_id, agency_id);

CREATE INDEX IF NOT EXISTS idx_resource_material_resource_category_status
  ON resource.material_resource (tenant_id, organization_id, category, status);

CREATE INDEX IF NOT EXISTS idx_inventory_stock_movement_org_agency_product
  ON inventory.stock_movement (tenant_id, organization_id, agency_id, product_id);
