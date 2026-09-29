/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_343860094")

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
    "help": "",
    "hidden": false,
    "id": "select1531004670",
    "maxSelect": 0,
    "name": "uploadType",
    "presentable": false,
    "required": true,
    "system": false,
    "type": "select",
    "values": [
      "dental_image",
      "document",
      "other"
    ]
  }))

  // add field
  collection.fields.addAt(3, new Field({
    "help": "",
    "hidden": false,
    "id": "file2359244304",
    "maxSelect": 0,
    "maxSize": 0,
    "mimeTypes": [],
    "name": "file",
    "presentable": false,
    "protected": false,
    "required": true,
    "system": false,
    "thumbs": [],
    "type": "file"
  }))

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_343860094")

  // remove field
  collection.fields.removeById("relation450549739")

  // remove field
  collection.fields.removeById("select1531004670")

  // remove field
  collection.fields.removeById("file2359244304")

  return app.save(collection)
})
