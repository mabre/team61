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
     * If the key does not hold a boolean, false is returned.
     * @param key the key whose value is obtained
     * @return value mapped to the given key
     */
    public boolean getBoolean(String key) {
        Object ret = this.get(key);
        if(ret instanceof Boolean) {
            return (Boolean)ret;
        } else {
            System.err.println(key + " is no Boolean");
            return false;
        }
    }

    /**
     * Gets the integer value for the given key.
     * If the key does not hold an integer, 0 is returned.
     * @param key the key whose value is obtained
     * @return value mapped to the given key
     */
    public int getInt(String key) {
        Object ret = this.get(key);
        if(ret instanceof Number) {
            return ((Number) ret).intValue();
        } else {
            System.err.println(key + " is no Number");
            return 0;
        }
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
     * If the key does not hold a double, 0 is returned.
     * @param key the key whose value is obtained
     * @return value mapped to the given key
     */
    public double getDouble(String key) {
        Object ret = this.get(key);
        if(ret instanceof Number) {
            return ((Number)ret).doubleValue();
        } else {
            System.err.println(key + " is no Number");
            return 0;
        }
    }

    /**
     * Gets the string value for the given key.
     * If the key does not hold a string, an empty string is returned.
     * @param key the key whose value is obtained
     * @return value mapped to the given key
     */
    public String getString(String key) {
        Object ret = this.get(key);
        if(ret instanceof String) {
            return (String)ret;
        } else {
            System.err.println(key + " is no String");
            return "";
        }
    }

    /**
     * Gets the JSONObject mapped to the given key.
     * If the key does not hold a json object, an empty json object is returned.
     * @param key the key whose value is obtained
     * @return value mapped to the given key
     */
    public JSONObject getJSONObject(String key) {
        Object ret = this.get(key);
        if(ret instanceof org.json.simple.JSONObject) {
            return new JSONObject((org.json.simple.JSONObject)this.get(key));
        } else {
            System.err.println(key + " is no org.json.simple.JSONObject");
            return new JSONObject("{}");
        }
    }

    /**
     * Gets the JSONArray mapped to the given key.
     * If the key does not hold a json array, an empty json array is returned.
     * @param key the key whose value is obtained
     * @return value mapped to the given key
     */
    public JSONArray getJSONArray(String key) {
        Object ret = this.get(key);
        if(ret instanceof org.json.simple.JSONArray) {
            return new JSONArray((org.json.simple.JSONArray)this.get(key));
        } else {
            System.err.println(key + " is no org.json.simple.JSONArray");
            return new JSONArray("[]");
        }
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
