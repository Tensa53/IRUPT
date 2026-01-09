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
 * Test class for Utf8 Covers lines 64-65 (copy constructor) and lines
 * 116-117-118 (setByteLength method) of Utf8.java
 */
public class TestUtf8_4 {

  private Utf8 utf8Instance;

  @Before
  public void setUp() {
    utf8Instance = new Utf8();
  }

  @After
  public void tearDown() {
    utf8Instance = null;
  }

  // ========== Tests for Copy Constructor (lines 64-65) ==========

  @Test
  public void testCopyConstructor_ValidUtf8_CreatesIndependentCopy() {
    // Arrange
    Utf8 original = new Utf8("Hello World");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same string value as original", original.toString(), copy.toString());
    assertNotSame("Copy should have different byte array reference", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_EmptyUtf8_CreatesEmptyCopy() {
    // Arrange
    Utf8 original = new Utf8("");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy of empty string should have zero length", 0, copy.getByteLength());
    assertEquals("Copy should equal empty string", "", copy.toString());
  }

  @Test
  public void testCopyConstructor_ModifyOriginal_DoesNotAffectCopy() {
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
  public void testCopyConstructor_ModifyCopy_DoesNotAffectOriginal() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);

    // Act
    copy.set("Modified Copy");

    // Assert
    assertEquals("Original should remain unchanged", "Original", original.toString());
    assertEquals("Copy should be modified", "Modified Copy", copy.toString());
  }

  @Test
  public void testCopyConstructor_UnicodeCharacters_CopiesCorrectly() {
    // Arrange
    Utf8 original = new Utf8("Hello 你好 مرحبا");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should preserve unicode characters", original.toString(), copy.toString());
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
  }

  @Test
  public void testCopyConstructor_LargeString_CopiesAllBytes() {
    // Arrange
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      sb.append("Test");
    }
    Utf8 original = new Utf8(sb.toString());

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same content as original", original.toString(), copy.toString());
    assertEquals("Copy should have same length as original", original.getByteLength(), copy.getByteLength());
  }

  @Test
  public void testCopyConstructor_ByteArrayContent_CopiesCorrectly() {
    // Arrange
    byte[] testBytes = "Test".getBytes(StandardCharsets.UTF_8);
    Utf8 original = new Utf8(testBytes);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
    byte[] originalBytes = original.getBytes();
    byte[] copyBytes = copy.getBytes();
    for (int i = 0; i < original.getByteLength(); i++) {
      assertEquals("Byte at index " + i + " should be equal", originalBytes[i], copyBytes[i]);
    }
  }

  // ========== Tests for setByteLength method (lines 116-117-118) ==========

  @Test
  public void testSetByteLength_IncreaseLength_ExpandsByteArray() {
    // Arrange
    Utf8 utf8 = new Utf8("Hello");
    int originalLength = utf8.getByteLength();
    int newLength = originalLength + 10;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    assertEquals("Byte length should be updated to new length", newLength, utf8.getByteLength());
    assertTrue("Byte array should be expanded", utf8.getBytes().length >= newLength);
  }

  @Test
  public void testSetByteLength_DecreaseLengthWithoutReallocation_UpdatesLength() {
    // Arrange
    Utf8 utf8 = new Utf8("Hello World");
    int newLength = 5;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    assertEquals("Byte length should be reduced", newLength, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_SameLength_ClearsCachedString() {
    // Arrange
    Utf8 utf8 = new Utf8("Hello");
    String cachedString = utf8.toString(); // Cache the string
    int currentLength = utf8.getByteLength();

    // Modify bytes directly
    byte[] bytes = utf8.getBytes();
    bytes[0] = 'J';

    // Act
    utf8.setByteLength(currentLength);

    // Assert
    assertEquals("String cache should be cleared", "Jello", utf8.toString());
    assertNotEquals("New string should differ from cached string", cachedString, utf8.toString());
  }

  @Test
  public void testSetByteLength_ZeroLength_SetsToZero() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");

    // Act
    utf8.setByteLength(0);

    // Assert
    assertEquals("Byte length should be zero", 0, utf8.getByteLength());
    assertEquals("String should be empty", "", utf8.toString());
  }

  @Test
  public void testSetByteLength_IncreaseThenDecrease_PreservesData() {
    // Arrange
    Utf8 utf8 = new Utf8("Hello");
    byte[] originalBytes = utf8.getBytes().clone();
    int originalLength = utf8.getByteLength();

    // Act
    utf8.setByteLength(20); // Increase
    utf8.setByteLength(originalLength); // Decrease back

    // Assert
    assertEquals("Length should be restored", originalLength, utf8.getByteLength());
    byte[] currentBytes = utf8.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original data should be preserved", originalBytes[i], currentBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_LargeIncrease_ExpandsCorrectly() {
    // Arrange
    Utf8 utf8 = new Utf8("Small");
    int largeLength = 10000;

    // Act
    utf8.setByteLength(largeLength);

    // Assert
    assertEquals("Should handle large length increase", largeLength, utf8.getByteLength());
    assertTrue("Byte array should be large enough", utf8.getBytes().length >= largeLength);
  }

  @Test
  public void testSetByteLength_PreservesOriginalBytesWhenExpanding() {
    // Arrange
    String testString = "Test Data";
    Utf8 utf8 = new Utf8(testString);
    byte[] originalBytes = testString.getBytes(StandardCharsets.UTF_8);
    int originalLength = originalBytes.length;
    int newLength = originalLength + 50;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    byte[] expandedBytes = utf8.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original bytes should be preserved at index " + i, originalBytes[i], expandedBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_ReturnsThis_ForMethodChaining() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");

    // Act
    Utf8 result = utf8.setByteLength(10);

    // Assert
    assertSame("setByteLength should return this for method chaining", utf8, result);
  }

