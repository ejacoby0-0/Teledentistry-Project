/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_2178835042");

  return app.delete(collection);
}, (app) => {
  const collection = new Collection({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"patient\"\n&& @request.body.consultation.appointment.patient.user = @request.auth.id",
    "deleteRule": null,
    "fields": [
      {
        "autogeneratePattern": "[a-z0-9]{15}",
        "help": "",
        "hidden": false,
        "id": "text3208210256",
        "max": 15,
        "min": 15,
        "name": "id",
        "pattern": "^[a-z0-9]+$",
        "presentable": false,
        "primaryKey": true,
        "required": true,
        "system": true,
        "type": "text"
      },
      {
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
      },
      {
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
      },
      {
        "help": "",
        "hidden": false,
        "id": "file3309110367",
        "maxSelect": 0,
        "maxSize": 0,
        "mimeTypes": [],
        "name": "image",
        "presentable": false,
        "protected": true,
        "required": false,
        "system": false,
        "thumbs": [],
        "type": "file"
      },
      {
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
      },
      {
        "hidden": false,
        "id": "autodate2990389176",
        "name": "created",
        "onCreate": true,
        "onUpdate": false,
        "presentable": false,
        "system": false,
        "type": "autodate"
      },
      {
        "hidden": false,
        "id": "autodate3332085495",
        "name": "updated",
        "onCreate": true,
        "onUpdate": true,
        "presentable": false,
        "system": false,
        "type": "autodate"
      }
    ],
    "id": "pbc_2178835042",
    "indexes": [],
    "listRule": "@request.auth.id != \"\"\n&& (\n    consultation.appointment.patient.user = @request.auth.id\n    ||\n    consultation.appointment.practitioner.user = @request.auth.id\n)",
    "name": "store_and_forward",
    "system": false,
    "type": "base",
    "updateRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"patient\"\n&& consultation.appointment.patient.user = @request.auth.id\n&& @request.body.consultation:changed = false",
    "viewRule": "@request.auth.id != \"\"\n&& (\n    consultation.appointment.patient.user = @request.auth.id\n    ||\n    consultation.appointment.practitioner.user = @request.auth.id\n)"
  });

  return app.save(collection);
})
