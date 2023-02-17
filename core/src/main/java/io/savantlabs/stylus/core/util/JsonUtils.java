package io.savantlabs.stylus.core.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public final class JsonUtils {

  private static final ObjectMapper OM = new ObjectMapper();

  /**
   * Pretty print a json object
   *
   * @param value
   * @return
   */
  @SneakyThrows
  public static String pprint(Object value) {
    String serialized = null;
    if (value != null) {
      serialized = OM.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }
    return serialized;
  }

  @SneakyThrows
  public static String serialize(Object value) {
    String serialized = null;
    if (value != null) {
      serialized = OM.writeValueAsString(value);
    }
    return serialized;
  }

  public static JsonNode toTree(Object value) {
    JsonNode serialized = null;
    if (value != null) {
      serialized = OM.valueToTree(value);
    }
    return serialized;
  }

  @SneakyThrows
  public static <T> T deserialize(InputStream is, Class<T> valClz) {
    T value = null;
    if (is != null) {
      try (is) {
        value = OM.readValue(is, valClz);
      }
    }
    return value;
  }

  @SneakyThrows
  public static <T> T deserialize(JsonNode json, Class<T> valClz) {
    T value = null;
    if (json != null && !json.isNull()) {
      value = OM.treeToValue(json, valClz);
    }
    return value;
  }

  @SneakyThrows
  public static <T> T deserialize(String str, Class<T> valClz) {
    T value = null;
    if (StringUtils.isNotBlank(str)) {
      value = OM.readValue(str, valClz);
    }
    return value;
  }

  @SneakyThrows
  public static <T> List<T> deserializeList(String str, Class<T> valClz) {
    List<T> list = null;
    if (StringUtils.isNotBlank(str)) {
      List<?> rawList = OM.readValue(str, List.class);
      list = rawList.stream().map(obj -> OM.convertValue(obj, valClz)).collect(Collectors.toList());
    }
    return list;
  }

  @SneakyThrows
  public static <T> List<T> deserializeList(JsonNode json, Class<T> valClz) {
    List<T> list = null;
    if (json != null && !json.isNull()) {
      List<?> rawList = OM.treeToValue(json, List.class);
      list = rawList.stream().map(obj -> OM.convertValue(obj, valClz)).collect(Collectors.toList());
    }
    return list;
  }

  @SneakyThrows
  public static <K, V> Map<K, V> deserializeMap(String str, Class<K> keyClz, Class<V> valClz) {
    Map<K, V> map = null;
    if (StringUtils.isNotBlank(str)) {
      Map<?, ?> rawMap = OM.readValue(str, Map.class);
      map = new HashMap<>();
      for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
        K key = OM.convertValue(entry.getKey(), keyClz);
        V value = OM.convertValue(entry.getValue(), valClz);
        map.put(key, value);
      }
    }
    return map;
  }

  @SneakyThrows
  public static <K, V> Map<K, V> deserializeMap(JsonNode json, Class<K> keyClz, Class<V> valClz) {
    Map<K, V> map = null;
    if (json != null && !json.isNull()) {
      Map<?, ?> rawMap = OM.treeToValue(json, Map.class);
      map = new HashMap<>();
      for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
        K key = OM.convertValue(entry.getKey(), keyClz);
        V value = OM.convertValue(entry.getValue(), valClz);
        map.put(key, value);
      }
    }
    return map;
  }

  @SneakyThrows
  public static <K, V> List<Pair<K, V>> deserializeMapToPairs(
      String str, Class<K> keyClz, Class<V> valClz) {
    List<Pair<K, V>> pairs = null;
    if (StringUtils.isNotBlank(str)) {
      Map<?, ?> rawMap = OM.readValue(str, Map.class);
      pairs = new ArrayList<>();
      for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
        K key = OM.convertValue(entry.getKey(), keyClz);
        V value = OM.convertValue(entry.getValue(), valClz);
        pairs.add(Pair.of(key, value));
      }
    }
    return pairs;
  }

  @SneakyThrows
  public static <K, V> List<Pair<K, V>> deserializeMapToPairs(
      JsonNode json, Class<K> keyClz, Class<V> valClz) {
    List<Pair<K, V>> pairs = null;
    if (json != null && !json.isNull()) {
      pairs = new ArrayList<>();
      Map<?, ?> rawMap = OM.treeToValue(json, Map.class);

      for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
        K key = OM.convertValue(entry.getKey(), keyClz);
        V value = OM.convertValue(entry.getValue(), valClz);
        pairs.add(Pair.of(key, value));
      }
    }
    return pairs;
  }

  public static Optional<JsonNode> tryGetNode(JsonNode root, String... path) {
    if (root.has(path[0])) {
      if (path.length == 1) {
        return Optional.ofNullable(root.get(path[0]));
      } else {
        return tryGetNode(root.get(path[0]), ArrayUtils.subarray(path, 1, path.length));
      }
    } else {
      return Optional.empty();
    }
  }

  public static Object toObject(JsonNode jsonNode) {
    Object obj;
    if (jsonNode == null || jsonNode.isNull()) {
      obj = null;
    } else if (jsonNode.isLong()) {
      obj = jsonNode.asLong();
    } else if (jsonNode.isInt()) {
      obj = jsonNode.asInt();
    } else if (jsonNode.isFloat()) {
      obj = jsonNode.asDouble();
    } else if (jsonNode.isDouble()) {
      obj = jsonNode.asDouble();
    } else if (jsonNode.isBoolean()) {
      obj = jsonNode.asBoolean();
    } else if (jsonNode.isTextual()) {
      obj = jsonNode.asText();
    } else {
      throw new IllegalArgumentException(
          "Does not know how to convert json node of type ["
              + jsonNode.getNodeType()
              + "] to an object.");
    }
    return obj;
  }

  public static ObjectMapper getObjectMapper() {
    return OM;
  }
}
