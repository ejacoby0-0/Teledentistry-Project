/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_963258555");

  return app.delete(collection);
}, (app) => {
  const collection = new Collection({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& @request.body.billing.appointment.practitioner.user = @request.auth.id",
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
        "collectionId": "pbc_2104570688",
        "help": "",
        "hidden": false,
        "id": "relation3961670826",
        "maxSelect": 0,
        "minSelect": 0,
        "name": "billing",
        "presentable": false,
        "required": true,
        "system": false,
        "type": "relation"
      },
      {
        "autogeneratePattern": "",
        "help": "",
        "hidden": false,
        "id": "text2697101061",
        "max": 0,
        "min": 0,
        "name": "policyNumber",
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
        "id": "select3403245552",
        "maxSelect": 0,
        "name": "claimStatus",
        "presentable": false,
        "required": true,
        "system": false,
        "type": "select",
        "values": [
          "submitted",
          "pending",
          "approved",
          "rejected"
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
    "id": "pbc_963258555",
    "indexes": [],
    "listRule": "@request.auth.id != \"\"\n&& (\n    billing.appointment.patient.user = @request.auth.id\n    ||\n    billing.appointment.practitioner.user = @request.auth.id\n)",
    "name": "insurance_claims",
    "system": false,
    "type": "base",
    "updateRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& billing.appointment.practitioner.user = @request.auth.id\n&& @request.body.billing:changed = false",
    "viewRule": "@request.auth.id != \"\"\n&& (\n    billing.appointment.patient.user = @request.auth.id\n    ||\n    billing.appointment.practitioner.user = @request.auth.id\n)"
  });

  return app.save(collection);
})
