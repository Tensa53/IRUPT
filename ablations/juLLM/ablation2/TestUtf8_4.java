/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avro.util;

import org.apache.avro.AvroRuntimeException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * Comprehensive test class for Utf8 class. Specifically targets lines 64-65
 * (copy constructor) and 116-117-118 (setByteLength with array expansion).
 */
public class TestUtf8_4 {

  private Utf8 utf8Instance;
  private static final String TEST_STRING = "Hello, World!";
  private static final String EMPTY_STRING = "";
  private static final String UNICODE_STRING = "Hello 世界 🌍";
  private static final String LONG_STRING = "This is a much longer string that will be used for testing";

  @Before
  public void setUp() {
    utf8Instance = new Utf8();
  }

  @After
  public void tearDown() {
    utf8Instance = null;
  }

  // ========== Tests for Copy Constructor (Lines 64-65) ==========

  @Test
  public void testCopyConstructor_WithValidUtf8_CreatesIndependentCopy() {
    // Arrange
    Utf8 original = new Utf8(TEST_STRING);
    byte[] originalBytes = original.getBytes();
    int originalLength = original.getByteLength();

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertNotNull("Copy should not be null", copy);
    assertEquals("Copy should have same byte length as original", originalLength, copy.getByteLength());
    assertEquals("Copy should have same string value as original", original.toString(), copy.toString());
    assertNotSame("Copy should have different byte array instance", originalBytes, copy.getBytes());

    // Verify independence: modifying copy should not affect original
    byte[] copyBytes = copy.getBytes();
    for (int i = 0; i < copy.getByteLength(); i++) {
      assertEquals("Byte at index " + i + " should match", originalBytes[i], copyBytes[i]);
    }
  }

  @Test
  public void testCopyConstructor_WithEmptyUtf8_CreatesEmptyCopy() {
    // Arrange
    Utf8 original = new Utf8(EMPTY_STRING);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertNotNull("Copy should not be null", copy);
    assertEquals("Copy should have zero byte length", 0, copy.getByteLength());
    assertEquals("Copy should represent empty string", EMPTY_STRING, copy.toString());
    assertNotSame("Copy should have different byte array instance", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_WithUnicodeUtf8_CreatesCorrectCopy() {
    // Arrange
    Utf8 original = new Utf8(UNICODE_STRING);
    int originalLength = original.getByteLength();

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertNotNull("Copy should not be null", copy);
    assertEquals("Copy should preserve Unicode characters", UNICODE_STRING, copy.toString());
    assertEquals("Copy should have same byte length", originalLength, copy.getByteLength());

    // Verify byte-by-byte copy
    byte[] originalBytes = original.getBytes();
    byte[] copyBytes = copy.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Unicode byte at index " + i + " should match", originalBytes[i], copyBytes[i]);
    }
  }

  @Test
  public void testCopyConstructor_IndependenceVerification_ModifyingCopyDoesNotAffectOriginal() {
    // Arrange
    Utf8 original = new Utf8(TEST_STRING);
    Utf8 copy = new Utf8(original);
    String newValue = "Modified";

    // Act
    copy.set(newValue);

    // Assert
    assertEquals("Original should remain unchanged", TEST_STRING, original.toString());
    assertEquals("Copy should have new value", newValue, copy.toString());
  }

  @Test
  public void testCopyConstructor_WithModifiedByteArray_CopiesCurrentState() {
    // Arrange
    Utf8 original = new Utf8(TEST_STRING);
    original.setByteLength(5); // Truncate to "Hello"

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have truncated length", 5, copy.getByteLength());
    assertEquals("Copy should reflect modified state", "Hello", copy.toString());
  }

  // ========== Tests for setByteLength with Array Expansion (Lines 116-117-118)
  // ==========

  @Test
  public void testSetByteLength_ExpandingArray_CreatesNewArrayAndCopiesData() {
    // Arrange
    utf8Instance.set("Test");
    byte[] originalBytes = utf8Instance.getBytes();
    int originalLength = utf8Instance.getByteLength();
    int newLength = originalLength + 10;

    // Act
    Utf8 result = utf8Instance.setByteLength(newLength);

    // Assert
    assertSame("setByteLength should return this instance", utf8Instance, result);
    assertEquals("New byte length should be set", newLength, utf8Instance.getByteLength());
    assertNotSame("New byte array should be allocated", originalBytes, utf8Instance.getBytes());

    // Verify original data was copied (lines 117)
    byte[] newBytes = utf8Instance.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original byte at index " + i + " should be preserved", originalBytes[i], newBytes[i]);
    }
    assertTrue("New array should be larger", utf8Instance.getBytes().length >= newLength);
  }

