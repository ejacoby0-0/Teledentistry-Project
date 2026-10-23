package com.example.teledentistry_project.data.model.prescription;

import java.util.List;

public class PrescriptionListResponse {
    public List<Prescription> items;
    public int page;
    public int perPage;
    public int totalItems;
    public int totalPages;
}