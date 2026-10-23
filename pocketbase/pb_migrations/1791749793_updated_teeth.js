/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_1273517211")

  // update collection data
  unmarshal({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& @request.body.dentalRecord.patient.appointments_via_patient.practitioner.user = @request.auth.id",
    "listRule": "@request.auth.id != \"\"\n&& (\n    dentalRecord.patient.user = @request.auth.id\n    ||\n    (\n        @request.auth.role = \"practitioner\"\n        && dentalRecord.patient.appointments_via_patient.practitioner.user = @request.auth.id\n    )\n)",
    "updateRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& dentalRecord.patient.appointments_via_patient.practitioner.user = @request.auth.id\n&& @request.body.dentalRecord:changed = false",
    "viewRule": "@request.auth.id != \"\"\n&& (\n    dentalRecord.patient.user = @request.auth.id\n    ||\n    (\n        @request.auth.role = \"practitioner\"\n        && dentalRecord.patient.appointments_via_patient.practitioner.user = @request.auth.id\n    )\n)"
  }, collection)

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_1273517211")

  // update collection data
  unmarshal({
    "createRule": null,
    "listRule": "@request.auth.id != \"\"\n&& dentalRecord.patient.user = @request.auth.id",
    "updateRule": null,
    "viewRule": "@request.auth.id != \"\"\n&& dentalRecord.patient.user = @request.auth.id"
  }, collection)

  return app.save(collection)
})
