package protocol;

import com.google.gson.JsonElement;

public class ResponseError {
    public Integer code;
    public String message;
    public JsonElement data;
}
