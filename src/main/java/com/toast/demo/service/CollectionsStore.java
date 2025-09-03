package com.toast.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toast.demo.model.Collection;
import com.toast.demo.model.SavedRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class CollectionsStore {

    private static final String FILE_PATH = "collections.json";
    private static final CollectionsStore INSTANCE = new CollectionsStore();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Consumer<List<Collection>>> listeners = new ArrayList<>();

    private List<Collection> collections = new ArrayList<>();

    private CollectionsStore() {
        load();
    }

    public static CollectionsStore getInstance() {
        return INSTANCE;
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public void addCollection(Collection collection) {
        collections.add(collection);
        saveAndNotify();
    }

    // Event system
    public void addListener(Consumer<List<Collection>> listener) {
        listeners.add(listener);
    }

    private void saveAndNotify() {
        save();
        notifyListeners();
    }

    private void notifyListeners() {
        for (Consumer<List<Collection>> listener : listeners) {
            listener.accept(Collections.unmodifiableList(collections));
        }
    }

    public void save() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), collections);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        File file = new File(FILE_PATH);
        try {
            if (!file.exists() || Files.size(file.toPath()) == 0) {
                // create with default collection
                collections = new ArrayList<>();
                collections.add(new Collection("Default"));
                save();
                return;
            }

            collections = objectMapper.readValue(
                file,
                new TypeReference<List<Collection>>() {
                }
            );

        } catch (JsonProcessingException e) {
            System.err.println("Corrupted collections.json, resetting...");
            collections = new ArrayList<>();
            collections.add(new Collection("Default"));
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeCollectionByName(String name) {
        collections.removeIf(c -> c.getName().equals(name));
        saveAndNotify();
    }

    public void addRequestToCollection(String collectionName, SavedRequest request) {
        collections.stream()
            .filter(c -> c.getName().equals(collectionName))
            .findFirst()
            .ifPresent(c -> c.getRequests().add(request));
        saveAndNotify();
    }
}