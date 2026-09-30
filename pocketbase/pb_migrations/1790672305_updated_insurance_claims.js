/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_963258555")

  // update collection data
  unmarshal({
    "listRule": "@request.auth.id != \"\"\n&& (\n    billing.appointment.patient.user = @request.auth.id\n    ||\n    billing.appointment.practitioner.user = @request.auth.id\n)",
    "viewRule": "@request.auth.id != \"\"\n&& (\n    billing.appointment.patient.user = @request.auth.id\n    ||\n    billing.appointment.practitioner.user = @request.auth.id\n)"
  }, collection)

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_963258555")

  // update collection data
  unmarshal({
    "listRule": null,
    "viewRule": null
  }, collection)

  return app.save(collection)
})
