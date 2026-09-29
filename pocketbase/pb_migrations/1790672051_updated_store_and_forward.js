/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_2178835042")

  // update collection data
  unmarshal({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"patient\"\n&& @request.body.consultation.appointment.patient.user = @request.auth.id",
    "listRule": "@request.auth.id != \"\"\n&& (\n    consultation.appointment.patient.user = @request.auth.id\n    ||\n    consultation.appointment.practitioner.user = @request.auth.id\n)",
    "updateRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"patient\"\n&& consultation.appointment.patient.user = @request.auth.id\n&& @request.body.consultation:changed = false",
    "viewRule": "@request.auth.id != \"\"\n&& (\n    consultation.appointment.patient.user = @request.auth.id\n    ||\n    consultation.appointment.practitioner.user = @request.auth.id\n)"
  }, collection)

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_2178835042")

  // update collection data
  unmarshal({
    "createRule": null,
    "listRule": null,
    "updateRule": null,
    "viewRule": null
  }, collection)

  return app.save(collection)
})
