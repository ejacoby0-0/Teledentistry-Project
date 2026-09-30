# Teledentistry Application Wireframes (Authentication Excluded)

This document outlines the UI/UX wireframes for the Teledentistry mobile and web application, covering all functional modules for **Admin**, **Patient**, and **Practitioner** roles, excluding all authentication screens (Login, Register, Password Reset).

---

## 1. Admin Role Wireframes

### 1.1 Admin Dashboard & Manage Users (User Enquiries & Practitioner Approvals)
```
+---------------------------------------------------+
| Admin Portal                     [Admin Profile]  |
+---------------------------------------------------+
| Tabs: [Users / Enquiries] [Approvals] [Reports]   |
+---------------------------------------------------+
| SEARCH USERS / ENQUIRIES                          |
| [Search by name or email............] [Filter]    |
+---------------------------------------------------+
| PENDING PRACTITIONER APPROVALS                    |
| ------------------------------------------------- |
| Dr. Jane Smith (DDS #48291)                       |
| License Verified: [View Docs]                     |
| Actions: [Approve]  [Reject]                      |
| ------------------------------------------------- |
| Dr. Robert Chen (DDS #99214)                      |
| License Verified: [View Docs]                     |
| Actions: [Approve]  [Reject]                      |
+---------------------------------------------------+
| USER SUPPORT ENQUIRIES                            |
| ------------------------------------------------- |
| Ticket #102: Patient unable to upload photo       |
| Status: Open | Priority: High                     |
| [Respond]                                         |
+---------------------------------------------------+
```

### 1.2 Admin View Reports
```
+---------------------------------------------------+
| System Analytics & Reports       [Export PDF]     |
+---------------------------------------------------+
| Date Range: [Last 30 Days v]                      |
+---------------------------------------------------+
| [ KPI SUMMARY ]                                   |
| - Total Consultations: 1,248                      |
| - Active Practitioners: 45                        |
| - Active Patients: 3,102                          |
| - Monthly Revenue: $45,800                        |
+---------------------------------------------------+
| CONSULTATION TRENDS (Chart)                       |
|   ^                                               |
|   |          _/--\                                |
|   |    _    /     \                               |
|   |   / \__/       \                              |
|   +---------------------------->                  |
|     W1   W2   W3   W4                             |
+---------------------------------------------------+
| FINANCIAL BREAKDOWN                               |
| Consultations: $38,000 | Prescriptions: $7,800    |
+---------------------------------------------------+
```

---

## 2. Patient Role Wireframes

### 2.1 Patient Dashboard (Manage Profile & Upload Image)
```
+---------------------------------------------------+
| Patient Dashboard              [Bell] [Profile]   |
+---------------------------------------------------+
| Welcome back, Sarah!                              |
|                                                   |
| UPCOMING APPOINTMENT                              |
| Dr. Emily Watson - Oct 12, 10:00 AM               |
| [Join Video Call]                                 |
+---------------------------------------------------+
| QUICK ACTIONS                                     |
| [Book Appointment]   [Chat with Dentist]          |
| [Upload Dental Image] [View Treatment Plan]       |
+---------------------------------------------------+
| UPLOAD DENTAL IMAGE                               |
| Select area: [Upper Left Molar v]                 |
| [ Tap to Capture / Select Photo ]                 |
| Notes: [Tooth sensitivity when eating cold...]    |
| [Submit Image to Dentist]                         |
+---------------------------------------------------+
| MANAGE PROFILE                                    |
| Name: Sarah Jenkins  | DOB: 14/05/1992            |
| Phone: +1 555-0199   | Insurance: Delta Dental    |
| [Update Profile Information]                      |
+---------------------------------------------------+
```

### 2.2 Patient Consult & Communication (Booking, Video, Chat)
```
+---------------------------------------------------+
| Book Consultation                [< Back]         |
+---------------------------------------------------+
| Select Consultation Type:                         |
| (*) General Checkup  ( ) Toothache / Emergency    |
+---------------------------------------------------+
| Select Dentist:                                   |
| [ Dr. Emily Watson, DDS (Available Today) v ]     |
+---------------------------------------------------+
| Select Date & Time:                               |
| [ Oct 12, 2026 ]   [ 10:00 AM ] [ 02:30 PM ]      |
+---------------------------------------------------+
| Reason for visit:                                 |
| [Dull ache in lower right molar............]      |
+---------------------------------------------------+
| [ Confirm & Pay Consultation Fee ($75) ]          |
+---------------------------------------------------+
```

```
+---------------------------------------------------+
| Video Call Room                  [End Call]       |
+---------------------------------------------------+
| +-----------------------------------------------+ |
| |                                               | |
| |              [Dr. Emily Watson]               | |
| |                                               | |
| +-----------------------------------------------+ |
| [Mute Mic] [Turn Off Camera] [Chat] [Flip Cam]    |
+---------------------------------------------------+
```

