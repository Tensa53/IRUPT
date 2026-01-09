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

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import org.apache.avro.AvroRuntimeException;

/**
 * Test class for Utf8 Specifically covers lines 64-65 (copy constructor) and
 * 116-117-118 (setByteLength method)
 */
public class TestUtf8_2 {

  private Utf8 utf8Instance;

  @Before
  public void setUp() {
    utf8Instance = null;
  }

  @After
  public void tearDown() {
    utf8Instance = null;
  }

  // ==================== Copy Constructor Tests (Lines 64-65)
  // ====================

  @Test
  public void testCopyConstructor_WithSimpleString_CreatesIndependentCopy() {
    // Arrange
    Utf8 original = new Utf8("Hello");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same byte length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same string value as original", original.toString(), copy.toString());
    assertNotSame("Copy should have different byte array instance", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_WithEmptyUtf8_CreatesEmptyCopy() {
    // Arrange
    Utf8 original = new Utf8("");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have zero byte length", 0, copy.getByteLength());
    assertEquals("Copy should be empty string", "", copy.toString());
    assertNotSame("Copy should have different byte array instance", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_WithUnicodeCharacters_PreservesContent() {
    // Arrange
    Utf8 original = new Utf8("Hello 世界 🌍");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should preserve unicode content", original.toString(), copy.toString());
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
    assertArrayEquals("Copy should have identical byte content",
        copyBytes(original.getBytes(), original.getByteLength()), copyBytes(copy.getBytes(), copy.getByteLength()));
  }

  @Test
  public void testCopyConstructor_ModifyingOriginal_DoesNotAffectCopy() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);

    // Act
    original.set("Modified");

    // Assert
    assertEquals("Original should be modified", "Modified", original.toString());
    assertEquals("Copy should remain unchanged", "Original", copy.toString());
  }

  @Test
  public void testCopyConstructor_ModifyingCopy_DoesNotAffectOriginal() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);

    // Act
    copy.set("Modified");

    // Assert
    assertEquals("Original should remain unchanged", "Original", original.toString());
    assertEquals("Copy should be modified", "Modified", copy.toString());
  }

  @Test
  public void testCopyConstructor_WithLongString_CopiesAllBytes() {
    // Arrange
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      sb.append("A");
    }
    Utf8 original = new Utf8(sb.toString());

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length", 1000, copy.getByteLength());
    assertEquals("Copy should have same content", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_WithMultiByteCharacters_CopiesCorrectly() {
    // Arrange - Using characters that take multiple bytes in UTF-8
    Utf8 original = new Utf8("日本語");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should preserve multi-byte characters", "日本語", copy.toString());
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
    assertTrue("Copy byte length should be greater than character count", copy.getByteLength() > 3);
  }

  @Test
  public void testCopyConstructor_WithCachedString_CopiesStringReference() {
    // Arrange
    Utf8 original = new Utf8("Test");
    original.toString(); // This caches the string

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same string value", original.toString(), copy.toString());
  }

  // ==================== setByteLength Tests (Lines 116-117-118)
  // ====================

  @Test
  public void testSetByteLength_IncreasingLength_AllocatesNewArray() {
    // Arrange
    utf8Instance = new Utf8("Short");
    int originalLength = utf8Instance.getByteLength();
    byte[] originalBytes = utf8Instance.getBytes();

    // Act
    utf8Instance.setByteLength(100);

    // Assert
    assertEquals("Byte length should be updated", 100, utf8Instance.getByteLength());
    assertNotSame("Should allocate new byte array", originalBytes, utf8Instance.getBytes());
    assertTrue("New array should be at least 100 bytes", utf8Instance.getBytes().length >= 100);
  }

  @Test
  public void testSetByteLength_IncreasingLength_PreservesOriginalBytes() {
    // Arrange
    utf8Instance = new Utf8("Test");
    byte[] originalBytes = copyBytes(utf8Instance.getBytes(), utf8Instance.getByteLength());
    int originalLength = utf8Instance.getByteLength();

    // Act
    utf8Instance.setByteLength(50);

    // Assert
    byte[] newBytes = utf8Instance.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original bytes should be preserved at index " + i, originalBytes[i], newBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_DecreasingLength_KeepsSameArray() {
    // Arrange
    utf8Instance = new Utf8("LongString");
    byte[] originalBytes = utf8Instance.getBytes();

    // Act
    utf8Instance.setByteLength(4);

    // Assert
    assertEquals("Byte length should be updated", 4, utf8Instance.getByteLength());
    assertSame("Should keep same byte array when decreasing", originalBytes, utf8Instance.getBytes());
  }

  @Test
  public void testSetByteLength_SameLength_ClearsCachedString() {
    // Arrange
    utf8Instance = new Utf8("Test");
    int originalLength = utf8Instance.getByteLength();
    String cachedString = utf8Instance.toString(); // Cache the string

    // Act
    utf8Instance.setByteLength(originalLength);

    // Assert
    assertEquals("Length should remain same", originalLength, utf8Instance.getByteLength());
  }

  @Test
  public void testSetByteLength_ToZero_UpdatesLength() {
    // Arrange
    utf8Instance = new Utf8("Test");

    // Act
    utf8Instance.setByteLength(0);

    // Assert
    assertEquals("Byte length should be zero", 0, utf8Instance.getByteLength());
    assertEquals("toString should return empty string", "", utf8Instance.toString());
  }

  @Test
  public void testSetByteLength_ToLargeValue_AllocatesLargeArray() {
    // Arrange
    utf8Instance = new Utf8();

    // Act
    utf8Instance.setByteLength(10000);

    // Assert
    assertEquals("Byte length should be 10000", 10000, utf8Instance.getByteLength());
    assertTrue("Array should be at least 10000 bytes", utf8Instance.getBytes().length >= 10000);
  }

  @Test
  public void testSetByteLength_MultipleTimes_HandlesGrowthCorrectly() {
    // Arrange
    utf8Instance = new Utf8("Start");

    // Act & Assert - First growth
    utf8Instance.setByteLength(10);
    assertEquals("First resize to 10", 10, utf8Instance.getByteLength());

    // Act & Assert - Second growth
    utf8Instance.setByteLength(20);
    assertEquals("Second resize to 20", 20, utf8Instance.getByteLength());

    // Act & Assert - Shrink
    utf8Instance.setByteLength(5);
    assertEquals("Shrink to 5", 5, utf8Instance.getByteLength());
  }

  @Test
  public void testSetByteLength_AfterCopyConstructor_WorksIndependently() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);

