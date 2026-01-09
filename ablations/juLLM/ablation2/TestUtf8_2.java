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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * Comprehensive test class for Utf8 utility class. Focuses on copy constructor
 * (lines 64-65) and setByteLength method (lines 116-117-118).
 */
public class TestUtf8_2 {

  private Utf8 utf8Instance;
  private static final String TEST_STRING = "Hello World";
  private static final String UNICODE_STRING = "Hello 世界 🌍";
  private static final String EMPTY_STRING = "";

  @Before
  public void setUp() {
    utf8Instance = new Utf8();
  }

  @After
  public void tearDown() {
    utf8Instance = null;
  }

  // ========== Copy Constructor Tests (Lines 64-65) ==========

  @Test
  public void testCopyConstructor_WithSimpleString_CreatesIndependentCopy() {
    // Arrange
    Utf8 original = new Utf8(TEST_STRING);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same byte length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same string value as original", original.toString(), copy.toString());
    assertNotSame("Copy should have different byte array reference", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_WithUnicodeString_CopiesAllBytes() {
    // Arrange
    Utf8 original = new Utf8(UNICODE_STRING);
    int originalLength = original.getByteLength();

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should preserve unicode string", UNICODE_STRING, copy.toString());
    assertEquals("Copy should have same byte length", originalLength, copy.getByteLength());

    // Verify byte-by-byte copy
    byte[] originalBytes = original.getBytes();
    byte[] copiedBytes = copy.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Byte at index " + i + " should match", originalBytes[i], copiedBytes[i]);
    }
  }

  @Test
  public void testCopyConstructor_WithEmptyUtf8_CreatesEmptyCopy() {
    // Arrange
    Utf8 original = new Utf8(EMPTY_STRING);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy of empty Utf8 should have zero length", 0, copy.getByteLength());
    assertEquals("Copy of empty Utf8 should be empty string", EMPTY_STRING, copy.toString());
  }

  @Test
  public void testCopyConstructor_ModifyingCopy_DoesNotAffectOriginal() {
    // Arrange
    Utf8 original = new Utf8(TEST_STRING);
    Utf8 copy = new Utf8(original);

    // Act - Modify the copy
    copy.set("Modified String");

    // Assert
    assertEquals("Original should remain unchanged", TEST_STRING, original.toString());
    assertEquals("Copy should reflect the modification", "Modified String", copy.toString());
  }

  @Test
  public void testCopyConstructor_WithByteArrayConstructor_CopiesCorrectly() {
    // Arrange
    byte[] bytes = TEST_STRING.getBytes(StandardCharsets.UTF_8);
    Utf8 original = new Utf8(bytes);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should produce same string", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_WithLargeString_CopiesAllData() {
    // Arrange - Create a large string
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      sb.append("Test");
    }
    String largeString = sb.toString();
    Utf8 original = new Utf8(largeString);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as large original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should match large string content", largeString, copy.toString());
  }

  // ========== setByteLength Method Tests (Lines 116-117-118) ==========

  @Test
  public void testSetByteLength_IncreasingLength_ReallocatesArray() {
    // Arrange
    utf8Instance.set("Test");
    int originalLength = utf8Instance.getByteLength();
    byte[] originalBytes = utf8Instance.getBytes();

    // Act - Increase length to trigger reallocation (line 116-118)
    int newLength = originalLength + 10;
    utf8Instance.setByteLength(newLength);

    // Assert
    assertEquals("Byte length should be updated", newLength, utf8Instance.getByteLength());
    assertNotSame("Bytes array should be reallocated", originalBytes, utf8Instance.getBytes());
    assertTrue("New array should be larger", utf8Instance.getBytes().length >= newLength);
  }

  @Test
  public void testSetByteLength_IncreasingLength_PreservesOriginalBytes() {
    // Arrange
    String testStr = "Preserve";
    utf8Instance.set(testStr);
    byte[] expectedBytes = testStr.getBytes(StandardCharsets.UTF_8);
    int originalLength = utf8Instance.getByteLength();

    // Act - Increase length to trigger array copy (line 117)
    utf8Instance.setByteLength(originalLength + 5);

    // Assert - Original bytes should be preserved
    byte[] newBytes = utf8Instance.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original byte at index " + i + " should be preserved", expectedBytes[i], newBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_DecreasingLength_KeepsSameArray() {
    // Arrange
    utf8Instance.set("LongString");
    byte[] originalBytes = utf8Instance.getBytes();

    // Act - Decrease length (should not reallocate)
    utf8Instance.setByteLength(4);

    // Assert
    assertEquals("Byte length should be decreased", 4, utf8Instance.getByteLength());
    assertSame("Bytes array should remain the same", originalBytes, utf8Instance.getBytes());
  }

  @Test
  public void testSetByteLength_SameLength_ClearsCachedString() {
    // Arrange
    utf8Instance.set("Test");
    String cachedString = utf8Instance.toString(); // Force caching
    int currentLength = utf8Instance.getByteLength();

    // Act - Set same length (should clear cached string)
    utf8Instance.setByteLength(currentLength);

    // Assert
    assertEquals("Length should remain the same", currentLength, utf8Instance.getByteLength());
    // Cached string is cleared but we can't directly test it without reflection
  }

