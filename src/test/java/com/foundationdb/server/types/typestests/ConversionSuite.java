/**
 * Copyright (C) 2009-2013 FoundationDB, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.foundationdb.server.types.typestests;

import com.foundationdb.server.types.TInstance;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

public final class ConversionSuite<T> {

    public static <T> SuiteBuilder<T> build(LinkedConversion<? super T> converters) {
        return new SuiteBuilder<>(converters);
    }

    public TestCase<?> testCaseAt(int index) {
        return testCases.get(index);
    }

    public ConversionSuite(LinkedConversion<? super T> converters, List<TestCase<? extends T>> testCases) {
        this.testCases = new ArrayList<>(testCases);
        this.converters = converters;
    }

    // for use in this package

    void putAndCheck(int i) {
        TestCase<? extends T> testCase = testCases.get(i);
        converters.setUp(testCase);
        testCase.put(converters.linkedTarget());
        converters.syncConversions();
        if (converters.linkedSource().isNull()) {
            fail("source shouldn't be null: " + converters.linkedSource());
        }
        converters.checkPut(testCase.expectedState());
        testCase.check(converters.linkedSource());
    }

    void targetAlwaysAcceptsNull(int i) {
        TestCase<? extends T> testCase = testCases.get(i);
        converters.setUp(testCase);
        converters.linkedTarget().putNull();
        converters.syncConversions();
        if (!converters.linkedSource().isNull()) {
            fail("source should be null: " + converters.linkedSource());
        }
    }

    void getMismatch(int i) {
        TestCase<? extends T> testCase = testCases.get(i);
        TInstance expectedType = testCase.type();
        converters.setUp(testCase);
        testCase.put(converters.linkedTarget());
        converters.syncConversions();

        TestCase<?> switched = resolveSwitcher(testCase);
        boolean gotError = false;
        try {
            switched.get(converters.linkedSource());
        } catch (WrongValueGetException t) {
            gotError = true;
        }
        if (!gotError) {
            fail(errorMessage("ValueSource", "getting", expectedType, switched));
        }
    }

    void setupUnsupported(int i) {
        TestCase<? extends T> testCase = testCases.get(i);
        converters.setUp(testCase);
    }

    void putUnsupported(int i) {
        TestCase<? extends T> testCase = testCases.get(i);
        testCase.put(converters.linkedTarget());
    }

    void getUnsupported(int i) {
        TestCase<? extends T> testCase = testCases.get(i);
        testCase.get(converters.linkedSource());
    }

    private String errorMessage(String failedClass, String action, TInstance expectedType, TestCase<?> switched) {
        return "expected " + failedClass + " error after " + action + ' ' + switched
                + ": expected check for " + expectedType.toString() + " when " + action + ' ' + switched.type();
    }

    void putMismatch(int i) {
        TestCase<? extends T> testCase = testCases.get(i);
        TInstance expectedType = testCase.type();
        converters.setUp(testCase);

        TestCase<?> switched = resolveSwitcher(testCase);
        boolean gotError = false;
        try {
            switched.put(converters.linkedTarget());
        } catch (WrongValueGetException t) {
            gotError = true;
        }
        if (!gotError) {
            fail(errorMessage("ValueTarget", "putting", expectedType, switched));
        }
    }

    private static TestCase<?> resolveSwitcher(TestCase<?> switcherTestCase) {
        Object state = switcherTestCase.expectedState();
        if (state instanceof MismatchedConversionsSuite.Switcher) {
            return ((MismatchedConversionsSuite.Switcher)state).switchTo();
        }
        throw new UnsupportedOperationException("not a switcher state: " + state);
    }

    List<String> testCaseNames() {
        List<String> names = new ArrayList<>();
        for (TestCase<? extends T> testCase : testCases) {
            names.add(testCase.toString());
        }
        return names;
    }

    LinkedConversion<? super T> linkedConversion() {
        return converters;
    }

    // Object state

    private final List<TestCase<? extends T>> testCases;
    private final LinkedConversion<? super T> converters;

    // nested classes

    public static class SuiteBuilder<T> {

        public ConversionSuite<?> suite() {
            return new ConversionSuite<>(converters, testCases);
        }

        public SuiteBuilder<T> add(TestCase<? extends T> testCase) {
            testCases.add(testCase);
            return this;
        }

        public SuiteBuilder(LinkedConversion<? super T> converters) {
            this.converters = converters;
            this.testCases = new ArrayList<>();
        }

        private final LinkedConversion<? super T> converters;
        private final List<TestCase<? extends T>> testCases;
    }
}
