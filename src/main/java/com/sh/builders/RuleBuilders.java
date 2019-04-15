package com.sh.builders;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class RuleBuilders {
  /**
   * Base rule interface which for a given input producing a matching output
   *
   * @param <T> input type
   * @param <R> output item type
   */
  interface Rule<T, R> {
    Collection<R> match(T input);
  }

  /**
   * RuleBuilder interface, allowing filter condition building and basic value transformation
   *
   * @param <T>
   * @param <R>
   * @param <B>
   */
  interface RuleBuilder<T, R, B extends RuleBuilder<T, R, B>> {
    /**
     * @param condition
     * @return this builder, with merged condition which is AND between the current and the given
     */
    B and(Predicate<T> condition);

    /**
     * @param condition
     * @return this builder, with merged condition which is OR between the current and the given
     */
    B or(Predicate<T> condition);

    /** @return this builder with condition negated */
    B not();

    /**
     * Build a sub-condition on separate subBuilder, merge using AND. Can be used for fluent
     * construction of OR expressions, i.e.
     *
     * <pre>
     *   builder()
     *     .and(this:isSomethnig)
     *     .when(sb -> sb
     *                 .or(this::isOne)
     *                 .or(this::isAnother)
     *     )
     *     .then(value)
     * </pre>
     *
     * @param subConditionBuilder
     * @return this builder with merged condition which is AND between the current and the condition
     *     of another builder
     */
    B when(UnaryOperator<B> subConditionBuilder);

    /**
     * @param values - values to be returned when condition meets
     * @return builds a rule for given values and current condition
     */
    Rule<T, R> then(Collection<R> values);

    /**
     * @param valueFactory - values to be created when condition meets
     * @return builds a rule for given values and current condition
     */
    Rule<T, R> thenInContext(Function<T, Collection<R>> valueFactory);

    /** @return current builder condition */
    Predicate<T> condition();

    /**
     * @return this builder, to avoid typecasts everywhere, as currently it is impossible to cast
     *     (this) safely
     */
    B thisBuilder();

    /**
     * @param value - single value to be returned when condition meets
     * @return builds a rule for given values and current condition
     */
    default Rule<T, R> then(R value) {
      return then(Collections.singleton(value));
    }
  }

  /** Fluent DSL class. Only possible action is to complete condition with a valueSupplier */
  interface PartialSubMatcher<T, R, U> {
    Rule<T, R> thenReturn(Function<U, Stream<R>> valueSupplier);

    default Rule<T, R> thenReturn(Collection<R> valueSupplier) {
      Objects.requireNonNull(valueSupplier);
      return thenReturn(u -> valueSupplier.stream());
    }

    default Rule<T, R> thenReturnSingle(Function<U, R> valueSupplier) {
      Objects.requireNonNull(valueSupplier);
      return thenReturn(u -> Stream.of(valueSupplier.apply(u)));
    }
  }

  /**
   * Basic implementation of RuleBuilder
   *
   * @param <T> - input type
   * @param <R> - ouput item type
   * @param <B> - this builder, for extention
   */
  public abstract static class RuleBuilderImpl<T, R, B extends RuleBuilderImpl<T, R, B>>
      implements RuleBuilder<T, R, B> {
    private Predicate<T> filter;

    RuleBuilderImpl(Predicate<T> initialFilter) {
      this.filter = initialFilter;
    }

    @Override
    public B thisBuilder() {
      return (B) this;
    }

    @Override
    public B and(Predicate<T> condition) {
      filter = filter.and(condition);
      return thisBuilder();
    }

    @Override
    public B or(Predicate<T> condition) {
      filter = filter.or(condition);
      return thisBuilder();
    }

    @Override
    public B not() {
      filter = filter.negate();
      return thisBuilder();
    }

    @Override
    public B when(UnaryOperator<B> subConditionBuilder) {
      B subBuilder = subConditionBuilder.apply(newInstance(false));
      return and(subBuilder.condition());
    }

    /**
     * @param matchAll
     * @return new instance of this builder, used for subCondition creation
     */
    protected abstract B newInstance(boolean matchAll);

    @Override
    public Rule<T, R> then(Collection<R> values) {
      return thenInContext(t -> values);
    }

    @Override
    public Rule<T, R> thenInContext(Function<T, Collection<R>> valueFactory) {
      return t -> filter.test(t) ? valueFactory.apply(t) : Collections.emptyList();
    }

    @Override
    public Predicate<T> condition() {
      return filter;
    }
  }

  /**
   * An extension to RuleBuilder which contains methods to simplify creation of dynamic rules, which
   * require iteration over some sub-objects
   *
   * @param <T> - input type
   * @param <R> - output items
   * @param <B> - this builder
   */
  public abstract static class IteratingRuleBuilderImpl<
          T, R, B extends IteratingRuleBuilderImpl<T, R, B>>
      extends RuleBuilderImpl<T, R, B> {

    IteratingRuleBuilderImpl(Predicate<T> initialFilter) {
      super(initialFilter);
    }

    <U> PartialSubMatcher<T, R, U> forEachMatching(Function<T, Stream<U>> streamFactory) {
      return new PartialSubMatcher<T, R, U>() {
        @Override
        public Rule<T, R> thenReturn(Function<U, Stream<R>> valueSupplier) {
          return forEachMatchingDo(streamFactory, valueSupplier);
        }
      };
    }

    public <U> Rule<T, R> forEachMatchingDo(
        Function<T, Stream<U>> streamFactory, Function<U, Stream<R>> valueSupplier) {
      return thenInContext(
          t -> {
            Collection<R> result =
                streamFactory.apply(t).flatMap(u -> valueSupplier.apply(u)).collect(toList());
            return result;
          });
    }
  }
}
