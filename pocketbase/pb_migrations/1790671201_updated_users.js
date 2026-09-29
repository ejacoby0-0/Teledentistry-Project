/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("_pb_users_auth_")

  // update collection data
  unmarshal({
    "createRule": "@request.body.role = \"patient\"",
    "updateRule": "@request.auth.id != \"\"\n&& id = @request.auth.id\n&& @request.body.role:changed = false"
  }, collection)

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("_pb_users_auth_")

  // update collection data
  unmarshal({
    "createRule": "",
    "updateRule": "@request.auth.id != \"\" && id = @request.auth.id"
  }, collection)

  return app.save(collection)
})
