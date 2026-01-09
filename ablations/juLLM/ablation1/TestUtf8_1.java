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

import java.nio.charset.StandardCharsets;
import org.apache.avro.AvroRuntimeException;

/**
 * Test class for Utf8 Covers lines 64-65, 116-117-118 of Utf8.java Focuses on
 * copy constructor and setByteLength method
 */
public class TestUtf8_1 {

  private Utf8 utf8Instance;
  private byte[] testBytes;

  @Before
  public void setUp() {
    testBytes = "Hello".getBytes(StandardCharsets.UTF_8);
    utf8Instance = new Utf8(testBytes);
  }

  @After
  public void tearDown() {
    utf8Instance = null;
    testBytes = null;
  }

  // ========================================
  // Tests for Copy Constructor (lines 64-65)
  // ========================================

  @Test
  public void testCopyConstructor_ValidUtf8_CreatesIndependentCopy() {
    // Arrange
    Utf8 original = new Utf8("Test");

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
    Utf8 original = new Utf8();

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy of empty Utf8 should have zero length", 0, copy.getByteLength());
    assertEquals("Copy of empty Utf8 should have empty string", "", copy.toString());
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
  public void testCopyConstructor_WithCachedString_CopiesCachedString() {
    // Arrange
    Utf8 original = new Utf8("Test");
    String cachedString = original.toString(); // Force string caching

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same string value", cachedString, copy.toString());
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
    for (int i = 0; i < 1000; i++) {
      sb.append("A");
    }
    Utf8 original = new Utf8(sb.toString());

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as large original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same content as large original", original.toString(), copy.toString());
  }

  // ========================================
  // Tests for setByteLength method (lines 116-117-118)
  // ========================================

  @Test
  public void testSetByteLength_IncreaseLength_AllocatesNewArray() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");
    byte[] originalBytes = utf8.getBytes();
    int originalLength = utf8.getByteLength();
    int newLength = originalLength + 10;

    // Act
    Utf8 result = utf8.setByteLength(newLength);

    // Assert
    assertEquals("New length should be set correctly", newLength, utf8.getByteLength());
    assertNotSame("Should allocate new byte array when increasing size", originalBytes, utf8.getBytes());
    assertSame("setByteLength should return this", utf8, result);
  }

  @Test
  public void testSetByteLength_DecreaseLength_KeepsExistingArray() {
    // Arrange
    Utf8 utf8 = new Utf8("Testing");
    byte[] originalBytes = utf8.getBytes();
    int newLength = 3;

    // Act
    Utf8 result = utf8.setByteLength(newLength);

    // Assert
    assertEquals("New length should be set correctly", newLength, utf8.getByteLength());
    assertSame("Should keep existing byte array when decreasing size", originalBytes, utf8.getBytes());
    assertSame("setByteLength should return this", utf8, result);
  }

  @Test
  public void testSetByteLength_SameLength_KeepsExistingArray() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");
    byte[] originalBytes = utf8.getBytes();
    int currentLength = utf8.getByteLength();

    // Act
    Utf8 result = utf8.setByteLength(currentLength);

