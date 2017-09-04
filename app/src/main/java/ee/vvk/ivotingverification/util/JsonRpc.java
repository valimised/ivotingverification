package ee.vvk.ivotingverification.util;

import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class JsonRpc {
    private static final String TAG = "JSON-RPC";
    private static final String DEVICE_INFO = String.format("%s,%s,%s (%s)",
            "Android", Build.VERSION.SDK_INT, Build.MODEL, Build.PRODUCT);

    public enum Method {
        VERIFY("Verify");

        private final String method;
        Method(String method) {
            this.method = method;
        }

        public String toString() {
            return String.format("%s.%s", "RPC", method);
        }
    }

    private static final String KEY_REQUEST_METHOD = "method";
    private static final String KEY_REQUEST_PARAMETERS = "params";
    private static final String KEY_RESPONSE_RESULT = "result";
    private static final String KEY_RESPONSE_ERROR = "error";
    private static final String KEY_ID = "id";
    private static final String KEY_DEVICE_INFO = "os";
    private static final int VALUE_REQUEST_ID = 1;

    public static ByteBuffer createRequest(Method method, Map<String, Object> params)
            throws JSONException{
        params.put(KEY_DEVICE_INFO, DEVICE_INFO);
        JSONObject rootObj = new JSONObject();

        JSONArray paramArray = new JSONArray();
        paramArray.put(new JSONObject(params));

        rootObj.put(KEY_REQUEST_METHOD, method.toString());
        rootObj.put(KEY_REQUEST_PARAMETERS, paramArray);
        rootObj.put(KEY_ID, VALUE_REQUEST_ID);
        if (Util.DEBUGGABLE) {
            Log.d(TAG, rootObj.toString(2));
        }
        return ByteBuffer.wrap(rootObj.toString().getBytes());
    }

    public static Response unmarshalResponse(Method method, InputStream in) throws Exception {
        Map<String, Object> rMap = null;
        String error = null;

        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case KEY_ID:
                    if (reader.nextInt() != VALUE_REQUEST_ID) {
                        throw new Exception("Response ID does not equal request ID");
                    }
                    break;
                case KEY_RESPONSE_RESULT:
                    if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull();
                        rMap = null;
                    } else {
                        switch (method) {
                            case VERIFY:
                                rMap = readVerifyResultToMap(reader);
                                break;
                        }
                    }
                    break;
                case KEY_RESPONSE_ERROR:
                    if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull();
                        error = null;
                    } else {
                        error = reader.nextString();
                    }
                    break;
                default:
                    throw new Exception("Unexpected server response json key: " + name);
            }
        }

        if (!(rMap != null && error == null || rMap == null && error != null)) {
            throw new Exception("Response result and error objects are both either null or non-null");
        }

        return new Response(rMap, error);
    }

    private static Map<String, Object> readVerifyResultToMap(JsonReader reader) throws IOException {
        Map<String, Object> res = new HashMap<>();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "SessionID":
                case "Type":
                case "Vote":
                    res.put(name, reader.nextString());
                    break;
                case "Qualification":
                    reader.beginObject();
                    while (reader.hasNext()) {
                        name = reader.nextName();
                        switch (name) {
                            case "ocsp":
                            case "tspreg":
                                res.put(name, reader.nextString());
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Unexpected json key in RPC.Verify response: " + name);
                        }
                    }
                    reader.endObject();
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unexpected json key in RPC.Verify response: " + name);
            }

        }
        reader.endObject();
        return res;
    }

    public static class Response {
        public final Map<String, ?> result;
        public final String error;

        private Response(Map<String, ?> result, String error) {
            this.result = result;
            this.error = error;
        }
    }
}
