package com.toast.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Folder {

    private String name;
    private List<SavedRequest> requests = new ArrayList<>();
    private List<Folder> subFolders = new ArrayList<>();

    public Folder() {
    }

    public Folder(String name) {
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

    public List<Folder> getSubFolders() {
        return subFolders;
    }

    public void setSubFolders(List<Folder> subFolders) {
        this.subFolders = subFolders;
    }

    public void addRequest(SavedRequest req) {
        requests.add(req);
    }

    public void addSubFolder(Folder folder) {
        subFolders.add(folder);
    }
}
