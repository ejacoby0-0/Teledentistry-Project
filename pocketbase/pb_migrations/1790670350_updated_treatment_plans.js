/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_827889277")

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
    "required": true,
    "system": false,
    "type": "relation"
  }))

  // add field
  collection.fields.addAt(3, new Field({
    "help": "",
    "hidden": false,
    "id": "number1552966197",
    "max": null,
    "min": null,
    "name": "totalCost",
    "onlyInt": false,
    "presentable": false,
    "required": false,
    "system": false,
    "type": "number"
  }))

  // add field
  collection.fields.addAt(4, new Field({
    "help": "",
    "hidden": false,
    "id": "date1269603864",
    "max": "",
    "min": "",
    "name": "startDate",
    "presentable": false,
    "required": true,
    "system": false,
    "type": "date"
  }))

  // add field
  collection.fields.addAt(5, new Field({
    "help": "",
    "hidden": false,
    "id": "select2063623452",
    "maxSelect": 0,
    "name": "status",
    "presentable": false,
    "required": true,
    "system": false,
    "type": "select",
    "values": [
      "planned",
      "active",
      "completed",
      "cancelled"
    ]
  }))

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_827889277")

  // remove field
  collection.fields.removeById("relation450549739")

  // remove field
  collection.fields.removeById("relation389168316")

  // remove field
  collection.fields.removeById("number1552966197")

  // remove field
  collection.fields.removeById("date1269603864")

  // remove field
  collection.fields.removeById("select2063623452")

  return app.save(collection)
})