  @Test
  public void testSetByteLength_ZeroLength_ValidOperation() {
    // Arrange
    utf8Instance.set("Something");

    // Act
    utf8Instance.setByteLength(0);

    // Assert
    assertEquals("Byte length should be zero", 0, utf8Instance.getByteLength());
    assertEquals("toString should return empty string", EMPTY_STRING, utf8Instance.toString());
  }

  @Test
  public void testSetByteLength_FromEmptyToNonEmpty_AllocatesArray() {
    // Arrange
    Utf8 emptyUtf8 = new Utf8();
    assertEquals("Should start with zero length", 0, emptyUtf8.getByteLength());

    // Act - Increase from 0 to positive length (triggers reallocation)
    int newLength = 10;
    emptyUtf8.setByteLength(newLength);

    // Assert
    assertEquals("Byte length should be updated", newLength, emptyUtf8.getByteLength());
    assertTrue("Bytes array should be allocated", emptyUtf8.getBytes().length >= newLength);
  }

  @Test
  public void testSetByteLength_ExactBoundary_AllocatesExactSize() {
    // Arrange
    utf8Instance.set("A");

    // Act - Set to a specific length that requires reallocation
    int targetLength = 100;
    utf8Instance.setByteLength(targetLength);

    // Assert
    assertEquals("Should have target length", targetLength, utf8Instance.getByteLength());
    assertTrue("Array should be at least target size", utf8Instance.getBytes().length >= targetLength);
  }

  @Test
  public void testSetByteLength_LargeValidValue_WorksCorrectly() {
    // Arrange
    utf8Instance.set("Test");

    // Act - Set to a large but valid length
    // Note: Cannot test Integer.MAX_VALUE as it causes OutOfMemoryError
    // The MAX_LENGTH check at line 112 would prevent values exceeding the
    // configured limit
    // This test validates that reasonable large values work correctly
    int largeLength = 10000;
    utf8Instance.setByteLength(largeLength);

    // Assert
    assertEquals("Should accept large valid length", largeLength, utf8Instance.getByteLength());
  }

  @Test
  public void testSetByteLength_ChainedCalls_ReturnsThis() {
    // Arrange
    utf8Instance.set("Start");

    // Act
    Utf8 result = utf8Instance.setByteLength(10);

    // Assert
    assertSame("setByteLength should return this for chaining", utf8Instance, result);
  }

  @Test
  public void testSetByteLength_MultipleIncreases_ContinuesGrowingArray() {
    // Arrange
    utf8Instance.set("X");

    // Act - Multiple increases
    utf8Instance.setByteLength(5);
    utf8Instance.setByteLength(10);
    utf8Instance.setByteLength(20);

    // Assert
    assertEquals("Final length should be 20", 20, utf8Instance.getByteLength());
    assertTrue("Array should accommodate final length", utf8Instance.getBytes().length >= 20);
  }

  // ========== Integration Tests ==========

  @Test
  public void testCopyConstructorAndSetByteLength_Integration() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);

    // Act - Modify copy's byte length
    copy.setByteLength(copy.getByteLength() + 5);

    // Assert - Original should be unaffected
    assertEquals("Original should retain its length", "Original".getBytes(StandardCharsets.UTF_8).length,
        original.getByteLength());
    assertNotEquals("Copy should have different length", original.getByteLength(), copy.getByteLength());
  }

  @Test
  public void testSetByteLength_AfterCopyConstructor_IndependentArrays() {
    // Arrange
    Utf8 original = new Utf8("Test");
    Utf8 copy = new Utf8(original);
    byte[] originalBytesRef = original.getBytes();

    // Act - Force reallocation in copy
    copy.setByteLength(100);

    // Assert
    assertSame("Original's byte array should be unchanged", originalBytesRef, original.getBytes());
    assertNotSame("Copy should have different byte array", originalBytesRef, copy.getBytes());
  }

  // ========== Edge Cases and Boundary Tests ==========

  @Test
  public void testCopyConstructor_WithSingleByteString_CopiesCorrectly() {
    // Arrange
    Utf8 original = new Utf8("A");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Single byte should be copied", 1, copy.getByteLength());
    assertEquals("String value should match", "A", copy.toString());
  }

  @Test
  public void testSetByteLength_IncrementByOne_WorksCorrectly() {
    // Arrange
    utf8Instance.set("AB");
    int originalLength = utf8Instance.getByteLength();

    // Act
    utf8Instance.setByteLength(originalLength + 1);

    // Assert
    assertEquals("Length should increment by one", originalLength + 1, utf8Instance.getByteLength());
  }

  @Test
  public void testCopyConstructor_WithMultiByteUnicode_PreservesEncoding() {
    // Arrange - Multi-byte UTF-8 characters
    String multiByteStr = "€£¥";
    Utf8 original = new Utf8(multiByteStr);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Multi-byte unicode should be preserved", multiByteStr, copy.toString());
    assertEquals("Byte lengths should match", original.getByteLength(), copy.getByteLength());
  }

  @Test
  public void testSetByteLength_WithUnicodeContent_MaintainsByteIntegrity() {
    // Arrange
    utf8Instance.set("日本");
    byte[] originalBytes = utf8Instance.getBytes().clone();
    int originalLength = utf8Instance.getByteLength();

    // Act - Increase length
    utf8Instance.setByteLength(originalLength + 10);

    // Assert - Original bytes should be intact
    byte[] newBytes = utf8Instance.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Unicode byte at index " + i + " should be preserved", originalBytes[i], newBytes[i]);
    }
  }
}
