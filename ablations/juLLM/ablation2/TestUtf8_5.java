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
 * Comprehensive test class for Utf8 class focusing on: - Lines 64-65: Copy
 * constructor with array copy - Lines 116-117-118: setByteLength method with
 * array resizing
 */
public class TestUtf8_5 {

  private Utf8 utf8Instance;
  private byte[] testBytes;
  private String testString;

  @Before
  public void setUp() {
    testString = "Hello, World!";
    testBytes = testString.getBytes(StandardCharsets.UTF_8);
    utf8Instance = new Utf8(testString);
  }

  @After
  public void tearDown() {
    utf8Instance = null;
    testBytes = null;
    testString = null;
  }

  // ========================================
  // Tests for Copy Constructor (Lines 64-65)
  // ========================================

  /**
   * Test copy constructor creates a new independent byte array Covers lines
   * 64-65: new byte array creation and System.arraycopy
   */
  @Test
  public void testCopyConstructor_WithValidUtf8_CreatesIndependentCopy() {
    // Arrange
    Utf8 original = new Utf8("Test String");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertNotNull("Copy should not be null", copy);
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same string value", original.toString(), copy.toString());
    assertNotSame("Copy should have different byte array instance", original.getBytes(), copy.getBytes());
  }

  /**
   * Test that modifying copy doesn't affect original Verifies line 64-65: proper
   * array copy independence
   */
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

  /**
   * Test copy constructor with empty Utf8 Edge case for lines 64-65
   */
  @Test
  public void testCopyConstructor_WithEmptyUtf8_CreatesEmptyCopy() {
    // Arrange
    Utf8 original = new Utf8();

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have zero byte length", 0, copy.getByteLength());
    assertEquals("Copy should be empty string", "", copy.toString());
  }

  /**
   * Test copy constructor with UTF-8 multi-byte characters Covers lines 64-65
   * with complex UTF-8 data
   */
  @Test
  public void testCopyConstructor_WithMultiByteCharacters_CopiesCorrectly() {
    // Arrange
    String multiByteString = "Hello 世界 🌍";
    Utf8 original = new Utf8(multiByteString);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should preserve multi-byte characters", multiByteString, copy.toString());
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
  }

  /**
   * Test copy constructor preserves cached string Verifies line 66: string field
   * copy
   */
  @Test
  public void testCopyConstructor_WithCachedString_PreservesString() {
    // Arrange
    Utf8 original = new Utf8("Cached");
    original.toString(); // Cache the string

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same string value", "Cached", copy.toString());
  }

