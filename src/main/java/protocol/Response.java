package protocol;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonToken;

public class Response {
    public String jsonrpc;
    public Integer id;
    public JsonElement result;
    public ResponseError error;
}
