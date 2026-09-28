-- ============================================================
-- Smil-E - Initial Database Schema
-- Project: Smil-E Teledentistry
-- Backend: Supabase / PostgreSQL
-- ============================================================

-- ============================================================
-- EXTENSIONS
-- ============================================================

create extension if not exists pgcrypto;


-- ============================================================
-- USER PROFILE
-- ============================================================
-- Supabase Auth stores the actual authentication credentials
-- in auth.users.
--
-- This table stores application-level information associated
-- with the authenticated user.
-- ============================================================

create table public.profiles (
    user_id uuid primary key
        references auth.users(id)
        on delete cascade,

    email text not null,

    role text not null
        check (role in ('patient', 'practitioner', 'admin')),

    created_at timestamptz not null default now()
);


-- ============================================================
-- PATIENT
-- ============================================================

create table public.patients (
    patient_id uuid primary key
        references public.profiles(user_id)
        on delete cascade,

    medical_history text,

    dental_history text,

    emergency_contact varchar(50)
);


-- ============================================================
-- PRACTITIONER
-- ============================================================
-- The ERD identifies licenseNumber as the primary key.
-- user_id connects the practitioner to the authenticated
-- Supabase user.
-- ============================================================

create table public.practitioners (
    license_number varchar(50) primary key,

    user_id uuid not null unique
        references public.profiles(user_id)
        on delete cascade,

    specialization varchar(50),

    availability_status varchar(50)
);


-- ============================================================
-- DENTAL RECORD
-- ============================================================

create table public.dental_records (
    record_id varchar(50) primary key,

    patient_id uuid not null
        references public.patients(patient_id)
        on delete cascade,

    last_updated timestamptz not null default now()
);


-- ============================================================
-- TOOTH
-- ============================================================
-- A tooth belongs to a dental record.
-- The ERD identifies toothNumber and recordID as the key
-- relationship.
-- ============================================================

create table public.teeth (
    tooth_number varchar(50) not null,

    record_id varchar(50) not null
        references public.dental_records(record_id)
        on delete cascade,

    condition varchar(50),

    treatment_history text,

    primary key (tooth_number, record_id)
);


-- ============================================================
-- TREATMENT PLAN
-- ============================================================

create table public.treatment_plans (
    plan_id varchar(50) primary key,

    patient_id uuid not null
        references public.patients(patient_id)
        on delete cascade,

    dentist_license_number varchar(50) not null
        references public.practitioners(license_number),

    total_cost decimal(10, 2),

    start_date date,

    status varchar(50)
);


-- ============================================================
-- MEDIA RECORDS
-- ============================================================

create table public.media_records (
    media_id varchar(50) primary key,

    patient_id uuid not null
        references public.patients(patient_id)
        on delete cascade,

    upload_type varchar(50),

    file_path varchar(255),

    upload_date timestamptz not null default now()
);


-- ============================================================
-- PRESCRIPTION
-- ============================================================

create table public.prescriptions (
    prescription_id varchar(50) primary key,

    patient_id uuid not null
        references public.patients(patient_id)
        on delete cascade,

    practitioner_license_number varchar(50) not null
        references public.practitioners(license_number),

    medication varchar(50),

    dosage varchar(50),

    issue_date date,

    expiry_date date
);


-- ============================================================
-- APPOINTMENT
-- ============================================================

create table public.appointments (
    appointment_id varchar(50) primary key,

    patient_id uuid not null
        references public.patients(patient_id)
        on delete cascade,

    dentist_license_number varchar(50) not null
        references public.practitioners(license_number),

    appointment_datetime timestamptz,

    start_time time,

    end_time time,

    status varchar(50)
);


-- ============================================================
-- BILLING
-- ============================================================

create table public.billing (
    billing_id varchar(50) primary key,

    appointment_id varchar(50) not null
        references public.appointments(appointment_id)
        on delete cascade,

    amount decimal(10, 2),

    insurance_coverage decimal(10, 2),

    payment_status varchar(50)
);


-- ============================================================
-- CONSULTATION
-- ============================================================

create table public.consultations (
    consultation_id varchar(50) primary key,

    appointment_id varchar(50) not null
        references public.appointments(appointment_id)
        on delete cascade,

    diagnosis_notes text
);


-- ============================================================
-- INSURANCE CLAIM
-- ============================================================

create table public.insurance_claims (
    claim_id varchar(50) primary key,

    billing_id varchar(50) not null
        references public.billing(billing_id)
        on delete cascade,

    policy_number varchar(50),

    claim_status varchar(50)
);


-- ============================================================
-- STORE & FORWARD
-- ============================================================

create table public.store_and_forward (
    message_id varchar(50) primary key,

    consultation_id varchar(50) not null
        references public.consultations(consultation_id)
        on delete cascade,

    symptom_details varchar(255),

    image_url varchar(255),

    triage_level varchar(50)
);


-- ============================================================
-- LIVE VIDEO CALL
-- ============================================================

create table public.live_video_calls (
    session_token varchar(50) primary key,

    consultation_id varchar(50) not null
        references public.consultations(consultation_id)
        on delete cascade,

    agora_channel_id varchar(50),

    call_duration integer
);