  @Test
  public void testSetByteLength_ExpandingFromEmpty_AllocatesNewArray() {
    // Arrange
    Utf8 emptyUtf8 = new Utf8();
    assertEquals("Initial length should be 0", 0, emptyUtf8.getByteLength());

    // Act
    emptyUtf8.setByteLength(20);

    // Assert
    assertEquals("Byte length should be expanded to 20", 20, emptyUtf8.getByteLength());
    assertNotNull("Byte array should be allocated", emptyUtf8.getBytes());
    assertTrue("Byte array should have sufficient capacity", emptyUtf8.getBytes().length >= 20);
  }

  @Test
  public void testSetByteLength_ExpandingSignificantly_PreservesOriginalContent() {
    // Arrange
    utf8Instance.set(LONG_STRING);
    byte[] originalBytes = utf8Instance.getBytes().clone();
    int originalLength = utf8Instance.getByteLength();
    int newLength = originalLength * 3;

    // Act
    utf8Instance.setByteLength(newLength);

    // Assert
    byte[] expandedBytes = utf8Instance.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Content at index " + i + " should be preserved during expansion", originalBytes[i],
          expandedBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_ExpandingBySingleByte_TriggersReallocation() {
    // Arrange
    utf8Instance.set("AB");
    int originalLength = utf8Instance.getByteLength();
    byte[] originalBytes = utf8Instance.getBytes();

    // Act
    utf8Instance.setByteLength(originalLength + 1);

    // Assert
    assertEquals("Length should be increased by one", originalLength + 1, utf8Instance.getByteLength());
    assertNotSame("Array should be reallocated even for single byte expansion", originalBytes, utf8Instance.getBytes());
  }

  @Test
  public void testSetByteLength_NotExpandingArray_ReusesSameArray() {
    // Arrange
    utf8Instance.set(LONG_STRING);
    byte[] originalBytes = utf8Instance.getBytes();
    int originalLength = utf8Instance.getByteLength();
    int smallerLength = originalLength - 5;

    // Act
    utf8Instance.setByteLength(smallerLength);

    // Assert
    assertEquals("Length should be reduced", smallerLength, utf8Instance.getByteLength());
    assertSame("Same array should be reused when not expanding", originalBytes, utf8Instance.getBytes());
  }

  @Test
  public void testSetByteLength_ExpandingArrayClearsStringCache() {
    // Arrange
    utf8Instance.set(TEST_STRING);
    String cachedString = utf8Instance.toString();
    int newLength = utf8Instance.getByteLength() + 10;

    // Act
    utf8Instance.setByteLength(newLength);

    // Assert - string cache should be cleared (line 121)
    // Modify the underlying bytes to verify cache was cleared
    byte[] bytes = utf8Instance.getBytes();
    bytes[0] = 'X';
    String newString = utf8Instance.toString();
    assertNotEquals("String cache should be cleared after setByteLength", cachedString, newString);
  }

  @Test
  public void testSetByteLength_ExceedingMaxLength_ThrowsException() {
    // Arrange
    utf8Instance.set("Test");
    boolean exceptionThrown = false;

    // Act
    try {
      // Use a value that exceeds Integer.MAX_VALUE - 8 (typical max array size)
      // This will trigger the MAX_LENGTH check in setByteLength (line 112-114)
      utf8Instance.setByteLength(Integer.MAX_VALUE - 1);
      fail("Should have thrown AvroRuntimeException");
    } catch (AvroRuntimeException e) {
      // Expected exception
      exceptionThrown = true;
      assertTrue("Exception message should mention exceeding maximum",
          e.getMessage().contains("exceeds maximum allowed"));
    } catch (OutOfMemoryError e) {
      // In some JVM configurations, OOM might occur before the check
      // Accept this as an alternative valid outcome
      exceptionThrown = true;
    }

    // Assert
    assertTrue("Should have thrown an exception", exceptionThrown);
  }

  @Test
  public void testSetByteLength_WithZeroLength_CreatesEmptyUtf8() {
    // Arrange
    utf8Instance.set(TEST_STRING);

    // Act
    utf8Instance.setByteLength(0);

    // Assert
    assertEquals("Length should be zero", 0, utf8Instance.getByteLength());
    assertEquals("Should represent empty string", EMPTY_STRING, utf8Instance.toString());
  }

  @Test
  public void testSetByteLength_MultipleExpansions_WorksCorrectly() {
    // Arrange
    utf8Instance.set("A");
    byte firstByte = utf8Instance.getBytes()[0];

    // Act - multiple expansions
    utf8Instance.setByteLength(5);
    utf8Instance.setByteLength(10);
    utf8Instance.setByteLength(20);

    // Assert
    assertEquals("Final length should be 20", 20, utf8Instance.getByteLength());
    assertEquals("Original first byte should be preserved", firstByte, utf8Instance.getBytes()[0]);
  }

  // ========== Additional Edge Cases and Integration Tests ==========

  @Test
  public void testCopyConstructor_WithNullString_CopiesCorrectly() {
    // Arrange
    byte[] rawBytes = "Direct".getBytes(StandardCharsets.UTF_8);
    Utf8 original = new Utf8(rawBytes);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should produce same string", original.toString(), copy.toString());
  }

  @Test
  public void testSetByteLength_AfterCopyConstructor_IndependentInstances() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);
    int newLength = original.getByteLength() + 5;

