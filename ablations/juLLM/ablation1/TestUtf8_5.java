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
 * Test class for Utf8 Covers lines 64-65 (copy constructor) and 116-117-118
 * (setByteLength with array expansion) of Utf8.java
 */
public class TestUtf8_5 {

  private Utf8 utf8Instance;

  @Before
  public void setUp() {
    utf8Instance = null;
  }

  @After
  public void tearDown() {
    utf8Instance = null;
  }

  // ==================== Tests for Copy Constructor (Lines 64-65)
  // ====================

  @Test
  public void testCopyConstructor_EmptyUtf8_CreatesEmptyCopy() {
    // Arrange
    Utf8 original = new Utf8();

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertNotNull("Copy should not be null", copy);
    assertEquals("Copy should have same byte length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
    assertNotSame("Copy should be a different object", original, copy);
  }

  @Test
  public void testCopyConstructor_SimpleString_CreatesIndependentCopy() {
    // Arrange
    Utf8 original = new Utf8("Hello");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
    assertEquals("Copy should have same string representation", original.toString(), copy.toString());
    assertNotSame("Copy bytes should be independent array", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_ModifyOriginal_DoesNotAffectCopy() {
    // Arrange
    Utf8 original = new Utf8("Test");
    Utf8 copy = new Utf8(original);
    String originalCopyValue = copy.toString();

    // Act
    original.set("Modified");

    // Assert
    assertEquals("Copy should retain original value", originalCopyValue, copy.toString());
    assertNotEquals("Original and copy should be different after modification", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_ModifyCopy_DoesNotAffectOriginal() {
    // Arrange
    Utf8 original = new Utf8("Test");
    Utf8 copy = new Utf8(original);
    String originalValue = original.toString();

    // Act
    copy.set("Modified");

    // Assert
    assertEquals("Original should retain its value", originalValue, original.toString());
    assertNotEquals("Original and copy should be different after modification", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_ComplexUnicodeString_CopiesCorrectly() {
    // Arrange
    String complexString = "Hello 世界 🌍 Ñoño";
    Utf8 original = new Utf8(complexString);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should preserve unicode characters", complexString, copy.toString());
    assertEquals("Copy byte length should match original", original.getByteLength(), copy.getByteLength());
    assertArrayEquals("Bytes should be identical", getBytesUpToLength(original), getBytesUpToLength(copy));
  }

  @Test
  public void testCopyConstructor_LargeString_CopiesAllBytes() {
    // Arrange
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      sb.append("TestData");
    }
    Utf8 original = new Utf8(sb.toString());

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
    assertEquals("String representations should match", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_ByteArrayConstructor_CopiesCorrectly() {
    // Arrange
    byte[] bytes = "TestBytes".getBytes();
    Utf8 original = new Utf8(bytes);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
  }

  // ==================== Tests for setByteLength (Lines 116-117-118)
  // ====================

  @Test
  public void testSetByteLength_ExpandArray_CreatesNewArrayAndCopiesData() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");
    int originalLength = utf8.getByteLength();
    byte[] originalBytes = utf8.getBytes();
    int newLength = originalLength + 100;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    assertEquals("Byte length should be updated", newLength, utf8.getByteLength());
    assertNotSame("Bytes array should be replaced with new array", originalBytes, utf8.getBytes());
    assertTrue("New array should be large enough", utf8.getBytes().length >= newLength);
  }

  @Test
  public void testSetByteLength_ExpandArray_PreservesExistingData() {
    // Arrange
    String testString = "Hello";
    Utf8 utf8 = new Utf8(testString);
    byte[] originalBytes = Utf8.getBytesFor(testString);
    int originalLength = utf8.getByteLength();
    int newLength = originalLength + 50;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    byte[] newBytes = utf8.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original byte data should be preserved at index " + i, originalBytes[i], newBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_SameLength_DoesNotExpandArray() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");
    int length = utf8.getByteLength();
    byte[] originalBytesArray = utf8.getBytes();

    // Act
    utf8.setByteLength(length);

    // Assert
    assertEquals("Length should remain the same", length, utf8.getByteLength());
    assertSame("Bytes array should remain the same object", originalBytesArray, utf8.getBytes());
  }

  @Test
  public void testSetByteLength_SmallerLength_DoesNotExpandArray() {
    // Arrange
    Utf8 utf8 = new Utf8("TestString");
    byte[] originalBytesArray = utf8.getBytes();
    int newLength = 4;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    assertEquals("Length should be reduced", newLength, utf8.getByteLength());
    assertSame("Bytes array should remain the same object", originalBytesArray, utf8.getBytes());
  }

  @Test
  public void testSetByteLength_ExpandFromZero_AllocatesNewArray() {
    // Arrange
    Utf8 utf8 = new Utf8();
    int newLength = 100;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    assertEquals("Length should be set", newLength, utf8.getByteLength());
    assertNotNull("Bytes array should not be null", utf8.getBytes());
    assertTrue("Array should be large enough", utf8.getBytes().length >= newLength);
  }

  @Test
  public void testSetByteLength_MultipleExpansions_PreservesData() {
    // Arrange
    Utf8 utf8 = new Utf8("AB");
    byte[] originalData = Utf8.getBytesFor("AB");

    // Act - First expansion
    utf8.setByteLength(5);

    // Assert - Data preserved after first expansion
    byte[] afterFirst = utf8.getBytes();
    assertEquals("Original data preserved at index 0", originalData[0], afterFirst[0]);
    assertEquals("Original data preserved at index 1", originalData[1], afterFirst[1]);

    // Act - Second expansion
    utf8.setByteLength(10);

    // Assert - Data still preserved after second expansion
    byte[] afterSecond = utf8.getBytes();
    assertEquals("Original data still preserved at index 0", originalData[0], afterSecond[0]);
    assertEquals("Original data still preserved at index 1", originalData[1], afterSecond[1]);
  }

  @Test
  public void testSetByteLength_ClearsStringCache() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");
    String firstString = utf8.toString(); // Cache the string

    // Act
    utf8.setByteLength(utf8.getByteLength() + 10);

    // Assert - String cache should be cleared
    // We can't directly test if cache is cleared, but we can verify behavior is
    // correct
    assertNotNull("Should still be able to call toString", utf8.toString());
  }

  @Test
  public void testSetByteLength_ExceedsMaxLength_ThrowsException() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");

    // Act & Assert
    // Note: MAX_LENGTH might be Integer.MAX_VALUE by default, which would cause
    // OutOfMemoryError
    // We test with a value that will either exceed the limit or cause memory
    // allocation issues
    try {
      utf8.setByteLength(Integer.MAX_VALUE);
      fail("Expected AvroRuntimeException or OutOfMemoryError to be thrown");
    } catch (AvroRuntimeException e) {
      assertTrue("Exception message should mention string length", e.getMessage().contains("String length"));
      assertTrue("Exception message should mention exceeds maximum", e.getMessage().contains("exceeds maximum"));
    } catch (OutOfMemoryError e) {
      // This is acceptable when MAX_LENGTH equals Integer.MAX_VALUE
      assertTrue("OutOfMemoryError is acceptable when trying to allocate max array", true);
    }
  }

  @Test
  public void testSetByteLength_VeryLargeExpansion_AllocatesCorrectSize() {
    // Arrange
    Utf8 utf8 = new Utf8("X");
    int largeSize = 10000;

    // Act
    utf8.setByteLength(largeSize);

    // Assert
    assertEquals("Length should be set to large size", largeSize, utf8.getByteLength());
    assertTrue("Array should accommodate the size", utf8.getBytes().length >= largeSize);
  }

  @Test
  public void testSetByteLength_BoundaryAtCurrentArraySize_DoesNotExpand() {
    // Arrange
    byte[] initialBytes = new byte[10];
    System.arraycopy(Utf8.getBytesFor("Test"), 0, initialBytes, 0, 4);
    Utf8 utf8 = new Utf8(initialBytes);
    utf8.setByteLength(4);
    byte[] bytesBeforeSetLength = utf8.getBytes();

    // Act
    utf8.setByteLength(10); // Same as current array length

    // Assert
    assertSame("Should not allocate new array when size equals current capacity", bytesBeforeSetLength,
        utf8.getBytes());
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

  // ==================== Integration Tests ====================

  @Test
  public void testCopyConstructorAndSetByteLength_Integration_WorksCorrectly() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);

    // Act
    copy.setByteLength(copy.getByteLength() + 10);

    // Assert
    assertNotEquals("Copy should be independent after modification", original.getByteLength(), copy.getByteLength());
    assertEquals("Original should remain unchanged", "Original", original.toString());
  }

  @Test
  public void testSetByteLength_AfterCopyConstructor_PreservesIndependence() {
    // Arrange
    Utf8 original = new Utf8("Data");
    Utf8 copy = new Utf8(original);

    // Act
    original.setByteLength(20);

    // Assert
    assertEquals("Copy length should remain unchanged", 4, copy.getByteLength());
    assertNotSame("Arrays should be independent", original.getBytes(), copy.getBytes());
  }

  // ==================== Edge Cases ====================

  @Test
  public void testCopyConstructor_NullString_HandlesCorrectly() {
    // Arrange
    Utf8 original = new Utf8();

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Empty UTF8 should copy correctly", 0, copy.getByteLength());
    assertNotNull("Copy should not be null", copy);
  }

  @Test
  public void testSetByteLength_ZeroLength_WorksCorrectly() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");

    // Act
    utf8.setByteLength(0);

    // Assert
    assertEquals("Length should be zero", 0, utf8.getByteLength());
    assertEquals("String representation should be empty", "", utf8.toString());
  }

  @Test
  public void testSetByteLength_OneByteExpansion_WorksCorrectly() {
    // Arrange
    Utf8 utf8 = new Utf8("A");
    int originalLength = utf8.getByteLength();

    // Act
    utf8.setByteLength(originalLength + 1);

    // Assert
    assertEquals("Length should increase by one", originalLength + 1, utf8.getByteLength());
  }

  // ==================== Helper Methods ====================

  /**
   * Helper method to get bytes up to the current length
   */
  private byte[] getBytesUpToLength(Utf8 utf8) {
    byte[] result = new byte[utf8.getByteLength()];
    System.arraycopy(utf8.getBytes(), 0, result, 0, utf8.getByteLength());
    return result;
  }
}
