/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_4141908196")

  // add field
  collection.fields.addAt(1, new Field({
    "cascadeDelete": false,
    "collectionId": "pbc_1820489269",
    "help": "",
    "hidden": false,
    "id": "relation450549739",
    "maxSelect": 0,
    "minSelect": 0,
    "name": "patient",
    "presentable": false,
    "required": true,
    "system": false,
    "type": "relation"
  }))

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_4141908196")

  // remove field
  collection.fields.removeById("relation450549739")

  return app.save(collection)
})
