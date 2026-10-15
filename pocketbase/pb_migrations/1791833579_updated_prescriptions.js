/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_220866482")

  // update collection data
  unmarshal({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& @request.body.practitioner.user = @request.auth.id\n&& @request.body.patient.appointments_via_patient.practitioner.user = @request.auth.id",
    "listRule": "@request.auth.id != \"\"\n&& (\n    patient.user = @request.auth.id\n    ||\n    (\n        @request.auth.role = \"practitioner\"\n        && patient.appointments_via_patient.practitioner.user = @request.auth.id\n    )\n)",
    "viewRule": "@request.auth.id != \"\"\n&& (\n    patient.user = @request.auth.id\n    ||\n    (\n        @request.auth.role = \"practitioner\"\n        && patient.appointments_via_patient.practitioner.user = @request.auth.id\n    )\n)"
  }, collection)

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_220866482")

  // update collection data
  unmarshal({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& @request.body.practitioner.user = @request.auth.id",
    "listRule": "@request.auth.id != \"\"\n&& (\n    patient.user = @request.auth.id\n    ||\n    practitioner.user = @request.auth.id\n)",
    "viewRule": "@request.auth.id != \"\"\n&& (\n    patient.user = @request.auth.id\n    ||\n    practitioner.user = @request.auth.id\n)"
  }, collection)

  return app.save(collection)
})
