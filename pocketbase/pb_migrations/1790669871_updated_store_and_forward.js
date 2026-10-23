/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_2178835042")

  // add field
  collection.fields.addAt(1, new Field({
    "cascadeDelete": false,
    "collectionId": "pbc_3441013282",
    "help": "",
    "hidden": false,
    "id": "relation2521204134",
    "maxSelect": 0,
    "minSelect": 0,
    "name": "consultation",
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
    "id": "text627742133",
    "max": 0,
    "min": 0,
    "name": "symptomDetails",
    "pattern": "",
    "presentable": false,
    "primaryKey": false,
    "required": true,
    "system": false,
    "type": "text"
  }))

  // add field
  collection.fields.addAt(3, new Field({
    "help": "",
    "hidden": false,
    "id": "file3309110367",
    "maxSelect": 0,
    "maxSize": 0,
    "mimeTypes": [],
    "name": "image",
    "presentable": false,
    "protected": false,
    "required": false,
    "system": false,
    "thumbs": [],
    "type": "file"
  }))

  // add field
  collection.fields.addAt(4, new Field({
    "help": "",
    "hidden": false,
    "id": "select2959125098",
    "maxSelect": 0,
    "name": "triageLevel",
    "presentable": false,
    "required": false,
    "system": false,
    "type": "select",
    "values": [
      "low",
      "medium",
      "high",
      "urgent"
    ]
  }))

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_2178835042")

  // remove field
  collection.fields.removeById("relation2521204134")

  // remove field
  collection.fields.removeById("text627742133")

  // remove field
  collection.fields.removeById("file3309110367")

  // remove field
  collection.fields.removeById("select2959125098")

  return app.save(collection)
})