```
+---------------------------------------------------+
| Chat with Dr. Emily Watson         [Call]         |
+---------------------------------------------------+
| [Dr. Watson]: Hello Sarah, how is the pain?       |
| 10:02 AM                                          |
|                                                   |
| [You]: It feels better after using the mouthwash  |
| 10:03 AM                                          |
+---------------------------------------------------+
| [ Type a message...                       ] [>]   |
+---------------------------------------------------+
```

### 2.3 Patient Treatment Plan & Billing
```
+---------------------------------------------------+
| Treatment Plan & Prescriptions   [< Back]         |
+---------------------------------------------------+
| ACTIVE TREATMENT PLAN #TP-402                     |
| Dentist: Dr. Emily Watson                         |
| Diagnosis: Mild Gingivitis & Molar Caries         |
|                                                   |
| Milestones:                                       |
| [x] Initial Consultation & X-Ray                  |
| [ ] Professional Cleaning (Scheduled Oct 15)      |
| [ ] Composite Filling #30                         |
+---------------------------------------------------+
| ISSUED PRESCRIPTIONS                              |
| - Chlorhexidine Mouthwash 0.12%                   |
|   Instructions: Rinse twice daily for 14 days     |
|   Status: Active | [View Pharmacy QR]             |
+---------------------------------------------------+
```

```
+---------------------------------------------------+
| Billing & Receipts               [Export PDF]     |
+---------------------------------------------------+
| ISSUED INCONSULTATION & PRESCRIPTION BILLS        |
| ------------------------------------------------- |
| Inv #9021 | Oct 10, 2026                          |
| Consult: Virtual Dental Checkup       $75.00      |
| Rx: Chlorhexidine Mouthwash           $15.00      |
| ------------------------------------------------- |
| Total: $90.00 | Status: [Paid]                    |
| [Download Receipt]                                |
+---------------------------------------------------+
```

---

## 3. Practitioner Role Wireframes

### 3.1 Practitioner Dashboard & Schedule
```
+---------------------------------------------------+
| Practitioner Portal            [Dr. Watson]       |
+---------------------------------------------------+
| TODAY'S SCHEDULE (Oct 10, 2026)                   |
| 09:00 AM - John Doe    (General Checkup)          |
| 10:00 AM - Sarah Jenkins (Follow-up)              |
| 02:00 PM - Michael Brown (Emergency Triage)       |
| [Start Next Consultation: Sarah Jenkins]          |
+---------------------------------------------------+
| MANAGE SCHEDULE & AVAILABILITY                    |
| Working Days: [Mon] [Tue] [Wed] [Thu] [Fri]       |
| Hours: [09:00 AM] to [05:00 PM]                   |
| [Save Availability Settings]                      |
+---------------------------------------------------+
```

### 3.2 Practitioner Consult & Patient Records (View Images & Consult)
```
+---------------------------------------------------+
| Active Consultation: Sarah Jenkins [End Session]  |
+---------------------------------------------------+
| Tabs: [Video Feed] [Patient Records] [Images]     |
+---------------------------------------------------+
| PATIENT RECORDS SUMMARY                           |
| Name: Sarah Jenkins | Age: 34 | Allergies: Penicillin|
| Medical History: Asthma                           |
+---------------------------------------------------+
| PATIENT UPLOADED IMAGES                           |
| [Thumbnail: Upper Left Molar]                     |
| [Click to Zoom / Inspect Image Filters]           |
+---------------------------------------------------+
| CONDUCT VIDEO CONSULTATION                        |
| [Active Video Stream with Patient]                |
| [Capture Frame] [Send Chat Message]               |
+---------------------------------------------------+
```

### 3.3 Practitioner Treatment (Diagnosis, Treatment Plan, Prescription)
```
+---------------------------------------------------+
| Treatment & Clinical Actions     [< Back]         |
+---------------------------------------------------+
| 1. CREATE DIAGNOSIS                               |
| Patient: Sarah Jenkins                            |
| Findings: [Localized enamel decay on tooth #30...]  |
| ICD-10 Code: [K02.1 - Dentin caries .......]      |
+---------------------------------------------------+
| 2. CREATE TREATMENT PLAN                          |
| Plan Title: [Molar Restoration & Hygiene]         |
| Procedures: [1. Prophylaxis 2. Composite #30]     |
| [Save & Assign Treatment Plan]                    |
+---------------------------------------------------+
| 3. ISSUE PRESCRIPTION                             |
| Medication: [Amoxicillin 500mg capsules]          |
| Dosage: [1 capsule every 8 hours for 7 days]      |
| [Sign & Issue e-Prescription]                     |
+---------------------------------------------------+
```
