/// <reference path="../pb_data/types.d.ts" />
migrate((app) => {
  const usersCollection = app.findCollectionByNameOrId("_pb_users_auth_");
  const patientsCollection = app.findCollectionByNameOrId("patients");
  const practitionersCollection = app.findCollectionByNameOrId("practitioners");
  const availabilityCollection = app.findCollectionByNameOrId("practitioner_availability");

  const mockUsers = [
    {
      email: "patient@example.com",
      password: "password123",
      passwordConfirm: "password123",
      fullName: "John Doe",
      phoneNumber: "+1234567890",
      role: "patient",
      verified: true
    },
    {
      email: "patient@test.com",
      password: "12345678",
      passwordConfirm: "12345678",
      fullName: "John Doe",
      phoneNumber: "+1234567890",
      role: "patient",
      verified: true
    },
    {
      email: "practitioner@example.com",
      password: "password123",
      passwordConfirm: "password123",
      fullName: "Dr. Sarah Jenkins",
      phoneNumber: "+1987654321",
      role: "practitioner",
      verified: true
    },
    {
      email: "dentist@test.com",
      password: "12345678",
      passwordConfirm: "12345678",
      fullName: "Dr. Sarah Jenkins",
      phoneNumber: "+1987654321",
      role: "practitioner",
      verified: true
    },
    {
      email: "admin@example.com",
      password: "password123",
      passwordConfirm: "password123",
      fullName: "System Administrator",
      phoneNumber: "+1112223333",
      role: "admin",
      verified: true
    },
    {
      email: "admin@test.com",
      password: "12345678",
      passwordConfirm: "12345678",
      fullName: "System Administrator",
      phoneNumber: "+1112223333",
      role: "admin",
      verified: true
    }
  ];

  for (const userData of mockUsers) {
    let userRecord;
    try {
      userRecord = app.findAuthRecordByEmail("_pb_users_auth_", userData.email);
    } catch (e) {
      userRecord = new Record(usersCollection);
      userRecord.setEmail(userData.email);
      userRecord.setPassword(userData.password);
      userRecord.set("fullName", userData.fullName);
      userRecord.set("phoneNumber", userData.phoneNumber);
      userRecord.set("role", userData.role);
      userRecord.set("verified", userData.verified);
      app.save(userRecord);
    }

    if (userData.role === "patient") {
      try {
        app.findFirstRecordByFilter("patients", "user = {:userId}", { userId: userRecord.id });
      } catch (e) {
        const patientRecord = new Record(patientsCollection);
        patientRecord.set("user", userRecord.id);
        patientRecord.set("fullName", userData.fullName);
        patientRecord.set("dateOfBirth", "2000-01-01 00:00:00.000Z");
        patientRecord.set("medicalHistory", "None reported");
        patientRecord.set("dentalHistory", "Regular cleanings");
        patientRecord.set("emergencyContact", "Jane Doe (+1234567899)");
        app.save(patientRecord);
      }
    } else if (userData.role === "practitioner") {
      let practitionerRecord;
      try {
        practitionerRecord = app.findFirstRecordByFilter("practitioners", "user = {:userId}", { userId: userRecord.id });
      } catch (e) {
        practitionerRecord = new Record(practitionersCollection);
        practitionerRecord.set("user", userRecord.id);
        practitionerRecord.set("fullName", userData.fullName);
        practitionerRecord.set("licenseNumber", "LIC-12345");
        practitionerRecord.set("specialization", "General Dentistry");
        practitionerRecord.set("availabilityStatus", "available");
        app.save(practitionerRecord);
      }

      // Seed practitioner availability slots for appointment booking
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
    }
  }
}, (app) => {
  const emails = ["patient@example.com", "patient@test.com", "practitioner@example.com", "dentist@test.com", "admin@example.com", "admin@test.com"];
  for (const email of emails) {
    try {
      const record = app.findAuthRecordByEmail("_pb_users_auth_", email);
      app.delete(record);
    } catch (e) {
    }
  }
})
