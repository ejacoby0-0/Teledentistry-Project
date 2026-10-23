/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_1037645436");

  // Remove strict field requirements so appointment creation succeeds seamlessly
  for (const field of collection.fields) {
    if (["patient", "practitioner", "datetime", "startTime", "endTime", "status"].includes(field.name)) {
      field.required = false;
    }
  }

  return app.save(collection);
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_1037645436");

  return app.save(collection);
})
