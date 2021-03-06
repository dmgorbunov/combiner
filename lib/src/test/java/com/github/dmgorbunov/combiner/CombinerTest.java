/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.github.dmgorbunov.combiner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class CombinerTest {

    private final static Logger log = LoggerFactory.getLogger(CombinerTest.class);

    @Test void combinerTest() {

        List<String> valuesB = Arrays.asList("one", "two");

        Combiner<ExampleTestData> builder = Combiner.of(ExampleTestData::new)
                .with(ExampleTestData::setA, "a")
                .add(ExampleTestData::setB, valuesB)
                .add(ExampleTestData::setC, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .add(ExampleTestData::setD, 1.0, 2.0, 365.0);

        List<ExampleTestData> fullSet = builder.buildSet(Combiner.Strategy.FULL);

        log.info("Full set: {}", fullSet.size());
        Assertions.assertEquals(60, fullSet.size(), "Full set size");

        List<ExampleTestData> optimizedSet = builder.buildSet(Combiner.Strategy.OPTIMIZED);
        Assertions.assertEquals(10, optimizedSet.size(), "Optimized set size");

        Assertions.assertTrue(optimizedSet.stream().map(ExampleTestData::getA)
                .allMatch(v -> v.equals("a")));
        
        Assertions.assertTrue(optimizedSet.stream().map(ExampleTestData::getB)
                .collect(Collectors.toList()).containsAll(valuesB));
    }
}
