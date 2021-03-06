/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.github.dmgorbunov.combiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Combiner<T>  {
    private boolean shuffle = true;
    private final Supplier<T> instantiator;
    private final List<Consumer<T>> activators;
    private List<List<Consumer<T>>> dataset = new ArrayList<>();

    private Combiner(Supplier<T> instantiator) {
        this.instantiator = instantiator;
        this.activators = new ArrayList<>();
    }

    /**
     * @param instantiator Combiner constructor
     * @param <T> Test data subtype
     * @return Builder of a certain data object subtype
     */
    public static <T> Combiner<T> of(Supplier<T> instantiator) {
        return new Combiner<T>(instantiator);
    }

    public final <U> Combiner<T> with(BiConsumer<T,U> consumer, U value) {
        activators.add(o -> consumer.accept(o, value));
        return this;
    }

    /**
     * @param consumer Method of test object class that accepts the provided value(s), e.g. Object::setFieldValue
     *                 This method will be applied to all provided fields during test data set generation.
     * @param values One or more fields to generate subsets from.
     * @param <U> Type of value
     * @return Builder instance
     */
    @SafeVarargs
    public final <U> Combiner<T> add(BiConsumer<T, U> consumer, U... values) {
        List<Consumer<T>> instanceModifiers =
                Stream.of(values)
                        .map(u -> (Consumer<T>) instance -> consumer.accept(instance, u))
                        .collect(Collectors.toList());
        if (shuffle) {
            Collections.shuffle(instanceModifiers);
        }
        dataset.add(instanceModifiers);
        return this;
    }
    public final <U> Combiner<T> add(BiConsumer<T, U> consumer, List<U> values) {
        return add(consumer, (U[])values.toArray());
    }
    @SafeVarargs
    public final <U> Combiner<T> withCollection(BiConsumer<T, U[]> consumer, U... values) {
        dataset.add(Collections.singletonList(u -> consumer.accept(u, values)));
        return this;
    }
    public <U> Combiner<T> withCollection(BiConsumer<T, List<U>> consumer, List<U> values) {
        dataset.add(Collections.singletonList(u -> consumer.accept(u, values)));
        return this;
    }
    /**
     * @param shuffle Disables or enables incoming data shuffling.
     *                Enabled by default to avoid pesticide paradox problems.
     * @return Builder instance
     */
    public Combiner<T> setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
        return this;
    }

    private T getValue() {
        T t = instantiator.get();
        activators.forEach(a -> a.accept(t));
        return t;
    }

    /**
     * @return single instance of a typed data object with random fields from passed parameters
     */
    public T build() {
        T value = getValue();
        dataset.stream()
                .map(s -> s.get(new Random().nextInt(s.size())))
                .forEach(m -> m.accept(value));
        return value;
    }

    public List<T> buildSet() {
        return buildSet(Strategy.OPTIMIZED);
    }

    public List<T> buildSet(Strategy strategy) {
        return strategy == Strategy.OPTIMIZED ? buildOptimizedSet() : buildFullSet();
    }

    /**
     * @return Full cartesian product of given parameters.
     * Warning: THERE WILL BE A LOT OF OBJECTS THEN.
     */
    private List<T> buildFullSet() {
        return cartesianProduct(dataset).stream().map(cl -> {
            T t = getValue();
            cl.forEach(c -> c.accept(t));
            return t;
        }).collect(Collectors.toList());
    }

    private static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        List<List<T>> currentCombinations = Collections.singletonList(Collections.emptyList());
        for (List<T> list : lists) {
            currentCombinations = appendElements(currentCombinations, list);
        }
        return currentCombinations;
    }

    private static <T> List<List<T>> appendElements(List<List<T>> combinations, List<T> extraElements) {
        return combinations.stream().flatMap(oldCombination ->
                extraElements.stream().map(extra -> {
                    List<T> combinationWithExtra = new ArrayList<>(oldCombination);
                    combinationWithExtra.add(extra);
                    return combinationWithExtra;
                }))
                .collect(Collectors.toList());
    }

    /**
     * Calculates the maximum length of parameter set (N).
     * Generates N unique data sets to build data objects from.
     * @return List of N typed objects
     */
    public List<T> buildOptimizedSet() {
        List<T> result = new ArrayList<>();
        int depth = dataset.stream().mapToInt(List::size).max().orElse(dataset.size());
        for (int i = 0; i<depth; i++) {
            List<Consumer<T>> newSubset = new ArrayList<>();
            for (List<Consumer<T>> subset : dataset) {
                newSubset.add(subset.get( i < subset.size() ? i : i % subset.size()));
            }
            T value = getValue();
            newSubset.forEach(m -> m.accept(value));
            result.add(value);
        }
        return result;
    }

    public enum Strategy {
        FULL,
        OPTIMIZED;
    }
}
