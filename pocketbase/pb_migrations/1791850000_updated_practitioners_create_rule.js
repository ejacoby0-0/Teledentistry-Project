/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_821706721");

  // Allow authenticated practitioner users to create their own profile record
  unmarshal({
    "createRule": "@request.auth.id != \"\" && @request.auth.role = \"practitioner\" && @request.body.user = @request.auth.id",
    "listRule": "@request.auth.id != \"\"",
    "viewRule": "@request.auth.id != \"\""
  }, collection);

  return app.save(collection);
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_821706721");

  unmarshal({
    "createRule": null
  }, collection);

  return app.save(collection);
})
