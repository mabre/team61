package de.hhu.propra.team61.io.json;

import org.json.simple.JSONValue;

/**
 * Wrapper class for org.json.simple.JSONObject providing convenience methods which are available in org.json.JSONObject.
 * <p>
 * The package from <a href="http://json.org/java">json.org</a> is released under the
 * <a href="http://www.json.org/license.html">JSON License</a>, which is
 * <a href="https://www.gnu.org/licenses/license-list.en.html#JSON">incompatible</a> with GPL 3. json-simple is available
 * under Apache License Version 2.0, which is <a href="https://www.gnu.org/licenses/license-list.en.html#apache2">compatible</a>
 * with GPL 3.
 */
@SuppressWarnings("unchecked") // limitation of org.json.simple
public class JSONObject extends org.json.simple.JSONObject {

    /**
     * Creates an empty JSONObject.
     */
    public JSONObject() {
        super();
    }

    /**
     * Creates a JSONObject containing the keys/values in the given json string.
     * @param jsonString a json string
     */
    public JSONObject(String jsonString) {
        super();
        org.json.simple.JSONObject json = (org.json.simple.JSONObject)JSONValue.parse(jsonString);
        this.putAll(json);
    }

    /**
     * Converts objects from superclass org.json.simple.JSONObject to de.hhu.propra.team61.io.json.JSONObject.
     * @param jsonObject org.json.simple.JSONObject to be converted
     */
    JSONObject(org.json.simple.JSONObject jsonObject) {
        super();
        this.putAll(jsonObject);
    }

    /**
     * Checks if the given key exists.
     * Same as calling {@code containsKey}.
     * @param key the key whose existence is checked
     * @return true if the key exists
     */
    public boolean has(String key) {
        return this.containsKey(key);
    }

    /**
     * Gets the boolean value for the given key.
     * The caller has to assure that the key really holds a boolean.
     * @param key the key whose value is obtained
     * @return value mapped to the given key
     */
    public boolean getBoolean(String key) {
        return (Boolean)this.get(key);
    }

    /**
     * Returns the value of the given boolean property. If the key is not found, the given default is returned.
     * @param key the key whose value shall be obtained
     * @param defaultValue the fall back value
     * @return the value for the key
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        if(has(key)) {
            return getBoolean(key);
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets the integer value for the given key.
     * The caller has to assure that the key really holds an integer.
     * @param key the key whose value is obtained
     * @return value mapped to the given key
     */
    public int getInt(String key) {
        return ((Number)this.get(key)).intValue();
    }

    /**
     * Returns the value of the given integer property. If the key is not found, the given default is returned.
     * @param key the key whose value shall be obtained
     * @param defaultValue the fall back value
     * @return the value for the key
     */
    public int getInt(String key, int defaultValue) {
        if(has(key)) {
            return getInt(key);
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets the double value for the given key.
     * The caller has to assure that the key really holds a double.
     * @param key the key whose value is obtained
     * @return value mapped to the given key
     */
    public double getDouble(String key) {
        return ((Number)this.get(key)).doubleValue();
    }

    /**
     * Gets the string value for the given key.
     * The caller has to assure that the key really holds a string.
     * @param key the key whose value is obtained
     * @return value mapped to the given key
     */
    public String getString(String key) {
        return (String)this.get(key);
    }

    /**
     * Gets the JSONObject mapped to the given key.
     * The caller has to assure that the key really holds a JSONObject.
     * @param key the key whose value is obtained
     * @return value mapped to the given key
     */
    public JSONObject getJSONObject(String key) {
        return new JSONObject((org.json.simple.JSONObject)this.get(key));
    }

    /**
     * Gets the JSONArray mapped to the given key.
     * The caller has to assure that the key really holds a JSONArray.
     * @param key the key whose value is obtained
     * @return value mapped to the given key
     */
    public JSONArray getJSONArray(String key) {
        return new JSONArray((org.json.simple.JSONArray)this.get(key));
    }

    // specialized versions of HashMap.put(K, V) to suppress warnings about unchecked calls

    /**
     * Maps the given value to the given key. If the key already exists, the value is replaced.
     * @param key key for the value
     * @param value value for the key
     */
    public void put(String key, boolean value) {
        super.put(key, value);
    }

    /**
     * Maps the given value to the given key. If the key already exists, the value is replaced.
     * @param key key for the value
     * @param value value for the key
     */
    public void put(String key, int value) {
        super.put(key, value);
    }

    /**
     * Maps the given value to the given key. If the key already exists, the value is replaced.
     * @param key key for the value
     * @param value value for the key
     */
    public void put(String key, double value) {
        super.put(key, value);
    }

    /**
     * Maps the given value to the given key. If the key already exists, the value is replaced.
     * @param key key for the value
     * @param value value for the key
     */
    public void put(String key, String value) {
        super.put(key, value);
    }

    /**
     * Maps the given value to the given key. If the key already exists, the value is replaced.
     * @param key key for the value
     * @param value value for the key
     */
    public void put(String key, JSONObject value) {
        super.put(key, value);
    }

    /**
     * Maps the given value to the given key. If the key already exists, the value is replaced.
     * @param key key for the value
     * @param value value for the key
     */
    public void put(String key, JSONArray value) {
        super.put(key, value);
    }
}
