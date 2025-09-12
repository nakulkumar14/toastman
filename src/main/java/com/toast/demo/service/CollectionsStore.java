package com.toast.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.toast.demo.model.Collection;
import com.toast.demo.model.SavedRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionsStore {

    private static final Logger log = LoggerFactory.getLogger(CollectionsStore.class);

    private static final String COLLECTIONS_DIR = "collections";
    private static final CollectionsStore INSTANCE = new CollectionsStore();
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private final List<Consumer<List<Collection>>> listeners = new ArrayList<>();

    private List<Collection> collections = new ArrayList<>();

    private CollectionsStore() {
        load();
    }

    public static CollectionsStore getInstance() {
        return INSTANCE;
    }

    public List<Collection> getCollections() {
        return new ArrayList<>(collections);
    }

    public void addCollection(Collection collection) {
        collections.add(collection);
        saveCollection(collection);
        notifyListeners();
    }

    // Event system
    public void addListener(Consumer<List<Collection>> listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (Consumer<List<Collection>> listener : listeners) {
            listener.accept(Collections.unmodifiableList(collections));
        }
    }

    private void saveCollection(Collection collection) {
        File dir = new File(COLLECTIONS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = getFileForCollection(collection.getName());
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, collection);
        } catch (IOException e) {
            log.error("Failed to save collection: " + collection.getName(), e);
        }
        notifyListeners();
    }

    private File getFileForCollection(String name) {
        return new File(COLLECTIONS_DIR, name.replaceAll("\\s+", "_") + ".json");
    }

    private void load() {
        File dir = new File(COLLECTIONS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            // add default collection if none exist
            Collection defaultCol = new Collection("Default");
            collections.add(defaultCol);
            saveCollection(defaultCol);
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File f : files) {
                try {
                    Collection col = objectMapper.readValue(f, Collection.class);
                    collections.add(col);
                } catch (IOException e) {
                    log.error("Exception : ", e);
                }
            }
        }
        notifyListeners();
    }

    public void removeCollectionByName(String name) {
        collections.removeIf(c -> c.getName().equalsIgnoreCase(name));
        File f = getFileForCollection(name);
        if (f.exists()) {
            f.delete();
        }
        notifyListeners();
    }

    public void addRequestToCollection(String collectionName, SavedRequest request) {
        collections.stream()
            .filter(c -> c.getName().equals(collectionName))
            .findFirst()
            .ifPresent(collection -> {
                collection.addRequest(request);
                saveCollection(collection);
                notifyListeners();
            });
    }

    public void removeRequestFromCollection(String collectionName, SavedRequest request) {
        collections.stream()
            .filter(c -> c.getName().equals(collectionName))
            .findFirst()
            .ifPresent(c -> {
                c.getRequests().removeIf(req -> req.getId().equals(request.getId()));
                saveCollection(c);
                notifyListeners();
            });
    }

    public void updateCollection(Collection updated) {
        for (int i = 0; i < collections.size(); i++) {
            if (collections.get(i).getName().equalsIgnoreCase(updated.getName())) {
                collections.set(i, updated);
                saveCollection(updated);   // overwrite file
                notifyListeners();
                return;
            }
        }
        // fallback: if not found, just add it
        addCollection(updated);
    }
}