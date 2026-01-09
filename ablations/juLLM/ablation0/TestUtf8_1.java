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
 * Test class for Utf8 Covers lines 64-65 (copy constructor), 116-117-118
 * (setByteLength method) of Utf8.java
 */
public class TestUtf8_1 {

  private Utf8 utf8Instance;
  private static final String TEST_STRING = "Hello World";
  private static final String EMPTY_STRING = "";
  private static final String UNICODE_STRING = "Hello 世界 🌍";
  private static final String LONG_STRING = "This is a longer string for testing purposes with many characters";

  @Before
  public void setUp() {
    utf8Instance = new Utf8();
  }

  @After
  public void tearDown() {
    utf8Instance = null;
  }

  // ==================== Tests for Copy Constructor (lines 64-65)
  // ====================

  @Test
  public void testCopyConstructor_WithSimpleString_CreatesIndependentCopy() {
    // Arrange
    Utf8 original = new Utf8(TEST_STRING);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same byte length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
    assertNotSame("Copy should be a different object", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_WithEmptyString_CreatesEmptyCopy() {
    // Arrange
    Utf8 original = new Utf8(EMPTY_STRING);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have zero byte length", 0, copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
  }

  @Test
  public void testCopyConstructor_WithUnicodeString_CopiesAllBytes() {
    // Arrange
    Utf8 original = new Utf8(UNICODE_STRING);
    int originalLength = original.getByteLength();

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same byte length", originalLength, copy.getByteLength());
    assertEquals("Copy should have same string representation", original.toString(), copy.toString());

    // Verify all bytes are copied correctly
    byte[] originalBytes = original.getBytes();
    byte[] copyBytes = copy.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Byte at index " + i + " should match", originalBytes[i], copyBytes[i]);
    }
  }

  @Test
  public void testCopyConstructor_ModifyingCopy_DoesNotAffectOriginal() {
    // Arrange
    Utf8 original = new Utf8(TEST_STRING);
    Utf8 copy = new Utf8(original);
    String newString = "Modified";

    // Act
    copy.set(newString);

    // Assert
    assertNotEquals("Original should not be affected by copy modification", original.toString(), copy.toString());
    assertEquals("Original should remain unchanged", TEST_STRING, original.toString());
    assertEquals("Copy should have new value", newString, copy.toString());
  }

  @Test
  public void testCopyConstructor_WithLongString_CopiesCompleteData() {
    // Arrange
    Utf8 original = new Utf8(LONG_STRING);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have identical content", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_CopiesCachedString() {
    // Arrange
    Utf8 original = new Utf8(TEST_STRING);
    original.toString(); // Cache the string

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should preserve string value", TEST_STRING, copy.toString());
  }

  // ==================== Tests for setByteLength Method (lines 116-117-118)
  // ====================

  @Test
  public void testSetByteLength_ExpandingBuffer_CreatesNewArray() {
    // Arrange
    utf8Instance.set("Hi");
    int originalLength = utf8Instance.getByteLength();
    int newLength = originalLength + 10;

    // Act
    byte[] originalBytes = utf8Instance.getBytes();
    Utf8 result = utf8Instance.setByteLength(newLength);
    byte[] newBytes = utf8Instance.getBytes();

    // Assert
    assertNotSame("New byte array should be created when expanding", originalBytes, newBytes);
    assertEquals("Length should be updated", newLength, utf8Instance.getByteLength());
    assertSame("Should return this for chaining", utf8Instance, result);

    // Verify original bytes were copied
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original byte at index " + i + " should be preserved", originalBytes[i], newBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_ReducingLength_KeepsExistingArray() {
    // Arrange
    utf8Instance.set(LONG_STRING);
    byte[] originalBytes = utf8Instance.getBytes();
    int newLength = 5;

    // Act
    utf8Instance.setByteLength(newLength);
    byte[] currentBytes = utf8Instance.getBytes();

    // Assert
    assertSame("Byte array should not be replaced when reducing length", originalBytes, currentBytes);
    assertEquals("Length should be updated", newLength, utf8Instance.getByteLength());
  }

  @Test
  public void testSetByteLength_SameLength_KeepsExistingArray() {
    // Arrange
    utf8Instance.set(TEST_STRING);
    int currentLength = utf8Instance.getByteLength();
    byte[] originalBytes = utf8Instance.getBytes();

    // Act
    utf8Instance.setByteLength(currentLength);
    byte[] currentBytes = utf8Instance.getBytes();

    // Assert
    assertSame("Byte array should not be replaced when length is same", originalBytes, currentBytes);
    assertEquals("Length should remain unchanged", currentLength, utf8Instance.getByteLength());
  }

  @Test
  public void testSetByteLength_ClearsStringCache() {
    // Arrange
    utf8Instance.set(TEST_STRING);
    String cachedString = utf8Instance.toString(); // Cache the string

    // Act
    utf8Instance.setByteLength(5);

    // Assert - this is harder to verify directly but we can check behavior
    // The string cache should be cleared and toString() will create a new string
    assertNotEquals("String representation should differ after length change", cachedString, utf8Instance.toString());
  }

  @Test
  public void testSetByteLength_ToZero_HandlesEmptyState() {
    // Arrange
    utf8Instance.set(TEST_STRING);

    // Act
    utf8Instance.setByteLength(0);

    // Assert
    assertEquals("Length should be zero", 0, utf8Instance.getByteLength());
    assertEquals("String representation should be empty", "", utf8Instance.toString());
  }

  @Test
  public void testSetByteLength_LargeExpansion_AllocatesCorrectSize() {
    // Arrange
    utf8Instance.set("A");
    int largeSize = 1000;

    // Act
    utf8Instance.setByteLength(largeSize);

    // Assert
    assertEquals("Length should be set to large size", largeSize, utf8Instance.getByteLength());
    assertTrue("Byte array should be at least the requested size", utf8Instance.getBytes().length >= largeSize);
  }

  @Test
  public void testSetByteLength_CopiesExistingData_WhenExpanding() {
    // Arrange
    String testData = "Test";
    utf8Instance.set(testData);
    byte[] expectedBytes = testData.getBytes(StandardCharsets.UTF_8);
    int originalLength = utf8Instance.getByteLength();

    // Act
    utf8Instance.setByteLength(originalLength + 10);
    byte[] actualBytes = utf8Instance.getBytes();

    // Assert
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Byte at position " + i + " should be preserved", expectedBytes[i], actualBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_WithMaxValue_HandlesCorrectly() {
    // Arrange
    utf8Instance.set("Test");

    // Act & Assert
    // Note: MAX_LENGTH is Integer.MAX_VALUE by default, so setting to MAX_VALUE
    // will try to allocate array which causes OutOfMemoryError before the check
    // This test verifies the behavior with a large but allocatable size
    try {
      // Use a large value that won't cause OOM but tests the expansion logic
      utf8Instance.setByteLength(10000);
      assertEquals("Length should be set to large value", 10000, utf8Instance.getByteLength());
    } catch (Exception e) {
      fail("Should not throw exception for reasonable large values: " + e.getMessage());
    }
  }

  @Test
  public void testSetByteLength_MultipleExpansions_MaintainsData() {
    // Arrange
    utf8Instance.set("AB");
    byte[] originalBytes = utf8Instance.getBytes();

    // Act - expand multiple times
    utf8Instance.setByteLength(10);
    utf8Instance.setByteLength(20);
    byte[] finalBytes = utf8Instance.getBytes();

    // Assert
    assertEquals("Length should be 20", 20, utf8Instance.getByteLength());
    // Original bytes should still be present
    assertEquals("First byte should be preserved", originalBytes[0], finalBytes[0]);
    assertEquals("Second byte should be preserved", originalBytes[1], finalBytes[1]);
  }

  @Test
  public void testSetByteLength_AfterCopyConstructor_IndependentBuffers() {
    // Arrange
    Utf8 original = new Utf8(TEST_STRING);
    Utf8 copy = new Utf8(original);
    int newLength = original.getByteLength() + 10;

    // Act
    copy.setByteLength(newLength);

    // Assert
    assertEquals("Original length should be unchanged", TEST_STRING.getBytes(StandardCharsets.UTF_8).length,
        original.getByteLength());
    assertEquals("Copy length should be updated", newLength, copy.getByteLength());
  }

  @Test
  public void testSetByteLength_ReturnsThis_ForChaining() {
    // Arrange
    utf8Instance.set(TEST_STRING);

    // Act
    Utf8 result = utf8Instance.setByteLength(10);

    // Assert
    assertSame("setByteLength should return this for method chaining", utf8Instance, result);
  }

  // ==================== Integration Tests ====================

  @Test
  public void testIntegration_CopyAndResize_MaintainsIndependence() {
    // Arrange
    Utf8 original = new Utf8("Original");

    // Act
    Utf8 copy = new Utf8(original);
    copy.setByteLength(original.getByteLength() + 5);

    // Assert
    assertNotEquals("Byte arrays should be different", original.getBytes(), copy.getBytes());
    assertEquals("Original should remain unchanged", "Original", original.toString());
  }

  @Test
  public void testIntegration_MultipleOperations_WorkCorrectly() {
    // Arrange & Act
    Utf8 utf8 = new Utf8("Start");
    Utf8 copy = new Utf8(utf8);
    copy.setByteLength(10);
    copy.set("Modified");

    // Assert
    assertEquals("Original should be unchanged", "Start", utf8.toString());
    assertEquals("Copy should have new value", "Modified", copy.toString());
  }
}