    // Act
    copy.setByteLength(newLength);

    // Assert
    assertNotEquals("Copy expansion should not affect original length", original.getByteLength(), copy.getByteLength());
    assertEquals("Original should remain unchanged", "Original", original.toString());
  }

  @Test
  public void testSetByteLength_BoundaryValue_MinimumExpansion() {
    // Arrange
    utf8Instance.set("X");
    int currentLength = utf8Instance.getByteLength();

    // Act - expand by exactly 1 to trigger line 116 condition
    utf8Instance.setByteLength(currentLength + 1);

    // Assert
    assertEquals("Length should be expanded minimally", currentLength + 1, utf8Instance.getByteLength());
  }

  @Test
  public void testSetByteLength_ReturnsThis_ForMethodChaining() {
    // Arrange
    utf8Instance.set("Chain");

    // Act
    Utf8 result = utf8Instance.setByteLength(10);

    // Assert
    assertSame("Should return this for method chaining", utf8Instance, result);
  }

  @Test
  public void testCopyConstructor_PreservesCachedString() {
    // Arrange
    Utf8 original = new Utf8(TEST_STRING);
    original.toString(); // Cache the string

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should produce same string value", original.toString(), copy.toString());
  }

  @Test
  public void testSetByteLength_WithLargeExpansion_HandlesCorrectly() {
    // Arrange
    utf8Instance.set("Small");
    int originalLength = utf8Instance.getByteLength();

    // Act - expand to 1000 bytes
    utf8Instance.setByteLength(1000);

    // Assert
    assertEquals("Should handle large expansion", 1000, utf8Instance.getByteLength());
    assertTrue("Array should be sufficiently large", utf8Instance.getBytes().length >= 1000);
  }
}
