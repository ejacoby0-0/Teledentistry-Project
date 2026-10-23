/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_2711770659")

  // update collection data
  unmarshal({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& @request.body.practitioner.user = @request.auth.id\n&& @request.body.availability.practitioner.user = @request.auth.id"
  }, collection)

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_2711770659")

  // update collection data
  unmarshal({
    "createRule": null
  }, collection)

  return app.save(collection)
})
