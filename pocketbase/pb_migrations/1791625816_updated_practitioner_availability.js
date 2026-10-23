/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_1468955758")

  // update collection data
  unmarshal({
    "createRule": "@request.auth.role = \"practitioner\" &&\n@request.body.practitioner.user = @request.auth.id",
    "listRule": "@request.auth.id != \"\"",
    "updateRule": "@request.auth.role = \"practitioner\" &&\npractitioner.user = @request.auth.id &&\n@request.body.practitioner:changed = false",
    "viewRule": "@request.auth.id != \"\""
  }, collection)

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_1468955758")

  // update collection data
  unmarshal({
    "createRule": null,
    "listRule": null,
    "updateRule": null,
    "viewRule": null
  }, collection)

  return app.save(collection)
})
