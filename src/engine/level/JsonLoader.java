package engine.level;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple, dependency-free JSON parser specifically designed to load level data
 * from the game's map file. This avoids the need for external libraries like Gson,
 * which is beneficial for large teams where managing dependencies is complex.
 *
 * NOTE: This is not a full-featured JSON parser. It is brittle and will likely
 * fail if the JSON structure deviates from the expected format in maps.json.
 */
@SuppressWarnings("unchecked")
public class JsonLoader {

    private final String json;
    private int at;
    private char ch;

    private JsonLoader(String json) {
        this.json = json;
        this.at = 0;
        this.ch = json.length() > 0 ? json.charAt(0) : '\0';
    }

    /**
     * Public entry point to parse the level data from a JSON string.
     * @param jsonContent The raw string content of the JSON file.
     * @return A list of Level objects.
     * @throws IOException if parsing fails.
     */
    public static List<Level> parse(String jsonContent) throws IOException {
        try {
            Map<String, Object> root = (Map<String, Object>) new JsonLoader(jsonContent).parseValue();
            if (root == null || !root.containsKey("levels")) {
                throw new IOException("JSON root must be an object with a 'levels' key.");
            }
            List<Map<String, Object>> levelMaps = (List<Map<String, Object>>) root.get("levels");

            List<Level> levels = new ArrayList<>();
            for (Map<String, Object> map : levelMaps) {
                levels.add(new Level(map));
            }
            return levels;
        } catch (Exception e) {
            // Wrap parsing exceptions into IOException to signal failure to the caller.
            throw new IOException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Public entry point to parse a generic JSON string into a Map.
     * @param jsonContent The raw string content of the JSON file.
     * @return A map representing the JSON object.
     * @throws IOException if parsing fails.
     */
    public static Map<String, Object> parseGeneric(String jsonContent) throws IOException {
        try {
            return (Map<String, Object>) new JsonLoader(jsonContent).parseValue();
        } catch (Exception e) {
            throw new IOException("Failed to parse generic JSON: " + e.getMessage(), e);
        }
    }

    private char next() {
        if (++at >= json.length()) {
            ch = '\0'; // End of file
            return ch;
        }
        ch = json.charAt(at);
        return ch;
    }

    private void skipWhitespace() {
        while (ch != '\0' && ch <= ' ') {
            next();
        }
    }

    private Object parseValue() {
        skipWhitespace();
        switch (ch) {
            case '{':
                return parseObject();
            case '[':
                return parseArray();
            case '"':
                return parseString();
            case 't':
            case 'f':
                return parseBoolean();
            case 'n':
                return parseNull();
            default:
                if (ch == '-' || (ch >= '0' && ch <= '9')) {
                    return parseNumber();
                }
                throw new RuntimeException("Unexpected character: " + ch);
        }
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> object = new LinkedHashMap<>();
        if (ch != '{') throw new RuntimeException("Object must start with '{'");
        next(); // consume '{'
        skipWhitespace();

        if (ch == '}') {
            next(); // consume '}'
            return object; // empty object
        }

        while (ch != '\0') {
            String key = parseString();
            skipWhitespace();
            if (ch != ':') throw new RuntimeException("Expected ':' after key in object");
            next(); // consume ':'
            
            object.put(key, parseValue());
            skipWhitespace();

            if (ch == '}') {
                next(); // consume '}'
                return object;
            }
            if (ch != ',') throw new RuntimeException("Expected ',' or '}' in object");
            next(); // consume ','
            skipWhitespace();
        }
        throw new RuntimeException("Unclosed object");
    }

    private List<Object> parseArray() {
        List<Object> array = new ArrayList<>();
        if (ch != '[') throw new RuntimeException("Array must start with '['");
        next(); // consume '['
        skipWhitespace();

        if (ch == ']') {
            next(); // consume ']'
            return array; // empty array
        }

        while (ch != '\0') {
            array.add(parseValue());
            skipWhitespace();

            if (ch == ']') {
                next(); // consume ']'
                return array;
            }
            if (ch != ',') throw new RuntimeException("Expected ',' or ']' in array");
            next(); // consume ','
            skipWhitespace();
        }
        throw new RuntimeException("Unclosed array");
    }

    private String parseString() {
        if (ch != '"') throw new RuntimeException("String must start with '\"'");
        StringBuilder sb = new StringBuilder();
        next(); // consume '"'
        while (ch != '\0' && ch != '"') {
            if (ch == '\\') {
                next(); // consume '\'backslash
                // Handle basic escape sequences
                switch (ch) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(ch); break;
                }
            } else {
                sb.append(ch);
            }
            next();
        }
        if (ch != '"') throw new RuntimeException("Unclosed string");
        next(); // consume '"'
        return sb.toString();
    }

    private Number parseNumber() {
        StringBuilder sb = new StringBuilder();
        boolean isDouble = false;
        while (ch == '-' || (ch >= '0' && ch <= '9') || ch == '.') {
            if (ch == '.') {
                isDouble = true;
            }
            sb.append(ch);
            next();
        }
        String numberStr = sb.toString();
        try {
            if (isDouble) {
                return Double.parseDouble(numberStr);
            } else {
                return Long.parseLong(numberStr); // Use Long to avoid overflow, then cast later
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number format: " + numberStr);
        }
    }

    private Boolean parseBoolean() {
        skipWhitespace();
        String boolStr = "";
        if (ch == 't') {
            for (int i = 0; i < 4; i++) { boolStr += ch; if (i < 3) next(); }
            if (boolStr.equals("true")) { next(); return true; }
        } else if (ch == 'f') {
            for (int i = 0; i < 5; i++) { boolStr += ch; if (i < 4) next(); }
            if (boolStr.equals("false")) { next(); return false; }
        }
        throw new RuntimeException("Invalid boolean literal");
    }

    private Object parseNull() {
        skipWhitespace();
        String nullStr = "";
        if (ch == 'n') {
            for (int i = 0; i < 4; i++) { nullStr += ch; if (i < 3) next(); }
            if (nullStr.equals("null")) { next(); return null; }
        }
        throw new RuntimeException("Invalid null literal");
    }
}
