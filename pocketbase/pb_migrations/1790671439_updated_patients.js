/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_1820489269")

  // update collection data
  unmarshal({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"patient\"\n&& @request.body.user = @request.auth.id",
    "listRule": "@request.auth.id != \"\"\n&& user = @request.auth.id",
    "updateRule": "@request.auth.id != \"\"\n&& user = @request.auth.id\n&& @request.body.user:changed = false",
    "viewRule": "@request.auth.id != \"\"\n&& user = @request.auth.id"
  }, collection)

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_1820489269")

  // update collection data
  unmarshal({
    "createRule": null,
    "listRule": null,
    "updateRule": null,
    "viewRule": null
  }, collection)

  return app.save(collection)
})
