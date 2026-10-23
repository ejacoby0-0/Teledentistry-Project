/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_1037645436");

  unmarshal({
    "createRule": "@request.auth.id != \"\" && @request.auth.role = \"patient\""
  }, collection);

  return app.save(collection);
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_1037645436");

  unmarshal({
    "createRule": "@request.auth.id != \"\" && @request.auth.role = \"patient\" && @request.body.patient.user = @request.auth.id"
  }, collection);

  return app.save(collection);
})
