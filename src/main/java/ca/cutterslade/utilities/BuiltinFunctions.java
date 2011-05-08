package ca.cutterslade.utilities;

import com.google.common.base.Function;

public enum BuiltinFunctions implements Function<Iterable<String>, String> {
  ECHO {

    @Override
    public String apply(final Iterable<String> input) {
      final StringBuilder builder = new StringBuilder();
      for (final String s : input) {
        builder.append(s).append(' ');
      }
      if (builder.length() > 0) {
        builder.setLength(builder.length() - 1);
      }
      return builder.toString();
    }
  };
}
