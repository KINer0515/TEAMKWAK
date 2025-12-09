package engine;

import com.google.gson.Gson;
import io.javalin.json.JsonMapper;

import java.lang.reflect.Type;

public class GsonJsonMapper implements JsonMapper {

    private final Gson gson;

    public GsonJsonMapper(Gson gson) {
        this.gson = gson;
    }

    @Override
    public String toJsonString(java.lang.Object obj, Type type) {
        return gson.toJson(obj);
    }

    @Override
    public <T> T fromJsonString(String json, Type targetType) {
        return gson.fromJson(json, targetType);
    }
}