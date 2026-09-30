/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_827889277")

  // update collection data
  unmarshal({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& @request.body.practitioner.user = @request.auth.id",
    "listRule": "@request.auth.id != \"\"\n&& (\n    patient.user = @request.auth.id\n    ||\n    practitioner.user = @request.auth.id\n)",
    "updateRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& practitioner.user = @request.auth.id\n&& @request.body.patient:changed = false\n&& @request.body.practitioner:changed = false",
    "viewRule": "@request.auth.id != \"\"\n&& (\n    patient.user = @request.auth.id\n    ||\n    practitioner.user = @request.auth.id\n)"
  }, collection)

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_827889277")

  // update collection data
  unmarshal({
    "createRule": null,
    "listRule": null,
    "updateRule": null,
    "viewRule": null
  }, collection)

  return app.save(collection)
})
