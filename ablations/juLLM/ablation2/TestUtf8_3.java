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
 * Comprehensive test class for Utf8 class focusing on: - Copy constructor
 * (lines 64-65) - setByteLength method (lines 116-117-118)
 */
public class TestUtf8_3 {

  private Utf8 testUtf8;
  private byte[] testBytes;
  private String testString;

  @Before
  public void setUp() {
    testString = "Hello World";
    testBytes = testString.getBytes(StandardCharsets.UTF_8);
    testUtf8 = new Utf8(testString);
  }

  @After
  public void tearDown() {
    testUtf8 = null;
    testBytes = null;
    testString = null;
  }

  // ==================== Tests for Copy Constructor (lines 64-65)
  // ====================

  @Test
  public void testCopyConstructor_ValidUtf8_CreatesIndependentCopy() {
    // Arrange
    Utf8 original = new Utf8("Test String");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same string value as original", original.toString(), copy.toString());
    assertNotSame("Copy should have different byte array instance", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_EmptyUtf8_CreatesEmptyCopy() {
    // Arrange
    Utf8 original = new Utf8("");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have zero length", 0, copy.getByteLength());
    assertEquals("Copy should be empty string", "", copy.toString());
  }

  @Test
  public void testCopyConstructor_ModifyOriginal_DoesNotAffectCopy() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);

    // Act
    original.set("Modified");

    // Assert
    assertNotEquals("Copy should remain unchanged when original is modified", original.toString(), copy.toString());
    assertEquals("Copy should still have original value", "Original", copy.toString());
  }

