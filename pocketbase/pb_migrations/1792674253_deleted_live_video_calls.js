/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_690178321");

  return app.delete(collection);
}, (app) => {
  const collection = new Collection({
    "createRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& @request.body.consultation.appointment.practitioner.user = @request.auth.id",
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
        "id": "text94567950",
        "max": 0,
        "min": 0,
        "name": "sessionToken",
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
        "id": "text2975308976",
        "max": 0,
        "min": 0,
        "name": "agoraChannelID",
        "pattern": "",
        "presentable": false,
        "primaryKey": false,
        "required": false,
        "system": false,
        "type": "text"
      },
      {
        "help": "",
        "hidden": false,
        "id": "number3737426654",
        "max": null,
        "min": null,
        "name": "callDuration",
        "onlyInt": false,
        "presentable": false,
        "required": false,
        "system": false,
        "type": "number"
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
    "id": "pbc_690178321",
    "indexes": [],
    "listRule": "@request.auth.id != \"\"\n&& (\n    consultation.appointment.patient.user = @request.auth.id\n    ||\n    consultation.appointment.practitioner.user = @request.auth.id\n)",
    "name": "live_video_calls",
    "system": false,
    "type": "base",
    "updateRule": "@request.auth.id != \"\"\n&& @request.auth.role = \"practitioner\"\n&& consultation.appointment.practitioner.user = @request.auth.id\n&& @request.body.consultation:changed = false",
    "viewRule": "@request.auth.id != \"\"\n&& (\n    consultation.appointment.patient.user = @request.auth.id\n    ||\n    consultation.appointment.practitioner.user = @request.auth.id\n)"
  });

  return app.save(collection);
})
