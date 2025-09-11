package com.toast.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Collection {

    private String name;
    private List<SavedRequest> requests = new ArrayList<>();

    public Collection(String collectionName, List<SavedRequest> result) {
        this.name = collectionName;
        this.requests = result;
    } // needed for Jackson

    public Collection(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SavedRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<SavedRequest> requests) {
        this.requests = requests;
    }

    public void addRequest(SavedRequest request) {
        requests.add(request);
    }
}