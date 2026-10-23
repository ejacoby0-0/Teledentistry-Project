/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_1037645436");

  return app.delete(collection);
}, (app) => {
  const collection = new Collection({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"patient\"\n&& @request.body.patient.user = @request.auth.id",
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
      },
      {
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
      },
      {
        "help": "",
        "hidden": false,
        "id": "date2482226890",
        "max": "",
        "min": "",
        "name": "dateTime",
        "presentable": false,
        "required": true,
        "system": false,
        "type": "date"
      },
      {
        "autogeneratePattern": "",
        "help": "",
        "hidden": false,
        "id": "text2393256231",
        "max": 0,
        "min": 0,
        "name": "startTime",
        "pattern": "",
        "presentable": false,
        "primaryKey": false,
        "required": true,
        "system": false,
        "type": "text"
      },
      {
        "autogeneratePattern": "",
        "help": "",
        "hidden": false,
        "id": "text4098681852",
        "max": 0,
        "min": 0,
        "name": "endTime",
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
        "id": "select2063623452",
        "maxSelect": 0,
        "name": "status",
        "presentable": false,
        "required": true,
        "system": false,
        "type": "select",
        "values": [
          "pending",
          "confirmed",
          "completed",
          "cancelled"
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
    "id": "pbc_1037645436",
    "indexes": [],
    "listRule": "@request.auth.id != \"\"\n&& (\n    patient.user = @request.auth.id\n    ||\n    practitioner.user = @request.auth.id\n)",
    "name": "appointments",
    "system": false,
    "type": "base",
    "updateRule": "@request.auth.id != \"\"\n&& (\n    patient.user = @request.auth.id\n    ||\n    practitioner.user = @request.auth.id\n)\n&& @request.body.patient:changed = false\n&& @request.body.practitioner:changed = false",
    "viewRule": "@request.auth.id != \"\"\n&& (\n    patient.user = @request.auth.id\n    ||\n    practitioner.user = @request.auth.id\n)"
  });

  return app.save(collection);
})
