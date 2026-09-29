/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_1273517211")

  // add field
  collection.fields.addAt(1, new Field({
    "cascadeDelete": false,
    "collectionId": "pbc_4141908196",
    "help": "",
    "hidden": false,
    "id": "relation1852747976",
    "maxSelect": 0,
    "minSelect": 0,
    "name": "dentalRecord",
    "presentable": false,
    "required": true,
    "system": false,
    "type": "relation"
  }))

  // add field
  collection.fields.addAt(2, new Field({
    "help": "",
    "hidden": false,
    "id": "number922199868",
    "max": null,
    "min": null,
    "name": "toothNumber",
    "onlyInt": false,
    "presentable": false,
    "required": true,
    "system": false,
    "type": "number"
  }))

  // add field
  collection.fields.addAt(3, new Field({
    "autogeneratePattern": "",
    "help": "",
    "hidden": false,
    "id": "text3184953411",
    "max": 0,
    "min": 0,
    "name": "condition",
    "pattern": "",
    "presentable": false,
    "primaryKey": false,
    "required": false,
    "system": false,
    "type": "text"
  }))

  // add field
  collection.fields.addAt(4, new Field({
    "autogeneratePattern": "",
    "help": "",
    "hidden": false,
    "id": "text3493838876",
    "max": 0,
    "min": 0,
    "name": "treatmentHistory",
    "pattern": "",
    "presentable": false,
    "primaryKey": false,
    "required": false,
    "system": false,
    "type": "text"
  }))

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_1273517211")

  // remove field
  collection.fields.removeById("relation1852747976")

  // remove field
  collection.fields.removeById("number922199868")

  // remove field
  collection.fields.removeById("text3184953411")

  // remove field
  collection.fields.removeById("text3493838876")

  return app.save(collection)
})
