# TELEDENTISTRY - Complete UI & Screen Explanation Documentation

This document provides a comprehensive overview of all user interfaces (UIs), XML layouts, Activities, and role-based navigation flows in the **TELEDENTISTRY** Android application.

---

## 1. Authentication Experience (XML + Java)

| Screen / Activity | Layout XML | Description & Functionality |
| :--- | :--- | :--- |
| **LoginActivity** | `activity_login.xml` | User authentication via email and password with inline validation. Communicates with Supabase Auth repository and saves session upon successful sign-in. |
| **RegisterActivity** | `activity_register.xml` | Account registration form with email/password validation, minimum length checks, and confirmation matching. |

---

## 2. Patient Experience

| Screen / Activity | Layout XML | Description & Functionality |
| :--- | :--- | :--- |
| **PatientHomeActivity** | `activity_patient_home.xml` | Main patient dashboard displaying upcoming appointment card, quick actions (Book Consult, Chat, Upload Image, Treatment Plan, Billing), profile access, and role-specific **Bottom Navigation Bar**. |
| **PatientProfileActivity** | `activity_patient_profile.xml` | Personal and medical information editing screen (Name, Email, Phone). |
| **BookAppointmentActivity** | `activity_book_appointment.xml` | Consultation booking form with practitioner dropdown selection, date/time inputs, and symptoms notes. |
| **ChatActivity** | `activity_chat.xml` | Healthcare messaging interface allowing patients to chat with their dentist and attach dental photos. |
| **UploadImageActivity** | `activity_upload_image.xml` | Dental image capture and category selection form (Tooth, Gum, Mouth, X-Ray) with preview. |
| **TreatmentPlanActivity** | `activity_treatment_plan.xml` | Visual treatment timeline and stage tracking (Completed, Current, Upcoming). |
| **BillingActivity** | `activity_billing.xml` | Invoice list displaying paid/pending statuses and demo payment modal. |
| **VideoCallActivity** | `activity_video_call.xml` | HD video tele-consultation call screen with status indicators and end call control. |

---

## 3. Practitioner / Dentist Experience

| Screen / Activity | Layout XML | Description & Functionality |
| :--- | :--- | :--- |
| **DentistHomeActivity** | `activity_dentist_home.xml` | Practitioner portal home showing today's consultation queue, quick actions, and **Bottom Navigation Bar** (Dashboard, Schedule, Patients, Profile). |
| **PatientRecordsActivity** | `activity_patient_records.xml` | Patient directory search and medical history records viewer. |
| **UploadedImagesActivity** | `activity_uploaded_images.xml` | Gallery view of patient-submitted dental images for clinical review. |
| **TreatmentManagementActivity** | `activity_treatment_management.xml` | Form to create diagnoses, clinical notes, and issue e-prescriptions. |

---

## 4. Administrator Experience

| Screen / Activity | Layout XML | Description & Functionality |
| :--- | :--- | :--- |
| **AdminHomeActivity** | `activity_admin_home.xml` | Administrative control center showing pending practitioner approval alerts, system statistics, and **Bottom Navigation Bar** (Dashboard, Users, Approvals, Reports). |
| **PractitionerApprovalsActivity** | `activity_practitioner_approvals.xml` | Review screen for pending practitioner licence credentials with Approve and Reject actions. |
| **ManageUsersActivity** | `activity_manage_users.xml` | User governance list with role filtering and active/inactive status toggles. |
| **UserEnquiriesActivity** | `activity_user_enquiries.xml` | Customer and practitioner support ticket manager with priority and status indicators. |
| **ReportsActivity** | `activity_reports.xml` | System analytics summary showing monthly consultation metrics and PDF export options. |

---

## 5. Bottom Navigation Bar Integration

Every role dashboard (`PatientHomeActivity`, `DentistHomeActivity`, and `AdminHomeActivity`) includes a persistent **Bottom Navigation Bar** fixed at the bottom of the screen, providing quick access to primary sections tailored specifically to that user role.
