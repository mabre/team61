package de.hhu.propra.team61.io.json;

import org.json.simple.JSONValue;

/**
 * Wrapper class for org.json.simple.JSONObject providing convenience methods which are available in org.json.JSONObject.
 * <p>
 * The package from <a href="http://json.org/java">json.org</a> is released under the
 * <a href="http://www.json.org/license.html>JSON License</a>, which is
 * <a href="https://www.gnu.org/licenses/license-list.en.html#JSON">incompatible</a> with GPL 3. json-simple is available
 * under Apache License Version 2.0, which is <a href="https://www.gnu.org/licenses/license-list.en.html#apache2">compatible</a>
 * with GPL 3.
 */
@SuppressWarnings("unchecked") // limitation of org.json.simple
public class JSONObject extends org.json.simple.JSONObject {

    public JSONObject() {
        super();
    }

    public JSONObject(String jsonString) {
        super();
        org.json.simple.JSONObject json = (org.json.simple.JSONObject)JSONValue.parse(jsonString);
        this.putAll(json);
    }

    JSONObject(org.json.simple.JSONObject jsonObject) {
        super();
        this.putAll(jsonObject);
    }

    public boolean has(String key) {
        return this.containsKey(key);
    }

    public boolean getBoolean(String key) {
        return (Boolean)this.get(key);
    }

    public int getInt(String key) {
        return ((Number)this.get(key)).intValue();
    }

    public double getDouble(String key) {
        return ((Number)this.get(key)).doubleValue();
    }

    public String getString(String key) {
        return (String)this.get(key);
    }

    public JSONObject getJSONObject(String key) {
        return new JSONObject((org.json.simple.JSONObject)this.get(key));
    }

    public JSONArray getJSONArray(String key) {
        return new JSONArray((org.json.simple.JSONArray)this.get(key));
    }
}
