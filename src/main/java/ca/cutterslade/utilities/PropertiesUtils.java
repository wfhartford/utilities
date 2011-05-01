package ca.cutterslade.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;

public final class PropertiesUtils {

  private static final Logger log = LoggerFactory.getLogger(PropertiesUtils.class);

  private PropertiesUtils() {
    throw new UnsupportedOperationException();
  }

  public static ImmutableMap<String, String> loadProperties(final String resourceName) throws IOException {
    return loadProperties(Resources.getResource(resourceName));
  }

  public static ImmutableMap<String, String> loadProperties(final Class<?> contextClass, final String resourceName) throws IOException {
    return loadProperties(Resources.getResource(contextClass, resourceName));
  }

  public static ImmutableMap<String, String> loadProperties(final URL resource) throws IOException {
    final InputSupplier<InputStream> inputSupplier = Resources.newInputStreamSupplier(resource);
    final Properties properties = new Properties();
    final InputStream stream = inputSupplier.getInput();
    try {
      properties.load(stream);
    }
    finally {
      try {
        stream.close();
      }
      catch (final IOException e) {
        log.warn("Exception closing resource", e);
      }
    }
    return Maps.fromProperties(properties);
  }

  public static ImmutableMap<String, String> systemProperties() {
    return Maps.fromProperties(System.getProperties());
  }

  public static ImmutableMap<String, String> environmentProperties() {
    return ImmutableMap.copyOf(System.getenv());
  }

  public static ImmutableMap<String, String> resolveProperties(final Map<String, String> properties) {
    return new PropertiesResolver(properties).getResolved();
  }

  public static ImmutableMap<String, String> resolveProperties(final Map<String, String>... properties) {
    return new PropertiesResolver(Arrays.asList(properties)).getResolved();
  }

}