package ca.cutterslade.utilities;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Enums;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class PropertiesResolver {

  private static final Pattern REPLACE_PATTERN = Pattern.compile("\\$\\{([^}{]+)\\}");

  private static final Pattern REPLACE_KEY_PATTERN =
      Pattern.compile("(\\$)?([^$}{:()]+)(?:\\(([^$}{()]+)\\))?(?::(.*))?");

  private static final int REPLACE_KEY_PATTERN_COMPUTE_GROUP = 1;

  private static final int REPLACE_KEY_PATTERN_KEY_GROUP = 2;

  private static final int REPLACE_KEY_PATTERN_ARGUMENTS_GROUP = 3;

  private static final int REPLACE_KEY_PATTERN_DEFAULT_GROUP = 4;

  private static final Pattern LITERAL_PATTERN = Pattern.compile("'([^}{$]+)'");

  private final ImmutableMap<String, String> source;

  private final Map<String, String> resolved = Maps.newHashMap();

  private final Set<String> pending = Sets.newHashSet();

  public PropertiesResolver(final Map<String, String> properties) {
    this.source = ImmutableMap.copyOf(properties);
  }

  public PropertiesResolver(final Iterable<? extends Map<String, String>> sources) {
    final List<Map<String, String>> sourceList = Lists.newArrayList(sources);
    Collections.reverse(sourceList);
    final Map<String, String> collected = Maps.newHashMap();
    for (final Map<String, String> propSource : sourceList) {
      collected.putAll(propSource);
    }
    source = ImmutableMap.copyOf(collected);
  }

  public ImmutableMap<String, String> getResolved() {
    for (final String key : source.keySet()) {
      resolved.put(key, getValue(key));
    }
    return ImmutableMap.copyOf(resolved);
  }

  private String getValue(final String key) {
    String value = resolved.get(key);
    if (null == value) {
      value = source.get(key);
      if (null != value) {
        Preconditions.checkState(pending.add(key), "Circular reference found on key %s", key);
        final Matcher matcher = REPLACE_PATTERN.matcher(value);
        final StringBuffer result = new StringBuffer();
        while (matcher.find()) {
          matcher.appendReplacement(result, Matcher.quoteReplacement(getReplacement(matcher.group(1))));
        }
        matcher.appendTail(result);
        value = result.toString();
        resolved.put(key, value);
        pending.remove(key);
      }
    }
    return value;
  }

  private String getReplacement(final String replaceKey) {
    final Matcher matcher = REPLACE_KEY_PATTERN.matcher(replaceKey);
    Preconditions.checkArgument(matcher.matches(), "Replacement key %s does not match expected pattern %s",
        replaceKey, REPLACE_KEY_PATTERN.pattern());
    final String replacement;
    final boolean compute = null != matcher.group(REPLACE_KEY_PATTERN_COMPUTE_GROUP);
    final String key = matcher.group(REPLACE_KEY_PATTERN_KEY_GROUP);
    final String value = compute ? null : getValue(key);
    final String arguments = matcher.group(REPLACE_KEY_PATTERN_ARGUMENTS_GROUP);
    final String dflt = matcher.group(REPLACE_KEY_PATTERN_DEFAULT_GROUP);
    if (compute) {
      replacement = getComputedReplacement(key, arguments, dflt);
    }
    else if (null == value && null == dflt) {
      replacement = "${" + replaceKey + "}";
    }
    else if (null == value) {
      replacement = dflt;
    }
    else {
      replacement = value;
    }
    return replacement;
  }

  private String getComputedReplacement(final String method, final String arguments, final String dflt) {
    final String[] args = arguments.split(",");
    final ImmutableList.Builder<String> builder = ImmutableList.builder();
    for (final String arg : args) {
      final Matcher matcher = LITERAL_PATTERN.matcher(arg);
      if (matcher.matches()) {
        builder.add(matcher.group(1));
      }
      else {
        builder.add(getReplacement(arg));
      }
    }
    return getComputedReplacement(method, builder.build(), dflt);
  }

  private String getComputedReplacement(final String methodName, final ImmutableList<String> arguments,
      final String dflt) {
    final String result = getComputeMethod(methodName, arguments.size()).apply(arguments);
    return null == result ? dflt : result;
  }

  private enum Resolver {
    BUILTIN_RESOLVER {

      @Override
      public Function<Iterable<String>, String> getMethod(final String methodName, final int argumentCount) {
        return Enums.valueOfFunction(BuiltinFunctions.class).apply(methodName.toUpperCase(Locale.US));
      }
    },
    FUNCTION_CLASS_RESOLVER {

      @Override
      public Function<Iterable<String>, String> getMethod(final String methodName, final int argumentCount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("getMethod has not been implemented");
      }
    },
    STATIC_METHOD_RESOLVER {

      @Override
      public Function<Iterable<String>, String> getMethod(final String methodName, final int argumentCount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("getMethod has not been implemented");
      }
    },
    STATIC_FIELD_RESOLVER {

      @Override
      public Function<Iterable<String>, String> getMethod(final String methodName, final int argumentCount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("getMethod has not been implemented");
      }
    },
    ENUM_CONSTANT_RESOLVER {

      @Override
      public Function<Iterable<String>, String> getMethod(final String methodName, final int argumentCount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("getMethod has not been implemented");
      }
    };

    public abstract Function<Iterable<String>, String> getMethod(String methodName, int argumentCount);
  }

  private Function<Iterable<String>, String> getComputeMethod(final String methodName, final int argumentCount) {
    Function<Iterable<String>, String> method = null;
    for (final Resolver resolver : Resolver.values()) {
      method = resolver.getMethod(methodName, argumentCount);
      if (null != method) {
        break;
      }
    }
    Preconditions.checkArgument(null != method, "Could not resolve method indicated by '%s'", methodName);
    return method;
  }

}
