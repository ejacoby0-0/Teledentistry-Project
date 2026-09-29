/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_963258555")

  // add field
  collection.fields.addAt(1, new Field({
    "cascadeDelete": false,
    "collectionId": "pbc_2104570688",
    "help": "",
    "hidden": false,
    "id": "relation3961670826",
    "maxSelect": 0,
    "minSelect": 0,
    "name": "billing",
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
    "id": "text2697101061",
    "max": 0,
    "min": 0,
    "name": "policyNumber",
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
    "id": "select3403245552",
    "maxSelect": 0,
    "name": "claimStatus",
    "presentable": false,
    "required": true,
    "system": false,
    "type": "select",
    "values": [
      "submitted",
      "pending",
      "approved",
      "rejected"
    ]
  }))

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_963258555")

  // remove field
  collection.fields.removeById("relation3961670826")

  // remove field
  collection.fields.removeById("text2697101061")

  // remove field
  collection.fields.removeById("select3403245552")

  return app.save(collection)
})