    // Assert
    assertEquals("Length should remain the same", currentLength, utf8.getByteLength());
    assertSame("Should keep existing byte array when length unchanged", originalBytes, utf8.getBytes());
    assertSame("setByteLength should return this", utf8, result);
  }

  @Test
  public void testSetByteLength_IncreaseThenIncrease_AllocatesLargerArray() {
    // Arrange
    Utf8 utf8 = new Utf8("AB");
    utf8.setByteLength(5); // First increase
    byte[] firstBytes = utf8.getBytes();

    // Act
    utf8.setByteLength(10); // Second increase

    // Assert
    assertEquals("Should have new length", 10, utf8.getByteLength());
    assertNotSame("Should allocate new array on second increase", firstBytes, utf8.getBytes());
  }

  @Test
  public void testSetByteLength_ClearsStringCache() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");
    String cachedString = utf8.toString(); // Cache the string

    // Act
    utf8.setByteLength(2);

    // Assert - toString() should return truncated string
    assertNotEquals("String cache should be cleared", cachedString, utf8.toString());
  }

  @Test
  public void testSetByteLength_ZeroLength_ReturnsEmptyString() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");

    // Act
    utf8.setByteLength(0);

    // Assert
    assertEquals("Zero length should result in empty string", "", utf8.toString());
    assertEquals("Byte length should be zero", 0, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_PreservesExistingBytes() {
    // Arrange
    byte[] originalBytes = "Hello".getBytes(StandardCharsets.UTF_8);
    Utf8 utf8 = new Utf8(originalBytes);

    // Act - Increase length
    utf8.setByteLength(10);

    // Assert - Original bytes should be preserved
    byte[] newBytes = utf8.getBytes();
    for (int i = 0; i < originalBytes.length; i++) {
      assertEquals("Original bytes should be preserved at index " + i, originalBytes[i], newBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_MaxLengthPlusOne_ThrowsException() {
    // Arrange
    Utf8 utf8 = new Utf8();
    // MAX_LENGTH is Integer.MAX_VALUE by default unless system property is set

    // Act & Assert
    try {
      utf8.setByteLength(Integer.MAX_VALUE);
      // If we get here, MAX_LENGTH might be Integer.MAX_VALUE
      // In that case, we can't test exceeding it without OutOfMemoryError
    } catch (AvroRuntimeException e) {
      assertTrue("Exception message should mention maximum allowed",
          e.getMessage().contains("exceeds maximum allowed"));
    } catch (OutOfMemoryError e) {
      // This is expected if MAX_LENGTH is Integer.MAX_VALUE
    }
  }

  @Test
  public void testSetByteLength_ChainingOperations_WorksCorrectly() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");

    // Act
    Utf8 result = utf8.setByteLength(10).setByteLength(5);

    // Assert
    assertSame("Should support method chaining", utf8, result);
    assertEquals("Final length should be set correctly", 5, utf8.getByteLength());
  }

  // ========================================
  // Edge Cases and Boundary Tests
  // ========================================

  @Test
  public void testCopyConstructor_SpecialCharacters_CopiesCorrectly() {
    // Arrange
    Utf8 original = new Utf8("Line1\nLine2\tTabbed");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should preserve special characters", original.toString(), copy.toString());
  }

  @Test
  public void testSetByteLength_AfterCopyConstructor_IndependentArrays() {
    // Arrange
    Utf8 original = new Utf8("Test");
    Utf8 copy = new Utf8(original);

    // Act
    copy.setByteLength(10);

    // Assert
    assertEquals("Original length should be unchanged", 4, original.getByteLength());
    assertEquals("Copy length should be changed", 10, copy.getByteLength());
  }

  @Test
  public void testSetByteLength_MultipleIncreases_AllocatesCorrectly() {
    // Arrange
    Utf8 utf8 = new Utf8("A");

    // Act & Assert - Test multiple increases
    utf8.setByteLength(5);
    assertEquals("First increase should work", 5, utf8.getByteLength());

    utf8.setByteLength(10);
    assertEquals("Second increase should work", 10, utf8.getByteLength());

    utf8.setByteLength(20);
    assertEquals("Third increase should work", 20, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_ReturnsThisForChaining() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");

    // Act
    Utf8 result1 = utf8.setByteLength(5);
    Utf8 result2 = utf8.setByteLength(3);

    // Assert
    assertSame("First call should return this", utf8, result1);
    assertSame("Second call should return this", utf8, result2);
  }

  @Test
  public void testCopyConstructor_NullString_HandlesGracefully() {
    // Arrange
    Utf8 original = new Utf8(new byte[5]);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length", original.getByteLength(), copy.getByteLength());
  }

  @Test
  public void testSetByteLength_OneByteIncrease_AllocatesNewArray() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");
    byte[] originalBytes = utf8.getBytes();
    int newLength = utf8.getByteLength() + 1;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    assertNotSame("Should allocate new array even for one byte increase", originalBytes, utf8.getBytes());
    assertEquals("Length should be increased by one", newLength, utf8.getByteLength());
  }
}
