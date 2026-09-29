/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_1273517211")

  // update collection data
  unmarshal({
    "listRule": "@request.auth.id != \"\"\n&& dentalRecord.patient.user = @request.auth.id",
    "viewRule": "@request.auth.id != \"\"\n&& dentalRecord.patient.user = @request.auth.id"
  }, collection)

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_1273517211")

  // update collection data
  unmarshal({
    "listRule": null,
    "viewRule": null
  }, collection)

  return app.save(collection)
})
