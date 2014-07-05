package de.hhu.propra.team61.io.json;

import org.json.simple.JSONValue;

/**
 * Wrapper class for org.json.simple.JSONArray providing convenience methods which are available in org.json.JSONArray.
 * See {@link de.hhu.propra.team61.io.json.JSONObject} for more information.
 */
@SuppressWarnings("unchecked") // limitation of org.json.simple
public class JSONArray extends org.json.simple.JSONArray {

    public JSONArray() {
        super();
    }

    public JSONArray(String jsonArrayString) {
        super();
        org.json.simple.JSONObject json = (org.json.simple.JSONObject)JSONValue.parse("{\"array\":" + jsonArrayString + "}");
        this.addAll((org.json.simple.JSONArray)json.get("array"));
    }

    JSONArray(org.json.simple.JSONArray jsonArray) {
        super();
        this.addAll(jsonArray);
    }

    public void put(JSONObject value) {
        this.add(value);
    }

    public void put(int value) {
        this.add(value);
    }

    public void put(String value) {
        this.add(value);
    }

    public int getInt(int index) {
        return ((Number)this.get(index)).intValue();
    }

    public String getString(int index) {
        return ((String)this.get(index));
    }

    public JSONObject getJSONObject(int index) {
        return new JSONObject((org.json.simple.JSONObject)this.get(index));
    }

    public int length() {
        return this.size();
    }

}
