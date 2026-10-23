/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const usersCollection = app.findCollectionByNameOrId("_pb_users_auth_");
  const practitionersCollection = app.findCollectionByNameOrId("practitioners");
  const availabilityCollection = app.findCollectionByNameOrId("practitioner_availability");

  // Ensure practitioner user account 1 exists
  let userRecord;
  try {
    userRecord = app.findAuthRecordByEmail("_pb_users_auth_", "dentist@test.com");
  } catch (e) {
    try {
      userRecord = app.findAuthRecordByEmail("_pb_users_auth_", "practitioner@example.com");
    } catch (e2) {
      userRecord = new Record(usersCollection);
      userRecord.setEmail("dentist@test.com");
      userRecord.setPassword("12345678");
      userRecord.set("fullName", "Dr. Sarah Jenkins");
      userRecord.set("phoneNumber", "+1987654321");
      userRecord.set("role", "practitioner");
      userRecord.set("verified", true);
      app.save(userRecord);
    }
  }

  // Ensure practitioner record 1 exists in practitioners collection
  let practitionerRecord;
  try {
    practitionerRecord = app.findFirstRecordByFilter("practitioners", "user = {:userId}", { userId: userRecord.id });
  } catch (e) {
    practitionerRecord = new Record(practitionersCollection);
    practitionerRecord.set("user", userRecord.id);
    practitionerRecord.set("fullName", "Dr. Sarah Jenkins");
    practitionerRecord.set("licenseNumber", "LIC-12345");
    practitionerRecord.set("specialization", "General Dentistry");
    practitionerRecord.set("availabilityStatus", "available");
    app.save(practitionerRecord);
  }

  // Ensure practitioner user account 2 exists
  let userRecord2;
  try {
    userRecord2 = app.findAuthRecordByEmail("_pb_users_auth_", "chen@test.com");
  } catch (e) {
    userRecord2 = new Record(usersCollection);
    userRecord2.setEmail("chen@test.com");
    userRecord2.setPassword("12345678");
    userRecord2.set("fullName", "Dr. Michael Chen");
    userRecord2.set("phoneNumber", "+1987654322");
    userRecord2.set("role", "practitioner");
    userRecord2.set("verified", true);
    app.save(userRecord2);
  }

  // Ensure practitioner record 2 exists in practitioners collection
  let practitionerRecord2;
  try {
    practitionerRecord2 = app.findFirstRecordByFilter("practitioners", "user = {:userId}", { userId: userRecord2.id });
  } catch (e) {
    practitionerRecord2 = new Record(practitionersCollection);
    practitionerRecord2.set("user", userRecord2.id);
    practitionerRecord2.set("fullName", "Dr. Michael Chen");
    practitionerRecord2.set("licenseNumber", "LIC-12346");
    practitionerRecord2.set("specialization", "Orthodontics");
    practitionerRecord2.set("availabilityStatus", "available");
    app.save(practitionerRecord2);
  }

  // Ensure practitioner availability slots exist
  try {
    app.findFirstRecordByFilter("practitioner_availability", "practitioner = {:pracId}", { pracId: practitionerRecord.id });
  } catch (e) {
    const availRecord = new Record(availabilityCollection);
    availRecord.set("practitioner", practitionerRecord.id);
    availRecord.set("date", "2026-10-25 00:00:00.000Z");
    availRecord.set("startTime", "09:00");
    availRecord.set("endTime", "17:00");
    availRecord.set("isAvailable", true);
    app.save(availRecord);
  }

  try {
    app.findFirstRecordByFilter("practitioner_availability", "practitioner = {:pracId}", { pracId: practitionerRecord2.id });
  } catch (e) {
    const availRecord = new Record(availabilityCollection);
    availRecord.set("practitioner", practitionerRecord2.id);
    availRecord.set("date", "2026-10-25 00:00:00.000Z");
    availRecord.set("startTime", "09:00");
    availRecord.set("endTime", "17:00");
    availRecord.set("isAvailable", true);
    app.save(availRecord);
  }
}, (app) => {
  // rollback
})
