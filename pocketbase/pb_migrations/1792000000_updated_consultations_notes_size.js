/// <reference path="../pb_data/types.d.ts" />
// diagnosisNotes may hold a JSON payload (notes + legacy chatLog + overlayState) that easily
// exceeds the 5000 char default limit (max 0). Chat and the teeth overlay now go over Matrix,
// so access rules stay practitioner-only.
migrate((app) => {
  const collection = app.findCollectionByNameOrId("pbc_3441013282")

  const notes = collection.fields.getByName("diagnosisNotes")
  notes.max = 2000000

  return app.save(collection)
}, (app) => {
  const collection = app.findCollectionByNameOrId("pbc_3441013282")

  const notes = collection.fields.getByName("diagnosisNotes")
  notes.max = 0

  return app.save(collection)
})
