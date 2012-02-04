package ca.cutterslade.utilities;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.ComputationException;
import com.google.common.io.Resources;

public enum BuiltinFunctions implements Function<Iterable<String>, String> {
  ECHO {

    @Override
    public String apply(final Iterable<String> input) {
      final String result;
      if (null == input) {
        result = null;
      }
      else {
        final StringBuilder builder = new StringBuilder();
        for (final String s : input) {
          builder.append(s).append(' ');
        }
        if (builder.length() > 0) {
          builder.setLength(builder.length() - 1);
        }
        result = builder.toString();
      }
      return result;
    }
  },
  READ {

    @Override
    public String apply(final Iterable<String> input) {
      final String result;
      if (null == input) {
        result = null;
      }
      else {
        final StringBuilder builder = new StringBuilder();
        for (final String resource : input) {
          try {
            builder.append(Resources.toString(Resources.getResource(resource), Charsets.UTF_8));
          }
          catch (final IOException e) {
            throw new ComputationException(e);
          }
        }
        result = builder.toString();
      }
      return result;
    }

  },
}
