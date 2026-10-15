/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_963258555")

  // update collection data
  unmarshal({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& @request.body.billing.appointment.practitioner.user = @request.auth.id",
    "updateRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& billing.appointment.practitioner.user = @request.auth.id\n&& @request.body.billing:changed = false"
  }, collection)

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_963258555")

  // update collection data
  unmarshal({
    "createRule": null,
    "updateRule": null
  }, collection)

  return app.save(collection)
})
