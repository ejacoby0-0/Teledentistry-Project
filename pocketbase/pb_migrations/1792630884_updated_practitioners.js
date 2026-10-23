/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_821706721")

  // remove field
  collection.fields.removeById("text2670745717")

  // remove field
  collection.fields.removeById("text2665083498")

  // remove field
  collection.fields.removeById("select843438578")

  // remove field
  collection.fields.removeById("text827839120")

  // remove field
  collection.fields.removeById("text3898508260")

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_821706721")

  // add field
  collection.fields.addAt(2, new Field({
    "autogeneratePattern": "",
    "help": "",
    "hidden": false,
    "id": "text2670745717",
    "max": 0,
    "min": 0,
    "name": "licenseNumber",
    "pattern": "",
    "presentable": false,
    "primaryKey": false,
    "required": true,
    "system": false,
    "type": "text"
  }))

  // add field
  collection.fields.addAt(3, new Field({
    "autogeneratePattern": "",
    "help": "",
    "hidden": false,
    "id": "text2665083498",
    "max": 0,
    "min": 0,
    "name": "specialization",
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
    "id": "select843438578",
    "maxSelect": 0,
    "name": "availabilityStatus",
    "presentable": false,
    "required": true,
    "system": false,
    "type": "select",
    "values": [
      "available",
      "unavailable"
    ]
  }))

  // add field
  collection.fields.addAt(5, new Field({
    "autogeneratePattern": "",
    "help": "",
    "hidden": false,
    "id": "text827839120",
    "max": 0,
    "min": 0,
    "name": "fullName",
    "pattern": "",
    "presentable": false,
    "primaryKey": false,
    "required": true,
    "system": false,
    "type": "text"
  }))

  // add field
  collection.fields.addAt(6, new Field({
    "autogeneratePattern": "",
    "help": "",
    "hidden": false,
    "id": "text3898508260",
    "max": 0,
    "min": 0,
    "name": "phoneNumber",
    "pattern": "",
    "presentable": false,
    "primaryKey": false,
    "required": false,
    "system": false,
    "type": "text"
  }))

  return app.save(collection)
})