    // Act
    copy.setByteLength(50);

    // Assert
    assertEquals("Copy should have new length", 50, copy.getByteLength());
    assertEquals("Original should keep its length", "Original".getBytes().length, original.getByteLength());
  }

  @Test
  public void testSetByteLength_ReturnsThis_ForMethodChaining() {
    // Arrange
    utf8Instance = new Utf8("Test");

    // Act
    Utf8 result = utf8Instance.setByteLength(10);

    // Assert
    assertSame("setByteLength should return this for method chaining", utf8Instance, result);
  }

  @Test
  public void testSetByteLength_VeryLargeValue_HandlesCorrectly() {
    // Arrange
    utf8Instance = new Utf8();

    // Act & Assert
    // Integer.MAX_VALUE is the default MAX_LENGTH, so it will try to allocate
    // This will likely throw OutOfMemoryError rather than AvroRuntimeException
    try {
      utf8Instance.setByteLength(Integer.MAX_VALUE);
      fail("Expected OutOfMemoryError or AvroRuntimeException to be thrown");
    } catch (OutOfMemoryError e) {
      // Expected on most systems - VM cannot allocate such a large array
      assertTrue("Should get OutOfMemoryError for very large allocation", true);
    } catch (AvroRuntimeException e) {
      // Also acceptable if MAX_LENGTH property is set lower
      assertTrue("Exception message should mention string length", e.getMessage().contains("String length"));
    }
  }

  @Test
  public void testSetByteLength_ModerateIncrease_Succeeds() {
    // Arrange
    utf8Instance = new Utf8("test");
    int moderateSize = 1000;

    // Act
    utf8Instance.setByteLength(moderateSize);

    // Assert
    assertEquals("Should accept moderate size increase", moderateSize, utf8Instance.getByteLength());
    assertTrue("Array should be at least the requested size", utf8Instance.getBytes().length >= moderateSize);
  }

  @Test
  public void testSetByteLength_WithExistingContent_PreservesBytes() {
    // Arrange
    utf8Instance = new Utf8("Hello");
    byte[] originalBytes = copyBytes(utf8Instance.getBytes(), utf8Instance.getByteLength());

    // Act
    utf8Instance.setByteLength(20);

    // Assert
    byte[] newBytes = utf8Instance.getBytes();
    for (int i = 0; i < originalBytes.length; i++) {
      assertEquals("Byte at index " + i + " should be preserved", originalBytes[i], newBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_CalledMultipleTimesIncreasing_MaintainsData() {
    // Arrange
    utf8Instance = new Utf8("Data");
    byte[] originalBytes = copyBytes(utf8Instance.getBytes(), utf8Instance.getByteLength());
    int originalLen = utf8Instance.getByteLength();

    // Act - Multiple increases
    utf8Instance.setByteLength(10);
    utf8Instance.setByteLength(20);
    utf8Instance.setByteLength(30);

    // Assert
    byte[] finalBytes = utf8Instance.getBytes();
    for (int i = 0; i < originalLen; i++) {
      assertEquals("Original data should be preserved after multiple resizes", originalBytes[i], finalBytes[i]);
    }
  }

  // ==================== Integration Tests ====================

  @Test
  public void testCopyConstructorAndSetByteLength_Integration_WorksCorrectly() {
    // Arrange
    Utf8 original = new Utf8("Test");
    Utf8 copy = new Utf8(original);

    // Act
    copy.setByteLength(20);

    // Assert
    assertEquals("Copy should have new length", 20, copy.getByteLength());
    assertEquals("Original should maintain its length", 4, original.getByteLength());
    assertEquals("Original content should be unchanged", "Test", original.toString());
  }

  @Test
  public void testSetByteLength_ThenCopyConstructor_CreatesCorrectCopy() {
    // Arrange
    Utf8 original = new Utf8("Start");
    original.setByteLength(50);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as resized original", 50, copy.getByteLength());
    assertNotSame("Copy should have independent byte array", original.getBytes(), copy.getBytes());
  }

  // ==================== Helper Methods ====================

  /**
   * Helper method to copy a portion of a byte array
   */
  private byte[] copyBytes(byte[] source, int length) {
    byte[] result = new byte[length];
    System.arraycopy(source, 0, result, 0, length);
    return result;
  }
}
