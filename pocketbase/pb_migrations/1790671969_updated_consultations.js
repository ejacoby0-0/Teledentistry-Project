/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_3441013282")

  // update collection data
  unmarshal({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& @request.body.appointment.practitioner.user = @request.auth.id",
    "listRule": "@request.auth.id != \"\"\n&& (\n    appointment.patient.user = @request.auth.id\n    ||\n    appointment.practitioner.user = @request.auth.id\n)",
    "updateRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& appointment.practitioner.user = @request.auth.id\n&& @request.body.appointment:changed = false",
    "viewRule": "@request.auth.id != \"\"\n&& (\n    appointment.patient.user = @request.auth.id\n    ||\n    appointment.practitioner.user = @request.auth.id\n)"
  }, collection)

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_3441013282")

  // update collection data
  unmarshal({
    "createRule": null,
    "listRule": null,
    "updateRule": null,
    "viewRule": null
  }, collection)

  return app.save(collection)
})
