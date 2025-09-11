package com.toast.demo.components;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.toast.demo.model.Collection;
import com.toast.demo.model.SavedRequest;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionImporter {

    private static final Logger log = LoggerFactory.getLogger(CollectionImporter.class);

    public static Collection importCollection(File file) throws IOException {
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(new FileReader(file), JsonObject.class);

        String collectionName = root.has("info")
            ? root.getAsJsonObject("info").get("name").getAsString()
            : file.getName();

        List<SavedRequest> result = new ArrayList<>();
        JsonArray items = root.getAsJsonArray("item");

        for (JsonElement itemEl : items) {
            JsonObject item = itemEl.getAsJsonObject();
            String name = item.get("name").getAsString();

            JsonObject req = item.getAsJsonObject("request");
            String method = req.get("method").getAsString();
            String url = req.getAsJsonObject("url").get("raw").getAsString();

            Map<String, String> headers = new LinkedHashMap<>();
            JsonArray headerArr = req.getAsJsonArray("header");
            if (headerArr != null) {
                for (JsonElement h : headerArr) {
                    JsonObject obj = h.getAsJsonObject();
                    headers.put(obj.get("key").getAsString(), obj.get("value").getAsString());
                }
            }

            String body = "";
            if (req.has("body")) {
                JsonObject bodyObj = req.getAsJsonObject("body");
                if ("raw".equals(bodyObj.get("mode").getAsString())) {
                    body = bodyObj.get("raw").getAsString();
                }
            }

            result.add(new SavedRequest(name, method, url, headers, body, null, "raw"));
            log.debug("Imported request: {} {} {}", method, url, headers);
        }

        log.debug("Collection name: {}, size: {}", collectionName, result.size());
        return new Collection(collectionName, result);
    }
}