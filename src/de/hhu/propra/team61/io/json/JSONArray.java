package de.hhu.propra.team61.io.json;

import org.json.simple.JSONValue;

/**
 * Wrapper class for org.json.simple.JSONArray providing convenience methods which are available in org.json.JSONArray.
 * See {@link de.hhu.propra.team61.io.json.JSONObject} for more information.
 */
@SuppressWarnings("unchecked") // limitation of org.json.simple
public class JSONArray extends org.json.simple.JSONArray {

    /**
     * Creates an empty JSONObject.
     */
    public JSONArray() {
        super();
    }

    /**
     * Creates a JSONArray containing the keys/values in the given json fragment (must start with '[', and end with ']).
     * @param jsonArrayString a json string
     */
    public JSONArray(String jsonArrayString) {
        super();
        org.json.simple.JSONObject json = (org.json.simple.JSONObject)JSONValue.parse("{\"array\":" + jsonArrayString + "}");
        this.addAll((org.json.simple.JSONArray) json.get("array"));
    }

    /**
     * Converts objects from superclass org.json.simple.JSONArray to de.hhu.propra.team61.io.json.JSONArray.
     * @param jsonArray org.json.simple.JSONArray to be converted
     */
    JSONArray(org.json.simple.JSONArray jsonArray) {
        super();
        this.addAll(jsonArray);
    }

    /**
     * Gets the number of elements in the JSONArray.
     * Same as calling {@code size}.
     * @return size of the array
     */
    public int length() {
        return this.size();
    }

    /**
     * Gets the integer at the given array index.
     * The caller has to assure that the key really holds an integer.
     * @param index the index whose value is obtained
     * @return value mapped to the given key
     */
    public int getInt(int index) {
        return ((Number)this.get(index)).intValue();
    }

    /**
     * Gets the integer at the given array index, or returns the given default value when the index does not exist.
     * The caller has to assure that the key really holds an integer.
     * @param index the index whose value is obtained
     * @return value mapped to the given key
     */
    public int getInt(int index, int defaultValue) {
        if(index < length()) return getInt(index);
        return defaultValue;
    }

    /**
     * Gets the string at the given array index.
     * The caller has to assure that the key really holds a string.
     * @param index the index whose value is obtained
     * @return value mapped to the given key
     */
    public String getString(int index) {
        return ((String)this.get(index));
    }

    /**
     * Gets the JSONObject at the given array index.
     * The caller has to assure that the key really holds a JSONObject.
     * @param index the index whose value is obtained
     * @return value mapped to the given key
     */
    public JSONObject getJSONObject(int index) {
        return new JSONObject((org.json.simple.JSONObject)this.get(index));
    }

    /**
     * Appends the given value to the array.
     * @param value value which is appended
     */
    public void put(JSONObject value) {
        this.add(value);
    }

    /**
     * Appends the given value to the array.
     * @param value value which is appended
     */
    public void put(int value) {
        this.add(value);
    }

    /**
     * Appends the given value to the array.
     * @param value value which is appended
     */
    public void put(String value) {
        this.add(value);
    }

    /**
     * Replaces the value at the given index with the given value
     * @param index index of the value
     * @param value new value
     */
    public void set(int index, JSONObject value) {
        super.set(index, value);
    }
}