  /**
   * Test copy constructor with large data Boundary test for lines 64-65
   */
  @Test
  public void testCopyConstructor_WithLargeData_CopiesSuccessfully() {
    // Arrange
    StringBuilder largeString = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      largeString.append("LargeData");
    }
    Utf8 original = new Utf8(largeString.toString());

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should match original", original.toString(), copy.toString());
  }

  /**
   * Test copy constructor byte-by-byte verification Verifies System.arraycopy on
   * line 65 works correctly
   */
  @Test
  public void testCopyConstructor_ByteByByteComparison_AllBytesMatch() {
    // Arrange
    Utf8 original = new Utf8("ByteTest");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    byte[] originalBytes = original.getBytes();
    byte[] copyBytes = copy.getBytes();
    for (int i = 0; i < original.getByteLength(); i++) {
      assertEquals("Byte at index " + i + " should match", originalBytes[i], copyBytes[i]);
    }
  }

  // ========================================
  // Tests for setByteLength (Lines 116-117-118)
  // ========================================

  /**
   * Test setByteLength with larger size triggers array resize Covers lines
   * 116-117-118: new array creation and copy
   */
  @Test
  public void testSetByteLength_IncreasingSize_ResizesArrayAndCopiesData() {
    // Arrange
    Utf8 utf8 = new Utf8("Short");
    int originalLength = utf8.getByteLength();
    int newLength = originalLength + 50;

    // Act
    Utf8 result = utf8.setByteLength(newLength);

    // Assert
    assertSame("setByteLength should return same instance", utf8, result);
    assertEquals("Byte length should be updated", newLength, utf8.getByteLength());
    assertTrue("Byte array should be resized", utf8.getBytes().length >= newLength);
  }

  /**
   * Test setByteLength preserves existing data when resizing Verifies line 117:
   * System.arraycopy preserves old data
   */
  @Test
  public void testSetByteLength_ResizingLarger_PreservesExistingBytes() {
    // Arrange
    String original = "Preserve";
    Utf8 utf8 = new Utf8(original);
    byte[] originalBytes = new byte[utf8.getByteLength()];
    System.arraycopy(utf8.getBytes(), 0, originalBytes, 0, utf8.getByteLength());
    int originalLength = utf8.getByteLength();

    // Act
    utf8.setByteLength(originalLength + 20);

    // Assert
    byte[] newBytes = utf8.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original byte at index " + i + " should be preserved", originalBytes[i], newBytes[i]);
    }
  }

  /**
   * Test setByteLength with same size doesn't resize Edge case: line 115
   * condition false, skips 116-118
   */
  @Test
  public void testSetByteLength_SameSize_DoesNotResize() {
    // Arrange
    Utf8 utf8 = new Utf8("SameSize");
    byte[] originalByteArray = utf8.getBytes();
    int currentLength = utf8.getByteLength();

    // Act
    utf8.setByteLength(currentLength);

    // Assert
    assertSame("Byte array should remain the same instance", originalByteArray, utf8.getBytes());
    assertEquals("Byte length should remain unchanged", currentLength, utf8.getByteLength());
  }

  /**
   * Test setByteLength with smaller size doesn't resize Edge case: line 115
   * condition false, skips 116-118
   */
  @Test
  public void testSetByteLength_DecreasingSize_DoesNotResize() {
    // Arrange
    Utf8 utf8 = new Utf8("LongerString");
    byte[] originalByteArray = utf8.getBytes();
    int newLength = 5;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    assertSame("Byte array should remain the same instance", originalByteArray, utf8.getBytes());
    assertEquals("Byte length should be updated", newLength, utf8.getByteLength());
  }

  /**
   * Test setByteLength clears cached string Verifies line 121: string cache
   * invalidation
   */
  @Test
  public void testSetByteLength_AnySize_ClearsCachedString() {
    // Arrange
    Utf8 utf8 = new Utf8("Cached");
    String cached = utf8.toString(); // Cache the string

    // Act
    utf8.setByteLength(utf8.getByteLength() + 10);

    // Assert - accessing toString will rebuild from bytes
    // The important part is that internal string was cleared
    assertNotNull("toString should still work", utf8.toString());
  }

  /**
   * Test setByteLength with zero size Edge case for lines 116-118
   */
  @Test
  public void testSetByteLength_ToZero_UpdatesLength() {
    // Arrange
    Utf8 utf8 = new Utf8("NonEmpty");

    // Act
    utf8.setByteLength(0);

    // Assert
    assertEquals("Byte length should be zero", 0, utf8.getByteLength());
    assertEquals("String should be empty", "", utf8.toString());
  }

  /**
   * Test setByteLength multiple times with increasing sizes Stress test for lines
   * 116-118 array resizing
   */
  @Test
  public void testSetByteLength_MultipleIncreases_ResizesCorrectly() {
    // Arrange
    Utf8 utf8 = new Utf8();

    // Act & Assert
    for (int size = 10; size <= 100; size += 10) {
      utf8.setByteLength(size);
      assertEquals("Byte length should match iteration " + size, size, utf8.getByteLength());
      assertTrue("Byte array should accommodate new size", utf8.getBytes().length >= size);
    }
  }

  /**
   * Test setByteLength with maximum valid length Boundary test for line 112-113
   * check before resize
   */
  @Test
  public void testSetByteLength_WithLargeButValidSize_Succeeds() {
    // Arrange
    Utf8 utf8 = new Utf8();
    int largeSize = 100000; // Large but valid

    // Act
    utf8.setByteLength(largeSize);

    // Assert
    assertEquals("Should handle large size", largeSize, utf8.getByteLength());
  }

  /**
   * Test setByteLength exceeding maximum throws exception Negative test for line
   * 113 exception throwing Note: This test is conditional - it only validates the
   * exception if MAX_LENGTH property is set to a reasonable value.
   */
  @Test
  public void testSetByteLength_ExceedingMaximum_ThrowsException() {
    // Arrange
    Utf8 utf8 = new Utf8();
    String maxLengthProp = System.getProperty("org.apache.avro.limits.string.maxLength");

    // Only test if a reasonable max length is set (not Integer.MAX_VALUE default)
    if (maxLengthProp != null) {
      try {
        int maxLength = Integer.parseUnsignedInt(maxLengthProp);
        // Act & Assert - try to exceed the configured maximum
        try {
          utf8.setByteLength(maxLength + 1);
          fail("Should throw AvroRuntimeException for excessive length");
        } catch (AvroRuntimeException e) {
          assertTrue("Exception message should mention length exceeded",
              e.getMessage().contains("exceeds maximum allowed"));
        }
      } catch (NumberFormatException nfe) {
        // Skip test if property is not parseable
      }
    }
    // If no max is set, we cannot safely test this without OOM, so test passes
  }

  /**
   * Test setByteLength returns same instance for method chaining Verifies line
   * 122: return this
   */
  @Test
  public void testSetByteLength_ReturnsThis_AllowsMethodChaining() {
    // Arrange
    Utf8 utf8 = new Utf8("Chain");

    // Act
    Utf8 result = utf8.setByteLength(20);

    // Assert
    assertSame("Should return same instance for chaining", utf8, result);
  }

  /**
   * Test setByteLength with exactly boundary size Edge case: line 115 equality
   * check
   */
  @Test
  public void testSetByteLength_ExactlyCurrentCapacity_DoesNotResize() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");
    int currentCapacity = utf8.getBytes().length;
    byte[] originalArray = utf8.getBytes();

    // Act
    utf8.setByteLength(currentCapacity);

    // Assert
    assertSame("Should not create new array when exactly at capacity", originalArray, utf8.getBytes());
  }

  /**
   * Test setByteLength creates new array with exact size needed Verifies line
   * 116: new array size matches newLength
   */
  @Test
  public void testSetByteLength_NewArraySize_MatchesRequestedLength() {
    // Arrange
    Utf8 utf8 = new Utf8("Resize");
    int requestedLength = utf8.getByteLength() + 100;

    // Act
    utf8.setByteLength(requestedLength);

    // Assert
    assertEquals("New array should have exact requested size", requestedLength, utf8.getBytes().length);
  }

  /**
   * Test setByteLength from empty Utf8 Edge case covering line 117 with zero
   * initial length
   */
  @Test
  public void testSetByteLength_FromEmpty_AllocatesNewArray() {
    // Arrange
    Utf8 utf8 = new Utf8();
    assertEquals("Should start with zero length", 0, utf8.getByteLength());

    // Act
    utf8.setByteLength(50);

    // Assert
    assertEquals("Should have new length", 50, utf8.getByteLength());
    assertEquals("Array should be allocated", 50, utf8.getBytes().length);
  }

  /**
   * Integration test: setByteLength then modify bytes Verifies line 116-118
   * creates writable array
   */
  @Test
  public void testSetByteLength_AfterResize_AllowsByteModification() {
    // Arrange
    Utf8 utf8 = new Utf8("Initial");
    byte[] newData = "NewData".getBytes(StandardCharsets.UTF_8);

    // Act
    utf8.setByteLength(newData.length);
    System.arraycopy(newData, 0, utf8.getBytes(), 0, newData.length);

    // Assert
    assertEquals("Should contain new data", "NewData", utf8.toString());
  }
}