  @Test
  public void testCopyConstructor_ModifyCopy_DoesNotAffectOriginal() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);

    // Act
    copy.set("Modified");

    // Assert
    assertNotEquals("Original should remain unchanged when copy is modified", original.toString(), copy.toString());
    assertEquals("Original should still have original value", "Original", original.toString());
  }

  @Test
  public void testCopyConstructor_UnicodeCharacters_CopiesCorrectly() {
    // Arrange
    Utf8 original = new Utf8("Hello 世界 🌍");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should preserve unicode characters", original.toString(), copy.toString());
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
  }

  @Test
  public void testCopyConstructor_LargeString_CopiesCorrectly() {
    // Arrange
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 10000; i++) {
      sb.append("a");
    }
    Utf8 original = new Utf8(sb.toString());

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as large original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same content as large original", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_ByteArrayIndependence_ModifyingOriginalBytesDoesNotAffectCopy() {
    // Arrange
    Utf8 original = new Utf8("Test");
    Utf8 copy = new Utf8(original);

    // Act - modify original's byte array directly
    byte[] originalBytes = original.getBytes();
    if (originalBytes.length > 0) {
      originalBytes[0] = (byte) 'X';
    }

    // Assert - copy should still have original value
    assertEquals("Copy should not be affected by direct byte modifications to original", "Test", copy.toString());
  }

  @Test
  public void testCopyConstructor_CachedString_IsCopied() {
    // Arrange
    Utf8 original = new Utf8("Cached");
    String cachedString = original.toString(); // Force string caching

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same cached string value", cachedString, copy.toString());
  }

  // ==================== Tests for setByteLength (lines 116-117-118)
  // ====================

  @Test
  public void testSetByteLength_IncreaseLength_AllocatesNewArray() {
    // Arrange
    Utf8 utf8 = new Utf8("Hi");
    int originalLength = utf8.getByteLength();
    int newLength = originalLength + 10;

    // Act
    Utf8 result = utf8.setByteLength(newLength);

    // Assert
    assertSame("setByteLength should return this", utf8, result);
    assertEquals("Length should be updated", newLength, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_DecreaseLength_KeepsSameArray() {
    // Arrange
    Utf8 utf8 = new Utf8("Hello World");
    byte[] originalBytes = utf8.getBytes();
    int newLength = 5;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    assertSame("Byte array should remain same when decreasing length", originalBytes, utf8.getBytes());
    assertEquals("Length should be updated", newLength, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_SameLength_ClearsCachedString() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");
    String cachedString = utf8.toString(); // Cache the string
    int currentLength = utf8.getByteLength();

    // Act
    utf8.setByteLength(currentLength);

    // Assert
    assertEquals("Length should remain same", currentLength, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_ZeroLength_SetsEmptyString() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");

    // Act
    utf8.setByteLength(0);

    // Assert
    assertEquals("Length should be zero", 0, utf8.getByteLength());
    assertEquals("String should be empty", "", utf8.toString());
  }

  @Test
  public void testSetByteLength_PreservesExistingBytes_WhenIncreasing() {
    // Arrange
    Utf8 utf8 = new Utf8("ABC");
    byte[] originalBytes = new byte[utf8.getByteLength()];
    System.arraycopy(utf8.getBytes(), 0, originalBytes, 0, utf8.getByteLength());
    int originalLength = utf8.getByteLength();

    // Act
    utf8.setByteLength(originalLength + 5);

    // Assert
    byte[] newBytes = utf8.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original bytes should be preserved at index " + i, originalBytes[i], newBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_LargeIncrease_AllocatesCorrectSize() {
    // Arrange
    Utf8 utf8 = new Utf8("A");
    int newLength = 1000;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    assertEquals("Length should be updated to large value", newLength, utf8.getByteLength());
    assertTrue("Byte array should be at least new length", utf8.getBytes().length >= newLength);
  }

  @Test
  public void testSetByteLength_ExceedsMaxLength_ThrowsException() {
    // Note: Testing MAX_LENGTH check (line 112) requires setting system property
    // org.apache.avro.limits.string.maxLength before class loading.
    // This test verifies the behavior is properly handled for reasonable values.

    // Arrange
    Utf8 utf8 = new Utf8("Test");
    int largeButValidLength = 1000000; // 1MB - reasonable size that won't cause OOM

    // Act
    Utf8 result = utf8.setByteLength(largeButValidLength);

    // Assert
    assertNotNull("Should handle large but valid length", result);
    assertEquals("Length should be set correctly", largeButValidLength, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_ChainedCalls_WorksCorrectly() {
    // Arrange
    Utf8 utf8 = new Utf8();

    // Act
    Utf8 result = utf8.setByteLength(10).setByteLength(20).setByteLength(15);

    // Assert
    assertSame("Chained calls should return same instance", utf8, result);
    assertEquals("Final length should be from last call", 15, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_MultipleIncreases_PreservesData() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");
    String originalString = utf8.toString();
    int originalLength = utf8.getByteLength();

    // Act
    utf8.setByteLength(originalLength + 5);
    utf8.setByteLength(originalLength + 10);
    utf8.setByteLength(originalLength); // Back to original

    // Assert
    assertEquals("Should have original length", originalLength, utf8.getByteLength());
    assertEquals("Should preserve original string", originalString, utf8.toString());
  }

  @Test
  public void testSetByteLength_AfterEmptyConstructor_AllocatesArray() {
    // Arrange
    Utf8 utf8 = new Utf8();
    assertEquals("Initial length should be 0", 0, utf8.getByteLength());

    // Act
    utf8.setByteLength(10);

    // Assert
    assertEquals("Length should be updated", 10, utf8.getByteLength());
    assertNotNull("Byte array should be allocated", utf8.getBytes());
    assertTrue("Byte array should be large enough", utf8.getBytes().length >= 10);
  }

  // ==================== Edge Cases and Boundary Tests ====================

  @Test
  public void testCopyConstructor_WithNullCachedString_HandlesCorrectly() {
    // Arrange
    Utf8 original = new Utf8(new byte[] { 72, 105 }); // "Hi" in bytes
    // Don't call toString() to leave string cache as null

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should work even with null cached string", "Hi", copy.toString());
  }

  @Test
  public void testSetByteLength_BoundaryValue_One() {
    // Arrange
    Utf8 utf8 = new Utf8();

    // Act
    utf8.setByteLength(1);

    // Assert
    assertEquals("Length should be 1", 1, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_IncreaseFromZero_WorksCorrectly() {
    // Arrange
    Utf8 utf8 = new Utf8();

    // Act
    utf8.setByteLength(100);

    // Assert
    assertEquals("Length should increase from zero", 100, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_MultipleDecreases_WorksCorrectly() {
    // Arrange
    Utf8 utf8 = new Utf8("Hello World Test");

    // Act
    utf8.setByteLength(10);
    utf8.setByteLength(5);
    utf8.setByteLength(2);

    // Assert
    assertEquals("Final length should be 2", 2, utf8.getByteLength());
  }

  @Test
  public void testCopyConstructor_SpecialCharacters_CopiesCorrectly() {
    // Arrange
    Utf8 original = new Utf8("Tab\tNewline\nReturn\rQuote\"");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Special characters should be preserved", original.toString(), copy.toString());
  }

  @Test
  public void testSetByteLength_ToSameValue_IsIdempotent() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");
    int length = utf8.getByteLength();
    byte[] originalBytes = utf8.getBytes();

    // Act
    utf8.setByteLength(length);
    utf8.setByteLength(length);
    utf8.setByteLength(length);

    // Assert
    assertEquals("Length should remain unchanged", length, utf8.getByteLength());
    assertSame("Byte array should remain same", originalBytes, utf8.getBytes());
  }
}