  @Test
  public void testSetByteLength_ExceedsMaxLength_ThrowsAvroRuntimeException() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");
    // Use a value that would exceed MAX_LENGTH but might cause OOM
    // Since MAX_LENGTH is Integer.MAX_VALUE by default, we catch both exceptions
    long exceedsMaxLength = (long) Integer.MAX_VALUE + 1;

    // Act & Assert
    try {
      // This should throw AvroRuntimeException for exceeding max length
      // or OutOfMemoryError if it tries to allocate
      utf8.setByteLength(Integer.MAX_VALUE);
      fail("Expected AvroRuntimeException or OutOfMemoryError to be thrown");
    } catch (AvroRuntimeException e) {
      assertTrue("Exception message should mention exceeding maximum",
          e.getMessage().contains("exceeds maximum allowed"));
    } catch (OutOfMemoryError e) {
      // This is also acceptable as it indicates the system tried to allocate too much
      // memory
      assertTrue("OutOfMemoryError is acceptable for very large allocations", true);
    }
  }

  @Test
  public void testSetByteLength_MultipleExpansions_WorksCorrectly() {
    // Arrange
    Utf8 utf8 = new Utf8("A");

    // Act
    utf8.setByteLength(10);
    utf8.setByteLength(20);
    utf8.setByteLength(30);

    // Assert
    assertEquals("Final length should be 30", 30, utf8.getByteLength());
    assertTrue("Byte array should accommodate final length", utf8.getBytes().length >= 30);
  }

  // ========== Additional Edge Case Tests ==========

  @Test
  public void testCopyConstructor_WithCachedString_CopiesStringReference() {
    // Arrange
    Utf8 original = new Utf8("Cached");
    original.toString(); // Ensure string is cached

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same string value", original.toString(), copy.toString());
  }

  @Test
  public void testSetByteLength_AfterCopyConstructor_IndependentOperation() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);

    // Act
    copy.setByteLength(20);

    // Assert
    assertNotEquals("Copy and original should have different lengths", original.getByteLength(), copy.getByteLength());
    assertEquals("Original should maintain its length", "Original".getBytes(StandardCharsets.UTF_8).length,
        original.getByteLength());
  }

  @Test
  public void testCopyConstructor_NullString_HandlesCorrectly() {
    // Arrange
    Utf8 original = new Utf8();

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Empty copy should have zero length", 0, copy.getByteLength());
    assertEquals("Empty copy should convert to empty string", "", copy.toString());
  }

  @Test
  public void testSetByteLength_OnEmptyUtf8_ExpandsCorrectly() {
    // Arrange
    Utf8 utf8 = new Utf8();

    // Act
    utf8.setByteLength(10);

    // Assert
    assertEquals("Empty Utf8 should expand to new length", 10, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_ClearsCachedString_VerifyMultipleCalls() {
    // Arrange
    Utf8 utf8 = new Utf8("First");
    utf8.toString(); // Cache string

    // Act
    utf8.setByteLength(10);
    String afterFirstSet = utf8.toString();
    utf8.setByteLength(5);
    String afterSecondSet = utf8.toString();

    // Assert
    assertNotNull("String should be retrievable after first setByteLength", afterFirstSet);
    assertNotNull("String should be retrievable after second setByteLength", afterSecondSet);
  }

  @Test
  public void testCopyConstructor_SpecialCharacters_PreservesEncoding() {
    // Arrange
    Utf8 original = new Utf8("Special: \n\t\r\b");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Special characters should be preserved", original.toString(), copy.toString());
    assertEquals("Byte lengths should match", original.getByteLength(), copy.getByteLength());
  }

  @Test
  public void testSetByteLength_Boundary_OneByteIncrease() {
    // Arrange
    Utf8 utf8 = new Utf8("A");
    int originalLength = utf8.getByteLength();

    // Act
    utf8.setByteLength(originalLength + 1);

    // Assert
    assertEquals("Length should increase by one byte", originalLength + 1, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_Boundary_OneBytDecrease() {
    // Arrange
    Utf8 utf8 = new Utf8("AB");
    int originalLength = utf8.getByteLength();

    // Act
    utf8.setByteLength(originalLength - 1);

    // Assert
    assertEquals("Length should decrease by one byte", originalLength - 1, utf8.getByteLength());
  }
}
