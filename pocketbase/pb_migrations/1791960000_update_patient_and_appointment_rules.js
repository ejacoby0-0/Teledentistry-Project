/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  // Update patients collection rules
  const patientsCollection = app.findCollectionByNameOrId("pbc_1820489269");
  unmarshal({
    "createRule": "@request.auth.id != \"\"",
    "listRule": "@request.auth.id != \"\"",
    "viewRule": "@request.auth.id != \"\"",
    "updateRule": "@request.auth.id != \"\""
  }, patientsCollection);
  app.save(patientsCollection);

  // Update appointments collection rules
  const appointmentsCollection = app.findCollectionByNameOrId("pbc_1037645436");
  unmarshal({
    "createRule": "@request.auth.id != \"\" && @request.auth.role = \"patient\"",
    "listRule": "@request.auth.id != \"\"",
    "viewRule": "@request.auth.id != \"\"",
    "updateRule": "@request.auth.id != \"\""
  }, appointmentsCollection);
  app.save(appointmentsCollection);
}, (app) => {
  // rollback
})
