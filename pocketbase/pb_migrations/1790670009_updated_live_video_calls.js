/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_690178321")

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
    "id": "text94567950",
    "max": 0,
    "min": 0,
    "name": "sessionToken",
    "pattern": "",
    "presentable": false,
    "primaryKey": false,
    "required": false,
    "system": false,
    "type": "text"
  }))

  // add field
  collection.fields.addAt(3, new Field({
    "autogeneratePattern": "",
    "help": "",
    "hidden": false,
    "id": "text2975308976",
    "max": 0,
    "min": 0,
    "name": "agoraChannelID",
    "pattern": "",
    "presentable": false,
    "primaryKey": false,
    "required": false,
    "system": false,
    "type": "text"
  }))

  // add field
  collection.fields.addAt(4, new Field({
    "help": "",
    "hidden": false,
    "id": "number3737426654",
    "max": null,
    "min": null,
    "name": "callDuration",
    "onlyInt": false,
    "presentable": false,
    "required": false,
    "system": false,
    "type": "number"
  }))

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_690178321")

  // remove field
  collection.fields.removeById("relation2521204134")

  // remove field
  collection.fields.removeById("text94567950")

  // remove field
  collection.fields.removeById("text2975308976")

  // remove field
  collection.fields.removeById("number3737426654")

  return app.save(collection)
})
