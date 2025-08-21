/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonPathFinder {
	
	private JsonPathFinder() {
        throw new AssertionError("Cannot instantiate JsonPathFinder");
    }
	
	public static List<String> findRepetitionPaths(JsonNode jsonNode, String key) {
		Map<String, List<String>> valuePaths = new HashMap<>();
		findRepititionsRecursive(jsonNode, key, "$", valuePaths);
		return valuePaths.entrySet().stream().filter(entry -> entry.getValue().size() > 1)
				.flatMap(entry -> entry.getValue().stream()).collect(Collectors.toList());
	}

	private static void findRepititionsRecursive(JsonNode jsonNode, String key, String path,
			Map<String, List<String>> valuePaths) {
		if (jsonNode.isObject()) {
			Iterator<Entry<String, JsonNode>> fields = jsonNode.fields();

			while (fields.hasNext()) {
				Entry<String, JsonNode> field = fields.next();
				String curPath = path + "." + field.getKey();

				if (field.getKey().equals(key)) {
					valuePaths.computeIfAbsent(field.getValue().asText(), k -> new ArrayList<>()).add(curPath);
				}

				findRepititionsRecursive(field.getValue(), key, curPath, valuePaths);
			}
		} else if (jsonNode.isArray()) {
			for (int i = 0; i < jsonNode.size(); i++) {
				JsonNode arr = jsonNode.get(i);
				findRepititionsRecursive(arr, key, path + "[" + i + "]", valuePaths);
			}
		}
	}

	public static String getPath(JSONObject json, String key, String value, boolean isObj) {
		return recursiveSearch(json, key, value, "$", isObj);
	}

	private static String recursiveSearch(JSONObject obj, String key, String value, String path, boolean isObj) {
		if (obj.has(key) && obj.get(key).equals(value)) {
			return path + "." + key;
		}
		if (isObj && obj.similar(new JSONObject(value))) {
			return path + "." + key;
		}

		for (String k : obj.keySet()) {
			Object v = obj.get(k);
			if (v instanceof JSONObject) {
				String result = recursiveSearch((JSONObject) v, key, value, path.isEmpty() ? k : path + "." + k, isObj);
				if (result != null) {
					return result;
				}
			} else if (v instanceof JSONArray) {
				String result = recursiveSearch((JSONArray) v, key, value, path.isEmpty() ? k : path + "." + k, isObj);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	private static String recursiveSearch(JSONArray arr, String key, String value, String path, boolean isObj) {

		for (int i = 0; i < arr.length(); i++) {
			Object v = arr.get(i);
			if (v instanceof JSONObject) {
				String result = recursiveSearch((JSONObject) v, key, value, path + "[" + i + "]", isObj);
				if (result != null) {
					return result;
				}
			} else if (v instanceof JSONArray) {
				String result = recursiveSearch((JSONArray) v, key, value, path + "[" + i + "]", isObj);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
}
