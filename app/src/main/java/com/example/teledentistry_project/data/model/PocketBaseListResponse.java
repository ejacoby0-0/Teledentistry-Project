package com.example.teledentistry_project.data.model;

import java.util.List;

public class PocketBaseListResponse<T> {

    private int page;
    private int perPage;
    private int totalItems;
    private int totalPages;

    private List<T> items;

    public int getPage() {
        return page;
    }

    public int getPerPage() {
        return perPage;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List<T> getItems() {
        return items;
    }
}