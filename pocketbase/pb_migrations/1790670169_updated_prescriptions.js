/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_220866482")

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

  // add field
  collection.fields.addAt(2, new Field({
    "cascadeDelete": false,
    "collectionId": "pbc_821706721",
    "help": "",
    "hidden": false,
    "id": "relation389168316",
    "maxSelect": 0,
    "minSelect": 0,
    "name": "practitioner",
    "presentable": false,
    "required": false,
    "system": false,
    "type": "relation"
  }))

  // add field
  collection.fields.addAt(3, new Field({
    "autogeneratePattern": "",
    "help": "",
    "hidden": false,
    "id": "text1525570416",
    "max": 0,
    "min": 0,
    "name": "medication",
    "pattern": "",
    "presentable": false,
    "primaryKey": false,
    "required": true,
    "system": false,
    "type": "text"
  }))

  // add field
  collection.fields.addAt(4, new Field({
    "autogeneratePattern": "",
    "help": "",
    "hidden": false,
    "id": "text507431585",
    "max": 0,
    "min": 0,
    "name": "dosage",
    "pattern": "",
    "presentable": false,
    "primaryKey": false,
    "required": true,
    "system": false,
    "type": "text"
  }))

  // add field
  collection.fields.addAt(5, new Field({
    "help": "",
    "hidden": false,
    "id": "date3359857795",
    "max": "",
    "min": "",
    "name": "issueDate",
    "presentable": false,
    "required": true,
    "system": false,
    "type": "date"
  }))

  // add field
  collection.fields.addAt(6, new Field({
    "help": "",
    "hidden": false,
    "id": "date3028367109",
    "max": "",
    "min": "",
    "name": "expiryDate",
    "presentable": false,
    "required": false,
    "system": false,
    "type": "date"
  }))

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_220866482")

  // remove field
  collection.fields.removeById("relation450549739")

  // remove field
  collection.fields.removeById("relation389168316")

  // remove field
  collection.fields.removeById("text1525570416")

  // remove field
  collection.fields.removeById("text507431585")

  // remove field
  collection.fields.removeById("date3359857795")

  // remove field
  collection.fields.removeById("date3028367109")

  return app.save(collection)
})
