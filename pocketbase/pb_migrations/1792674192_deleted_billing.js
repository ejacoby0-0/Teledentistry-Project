/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_2104570688");

  return app.delete(collection);
}, (app) => {
  const collection = new Collection({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& @request.body.appointment.practitioner.user = @request.auth.id",
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
      },
      {
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
      },
      {
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
      },
      {
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
    "id": "pbc_2104570688",
    "indexes": [],
    "listRule": "@request.auth.id != \"\"\n&& (\n    appointment.patient.user = @request.auth.id\n    ||\n    appointment.practitioner.user = @request.auth.id\n)",
    "name": "billing",
    "system": false,
    "type": "base",
    "updateRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& appointment.practitioner.user = @request.auth.id\n&& @request.body.appointment:changed = false",
    "viewRule": "@request.auth.id != \"\"\n&& (\n    appointment.patient.user = @request.auth.id\n    ||\n    appointment.practitioner.user = @request.auth.id\n)"
  });

  return app.save(collection);
})
