package protocol;

import com.google.gson.JsonElement;

public class Request {
    public String jsonrpc;
    public Integer id;
    public String method;
    public JsonElement params;
}
