package ca.cutterslade.utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

import com.google.common.base.Function;
import com.google.common.base.Throwables;

public class ResultSetTransformer<T> implements Iterator<T> {

  public static final Function<ResultSet, Integer> INTEGER_TRANSFORMER = new Function<ResultSet, Integer>() {

    @Override
    public Integer apply(final ResultSet input) {
      try {
        return null == input ? null : input.getInt(1);
      }
      catch (final SQLException e) {
        throw Throwables.propagate(e);
      }
    }
  };

  private final ResultSet result;

  private final Function<ResultSet, T> transformer;

  private boolean alreadyHasNext;

  public ResultSetTransformer(@Nonnull final ResultSet result, @Nonnull final Function<ResultSet, T> transformer) {
    this.result = result;
    this.transformer = transformer;
  }

  @Override
  public boolean hasNext() {
    if (!alreadyHasNext) {
      try {
        alreadyHasNext = result.next();
      }
      catch (final SQLException e) {
        throw Throwables.propagate(e);
      }
    }
    return alreadyHasNext;
  }

  @Override
  public T next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    alreadyHasNext = false;
    return transformer.apply(result);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("remove is not supported");
  }

}
