/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_2104570688")

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
    "help": "",
    "hidden": false,
    "id": "number2392944706",
    "max": null,
    "min": null,
    "name": "amount",
    "onlyInt": false,
    "presentable": false,
    "required": true,
    "system": false,
    "type": "number"
  }))

  // add field
  collection.fields.addAt(3, new Field({
    "help": "",
    "hidden": false,
    "id": "number893998565",
    "max": null,
    "min": null,
    "name": "insuranceCoverage",
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
    "id": "select2715662852",
    "maxSelect": 0,
    "name": "paymentStatus",
    "presentable": false,
    "required": true,
    "system": false,
    "type": "select",
    "values": [
      "pending",
      "paid",
      "partially_paid",
      "failed",
      "refunded"
    ]
  }))

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_2104570688")

  // remove field
  collection.fields.removeById("relation4265146436")

  // remove field
  collection.fields.removeById("number2392944706")

  // remove field
  collection.fields.removeById("number893998565")

  // remove field
  collection.fields.removeById("select2715662852")

  return app.save(collection)
})
