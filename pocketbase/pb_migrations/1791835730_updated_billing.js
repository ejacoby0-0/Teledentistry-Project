/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_2104570688")

  // update collection data
  unmarshal({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& @request.body.appointment.practitioner.user = @request.auth.id",
    "updateRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& appointment.practitioner.user = @request.auth.id\n&& @request.body.appointment:changed = false"
  }, collection)

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_2104570688")

  // update collection data
  unmarshal({
    "createRule": null,
    "updateRule": null
  }, collection)

  return app.save(collection)
})
