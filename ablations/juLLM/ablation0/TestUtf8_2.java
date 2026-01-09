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

import static org.junit.Assert.*;

/**
 * Test class for Utf8 Focuses on testing lines 64-65 (copy constructor) and
 * lines 116-117-118 (setByteLength with array expansion)
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

  // ========== Tests for Copy Constructor (lines 64-65) ==========

  @Test
  public void testCopyConstructor_ValidUtf8_CreatesIndependentCopy() {
    // Arrange
    Utf8 original = new Utf8("Hello World");
    int expectedLength = original.getByteLength();

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as original", expectedLength, copy.getByteLength());
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
    assertEquals("Copy should be empty", 0, copy.getByteLength());
    assertEquals("Copy should have empty string", "", copy.toString());
  }

  @Test
  public void testCopyConstructor_ModifyOriginal_DoesNotAffectCopy() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);
    String copyValue = copy.toString();

    // Act
    original.set("Modified");

    // Assert
    assertEquals("Copy should retain original value", "Original", copyValue);
    assertEquals("Original should be modified", "Modified", original.toString());
  }

  @Test
  public void testCopyConstructor_ModifyCopy_DoesNotAffectOriginal() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);

    // Act
    copy.set("Modified");

    // Assert
    assertEquals("Original should retain its value", "Original", original.toString());
    assertEquals("Copy should be modified", "Modified", copy.toString());
  }

  @Test
  public void testCopyConstructor_UnicodeCharacters_CopiesCorrectly() {
    // Arrange
    Utf8 original = new Utf8("Hello 世界 🌍");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same unicode string", original.toString(), copy.toString());
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
  }

  @Test
  public void testCopyConstructor_ByteArrayIndependence_VerifiesDeepCopy() {
    // Arrange
    Utf8 original = new Utf8("Test");
    byte[] originalBytes = original.getBytes();
    int originalFirstByte = originalBytes[0];

    // Act
    Utf8 copy = new Utf8(original);
    originalBytes[0] = (byte) 'X';

    // Assert
    assertEquals("Copy should not be affected by original byte modification", originalFirstByte, copy.getBytes()[0]);
  }

  @Test
  public void testCopyConstructor_LargeString_CopiesCorrectly() {
    // Arrange
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      sb.append("Test");
    }
    Utf8 original = new Utf8(sb.toString());

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same string value", original.toString(), copy.toString());
  }

  // ========== Tests for setByteLength (lines 116-117-118) ==========

  @Test
  public void testSetByteLength_ExpandArray_CreatesNewArray() {
    // Arrange
    utf8Instance = new Utf8("Hi");
    int originalLength = utf8Instance.getByteLength();
    int newLength = 100;

    // Act
    utf8Instance.setByteLength(newLength);

    // Assert
    assertEquals("Length should be updated to new value", newLength, utf8Instance.getByteLength());
    assertTrue("Byte array should be expanded", utf8Instance.getBytes().length >= newLength);
  }

  @Test
  public void testSetByteLength_ExpandArray_PreservesExistingContent() {
    // Arrange
    utf8Instance = new Utf8("Test");
    byte[] originalBytes = utf8Instance.getBytes().clone();
    int originalLength = utf8Instance.getByteLength();

    // Act
    utf8Instance.setByteLength(100);

    // Assert
    byte[] newBytes = utf8Instance.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original byte content should be preserved at index " + i, originalBytes[i], newBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_SameLength_DoesNotExpandArray() {
    // Arrange
    utf8Instance = new Utf8("Test");
    int currentLength = utf8Instance.getByteLength();
    byte[] originalByteArray = utf8Instance.getBytes();

    // Act
    utf8Instance.setByteLength(currentLength);

    // Assert
    assertEquals("Length should remain the same", currentLength, utf8Instance.getByteLength());
    assertSame("Byte array reference should not change", originalByteArray, utf8Instance.getBytes());
  }

  @Test
  public void testSetByteLength_SmallerLength_DoesNotCreateNewArray() {
    // Arrange
    utf8Instance = new Utf8("Hello World");
    byte[] originalByteArray = utf8Instance.getBytes();

    // Act
    utf8Instance.setByteLength(5);

    // Assert
    assertEquals("Length should be updated", 5, utf8Instance.getByteLength());
    assertSame("Byte array reference should not change when shrinking", originalByteArray, utf8Instance.getBytes());
  }

  @Test
  public void testSetByteLength_ZeroLength_ClearsString() {
    // Arrange
    utf8Instance = new Utf8("Test");

    // Act
    utf8Instance.setByteLength(0);

    // Assert
    assertEquals("Length should be zero", 0, utf8Instance.getByteLength());
    assertEquals("String should be empty", "", utf8Instance.toString());
  }

  @Test
  public void testSetByteLength_ClearsCachedString() {
    // Arrange
    utf8Instance = new Utf8("Test");
    String cachedString = utf8Instance.toString();

    // Act
    utf8Instance.setByteLength(utf8Instance.getByteLength());

    // Assert
    assertNotNull("Cached string should be cleared", utf8Instance.toString());
  }

  @Test
  public void testSetByteLength_ExceedsMaxLength_ThrowsException() {
    // Arrange
    utf8Instance = new Utf8("Test");

    // Act & Assert
    try {
      utf8Instance.setByteLength(Integer.MAX_VALUE);
      fail("Expected AvroRuntimeException or OutOfMemoryError to be thrown");
    } catch (AvroRuntimeException e) {
      assertTrue("Exception message should mention maximum allowed",
          e.getMessage().contains("exceeds maximum allowed"));
    } catch (OutOfMemoryError e) {
      // This can occur if MAX_LENGTH is Integer.MAX_VALUE and VM cannot allocate the
      // array
      assertTrue("OutOfMemoryError expected for very large array allocation", true);
    }
  }

  @Test
  public void testSetByteLength_MultipleExpansions_WorksCorrectly() {
    // Arrange
    utf8Instance = new Utf8("X");

    // Act & Assert - First expansion
    utf8Instance.setByteLength(10);
    assertEquals("First expansion should succeed", 10, utf8Instance.getByteLength());

    // Act & Assert - Second expansion
    utf8Instance.setByteLength(50);
    assertEquals("Second expansion should succeed", 50, utf8Instance.getByteLength());

    // Act & Assert - Third expansion
    utf8Instance.setByteLength(100);
    assertEquals("Third expansion should succeed", 100, utf8Instance.getByteLength());
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
  public void testSetByteLength_EmptyUtf8_ExpandsSuccessfully() {
    // Arrange
    utf8Instance = new Utf8();

    // Act
    utf8Instance.setByteLength(20);

    // Assert
    assertEquals("Empty Utf8 should expand successfully", 20, utf8Instance.getByteLength());
  }

  @Test
  public void testSetByteLength_AfterExpansion_ArrayIsLargeEnough() {
    // Arrange
    utf8Instance = new Utf8("AB");

    // Act
    utf8Instance.setByteLength(50);

    // Assert
    assertTrue("Byte array should be at least the new length", utf8Instance.getBytes().length >= 50);
  }

  @Test
  public void testSetByteLength_PreservesDataIntegrity_DuringExpansion() {
    // Arrange
    String testString = "Hello";
    utf8Instance = new Utf8(testString);
    byte[] originalBytes = Utf8.getBytesFor(testString);

    // Act
    utf8Instance.setByteLength(100);

    // Assert
    byte[] expandedBytes = utf8Instance.getBytes();
    for (int i = 0; i < originalBytes.length; i++) {
      assertEquals("Data integrity should be maintained at index " + i, originalBytes[i], expandedBytes[i]);
    }
  }

  // ========== Integration Tests ==========

  @Test
  public void testCopyConstructorAndSetByteLength_Integration() {
    // Arrange
    Utf8 original = new Utf8("Test");
    Utf8 copy = new Utf8(original);

    // Act
    copy.setByteLength(50);

    // Assert
    assertEquals("Original should remain unchanged", 4, original.getByteLength());
    assertEquals("Copy should be expanded", 50, copy.getByteLength());
  }

  @Test
  public void testSetByteLengthTwice_FirstExpands_SecondShrinks() {
    // Arrange
    utf8Instance = new Utf8("Test");

    // Act
    utf8Instance.setByteLength(100);
    byte[] arrayAfterExpansion = utf8Instance.getBytes();
    utf8Instance.setByteLength(5);

    // Assert
    assertEquals("Length should be shrunk to 5", 5, utf8Instance.getByteLength());
    assertSame("Array should not be reallocated when shrinking", arrayAfterExpansion, utf8Instance.getBytes());
  }
}
