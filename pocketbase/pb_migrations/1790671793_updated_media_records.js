/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_343860094")

  // update collection data
  unmarshal({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"patient\"\n&& @request.body.patient.user = @request.auth.id",
    "listRule": "@request.auth.id != \"\"\n&& patient.user = @request.auth.id",
    "updateRule": "@request.auth.id != \"\"\n&& patient.user = @request.auth.id\n&& @request.body.patient:changed = false",
    "viewRule": "@request.auth.id != \"\"\n&& patient.user = @request.auth.id"
  }, collection)

  // update field
  collection.fields.addAt(3, new Field({
    "help": "",
    "hidden": false,
    "id": "file2359244304",
    "maxSelect": 0,
    "maxSize": 0,
    "mimeTypes": [],
    "name": "file",
    "presentable": false,
    "protected": true,
    "required": true,
    "system": false,
    "thumbs": [],
    "type": "file"
  }))

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_343860094")

  // update collection data
  unmarshal({
    "createRule": null,
    "listRule": null,
    "updateRule": null,
    "viewRule": null
  }, collection)

  // update field
  collection.fields.addAt(3, new Field({
    "help": "",
    "hidden": false,
    "id": "file2359244304",
    "maxSelect": 0,
    "maxSize": 0,
    "mimeTypes": [],
    "name": "file",
    "presentable": false,
    "protected": false,
    "required": true,
    "system": false,
    "thumbs": [],
    "type": "file"
  }))

  return app.save(collection)
})
