/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_1273517211");

  return app.delete(collection);
}, (app) => {
  const collection = new Collection({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& @request.body.dentalRecord.patient.appointments_via_patient.practitioner.user = @request.auth.id",
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
        "collectionId": "pbc_4141908196",
        "help": "",
        "hidden": false,
        "id": "relation1852747976",
        "maxSelect": 0,
        "minSelect": 0,
        "name": "dentalRecord",
        "presentable": false,
        "required": true,
        "system": false,
        "type": "relation"
      },
      {
        "help": "",
        "hidden": false,
        "id": "number922199868",
        "max": null,
        "min": null,
        "name": "toothNumber",
        "onlyInt": false,
        "presentable": false,
        "required": true,
        "system": false,
        "type": "number"
      },
      {
        "autogeneratePattern": "",
        "help": "",
        "hidden": false,
        "id": "text3184953411",
        "max": 0,
        "min": 0,
        "name": "condition",
        "pattern": "",
        "presentable": false,
        "primaryKey": false,
        "required": false,
        "system": false,
        "type": "text"
      },
      {
        "autogeneratePattern": "",
        "help": "",
        "hidden": false,
        "id": "text3493838876",
        "max": 0,
        "min": 0,
        "name": "treatmentHistory",
        "pattern": "",
        "presentable": false,
        "primaryKey": false,
        "required": false,
        "system": false,
        "type": "text"
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
    "id": "pbc_1273517211",
    "indexes": [],
    "listRule": "@request.auth.id != \"\"\n&& (\n    dentalRecord.patient.user = @request.auth.id\n    ||\n    (\n        @request.auth.role = \"practitioner\"\n        && dentalRecord.patient.appointments_via_patient.practitioner.user = @request.auth.id\n    )\n)",
    "name": "teeth",
    "system": false,
    "type": "base",
    "updateRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& dentalRecord.patient.appointments_via_patient.practitioner.user = @request.auth.id\n&& @request.body.dentalRecord:changed = false",
    "viewRule": "@request.auth.id != \"\"\n&& (\n    dentalRecord.patient.user = @request.auth.id\n    ||\n    (\n        @request.auth.role = \"practitioner\"\n        && dentalRecord.patient.appointments_via_patient.practitioner.user = @request.auth.id\n    )\n)"
  });

  return app.save(collection);
})
