/// <reference path="../pb_data/types.d.ts" />
// Adds appointments.matrixRoomId. Hidden so it is never exposed/writable via the
// regular records API; it is only managed by pb_hooks/matrix.pb.js.
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_1037645436")

  collection.fields.add(new Field({
    "autogeneratePattern": "",
    "help": "",
    "hidden": true,
    "id": "text_matrix_room_id",
    "max": 0,
    "min": 0,
    "name": "matrixRoomId",
    "pattern": "",
    "presentable": false,
    "primaryKey": false,
    "required": false,
    "system": false,
    "type": "text"
  }))

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_1037645436")

  collection.fields.removeById("text_matrix_room_id")

  return app.save(collection)
})
