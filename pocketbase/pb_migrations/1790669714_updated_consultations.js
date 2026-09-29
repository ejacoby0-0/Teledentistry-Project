/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_3441013282")

  // add field
  collection.fields.addAt(1, new Field({
    "cascadeDelete": false,
    "collectionId": "pbc_1037645436",
    "help": "",
    "hidden": false,
    "id": "relation4265146436",
    "maxSelect": 0,
    "minSelect": 0,
    "name": "appointment",
    "presentable": false,
    "required": true,
    "system": false,
    "type": "relation"
  }))

  // add field
  collection.fields.addAt(2, new Field({
    "autogeneratePattern": "",
    "help": "",
    "hidden": false,
    "id": "text4244631190",
    "max": 0,
    "min": 0,
    "name": "diagnosisNotes",
    "pattern": "",
    "presentable": false,
    "primaryKey": false,
    "required": false,
    "system": false,
    "type": "text"
  }))

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_3441013282")

  // remove field
  collection.fields.removeById("relation4265146436")

  // remove field
  collection.fields.removeById("text4244631190")

  return app.save(collection)
})
