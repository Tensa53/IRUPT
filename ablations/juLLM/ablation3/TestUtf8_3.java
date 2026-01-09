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
 * Comprehensive JUnit 4.13 test class for Utf8. Focuses on testing the copy
 * constructor (lines 64-65) and setByteLength method (lines 116-117-118).
 */
public class TestUtf8_3 {

  private Utf8 emptyUtf8;
  private Utf8 simpleUtf8;
  private Utf8 multiByteUtf8;
  private Utf8 longUtf8;

  @Before
  public void setUp() {
    emptyUtf8 = new Utf8("");
    simpleUtf8 = new Utf8("Hello");
    multiByteUtf8 = new Utf8("こんにちは"); // Japanese "Hello" - multi-byte UTF-8
    longUtf8 = new Utf8("This is a longer string for testing purposes");
  }

  @After
  public void tearDown() {
    emptyUtf8 = null;
    simpleUtf8 = null;
    multiByteUtf8 = null;
    longUtf8 = null;
  }

  // ===== Copy Constructor Tests (Lines 64-65) =====

  @Test
  public void testCopyConstructor_WithEmptyUtf8_CreatesIndependentCopy() {
    // Arrange
    Utf8 original = new Utf8("");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertNotNull("Copy should not be null", copy);
    assertEquals("Copy should have same length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same content", original.toString(), copy.toString());
    assertNotSame("Copy should be different object", original, copy);
    assertNotSame("Copy should have different byte array", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_WithSimpleString_CreatesIndependentCopy() {
    // Arrange
    Utf8 original = new Utf8("Hello");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertNotNull("Copy should not be null", copy);
    assertEquals("Copy should have same length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same content", original.toString(), copy.toString());
    assertNotSame("Copy should be different object", original, copy);
    assertNotSame("Copy should have different byte array", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_WithMultiByteUtf8_CreatesIndependentCopy() {
    // Arrange
    Utf8 original = new Utf8("こんにちは");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertNotNull("Copy should not be null", copy);
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same string content", original.toString(), copy.toString());
    assertNotSame("Copy should be different object", original, copy);
    assertNotSame("Copy should have different byte array", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_ModifyingOriginal_DoesNotAffectCopy() {
    // Arrange
    Utf8 original = new Utf8("Hello");
    Utf8 copy = new Utf8(original);
    String originalContent = copy.toString();

    // Act
    original.set("World");

    // Assert
    assertEquals("Copy should retain original content", originalContent, copy.toString());
    assertNotEquals("Original should have different content", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_ModifyingCopy_DoesNotAffectOriginal() {
    // Arrange
    Utf8 original = new Utf8("Hello");
    Utf8 copy = new Utf8(original);
    String originalContent = original.toString();

    // Act
    copy.set("World");

    // Assert
    assertEquals("Original should retain its content", originalContent, original.toString());
    assertNotEquals("Copy should have different content", copy.toString(), original.toString());
  }

  @Test
  public void testCopyConstructor_WithLongString_CreatesCorrectCopy() {
    // Arrange
    String longString = "This is a very long string that contains many characters to test the copy constructor with larger data";
    Utf8 original = new Utf8(longString);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same content", original.toString(), copy.toString());
    assertNotSame("Copy should have different byte array", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_ByteArrayCopyIsCorrect_AllBytesMatch() {
    // Arrange
    Utf8 original = new Utf8("Test123");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    byte[] originalBytes = original.getBytes();
    byte[] copyBytes = copy.getBytes();
    for (int i = 0; i < original.getByteLength(); i++) {
      assertEquals("Byte at index " + i + " should match", originalBytes[i], copyBytes[i]);
    }
  }

  @Test
  public void testCopyConstructor_WithSpecialCharacters_CreatesCorrectCopy() {
    // Arrange
    Utf8 original = new Utf8("Special: !@#$%^&*()_+-=[]{}|;':\",./<>?");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same content", original.toString(), copy.toString());
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
  }

  // ===== setByteLength Tests (Lines 116-117-118) =====

  @Test
  public void testSetByteLength_WithSmallerLength_KeepsExistingArray() {
    // Arrange
    Utf8 utf8 = new Utf8("Hello");
    byte[] originalBytes = utf8.getBytes();
    int newLength = 3;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    assertEquals("Length should be updated", newLength, utf8.getByteLength());
    assertSame("Should reuse existing byte array", originalBytes, utf8.getBytes());
  }

  @Test
  public void testSetByteLength_WithSameLength_KeepsExistingArray() {
    // Arrange
    Utf8 utf8 = new Utf8("Hello");
    byte[] originalBytes = utf8.getBytes();
    int originalLength = utf8.getByteLength();

    // Act
    utf8.setByteLength(originalLength);

    // Assert
    assertEquals("Length should remain same", originalLength, utf8.getByteLength());
    assertSame("Should reuse existing byte array", originalBytes, utf8.getBytes());
  }

  @Test
  public void testSetByteLength_WithLargerLength_CreatesNewArray() {
    // Arrange
    Utf8 utf8 = new Utf8("Hi");
    byte[] originalBytes = utf8.getBytes();
    int originalLength = utf8.getByteLength();
    int newLength = 10;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    assertEquals("Length should be updated", newLength, utf8.getByteLength());
    assertNotSame("Should create new byte array", originalBytes, utf8.getBytes());
    assertTrue("New array should be larger", utf8.getBytes().length >= newLength);
  }

  @Test
  public void testSetByteLength_WithLargerLength_CopiesExistingBytes() {
    // Arrange
    byte[] expectedBytes = "Test".getBytes(StandardCharsets.UTF_8);
    Utf8 utf8 = new Utf8("Test");
    int originalLength = utf8.getByteLength();
    int newLength = 20;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    byte[] newBytes = utf8.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original byte at index " + i + " should be copied", expectedBytes[i], newBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_WithZeroLength_Works() {
    // Arrange
    Utf8 utf8 = new Utf8("Hello");

    // Act
    utf8.setByteLength(0);

    // Assert
    assertEquals("Length should be zero", 0, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_ClearsCachedString() {
    // Arrange
    Utf8 utf8 = new Utf8("Hello");
    String cachedString = utf8.toString(); // Force string caching

    // Act
    utf8.setByteLength(utf8.getByteLength());

    // Assert - toString should work even after clearing cache
    assertNotNull("toString should work after setByteLength", utf8.toString());
  }

  @Test
  public void testSetByteLength_ReturnsThis_ForMethodChaining() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");

    // Act
    Utf8 result = utf8.setByteLength(10);

    // Assert
    assertSame("Should return same instance for method chaining", utf8, result);
  }

  @Test
  public void testSetByteLength_WithIncreasingSize_AllocatesCorrectly() {
    // Arrange
    Utf8 utf8 = new Utf8();

    // Act & Assert - incrementally increase size
    utf8.setByteLength(5);
    assertEquals("Should handle first increase", 5, utf8.getByteLength());

    utf8.setByteLength(10);
    assertEquals("Should handle second increase", 10, utf8.getByteLength());

    utf8.setByteLength(20);
    assertEquals("Should handle third increase", 20, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_WithVeryLargeLength_AllocatesSuccessfully() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");

    // Act - Use a large but reasonable size that won't cause OutOfMemoryError
    // This tests the array expansion logic on lines 116-117-118
    int largeSize = 100000;
    utf8.setByteLength(largeSize);

    // Assert
    assertEquals("Should handle very large size", largeSize, utf8.getByteLength());
    assertTrue("Should allocate sufficient space", utf8.getBytes().length >= largeSize);

    // Verify original data is preserved
    assertEquals("First byte should be preserved", 'T', utf8.getBytes()[0]);
  }

  @Test
  public void testSetByteLength_FromEmptyUtf8_CanExpand() {
    // Arrange
    Utf8 utf8 = new Utf8();
    assertEquals("Should start with zero length", 0, utf8.getByteLength());

    // Act
    utf8.setByteLength(15);

    // Assert
    assertEquals("Should expand to new length", 15, utf8.getByteLength());
    assertNotNull("Should have valid byte array", utf8.getBytes());
  }

  @Test
  public void testSetByteLength_MultipleExpansions_MaintainsData() {
    // Arrange
    Utf8 utf8 = new Utf8("AB");
    byte[] originalBytes = utf8.getBytes();
    byte firstByte = originalBytes[0];
    byte secondByte = originalBytes[1];

    // Act
    utf8.setByteLength(5);
    utf8.setByteLength(10);
    utf8.setByteLength(20);

    // Assert
    byte[] finalBytes = utf8.getBytes();
    assertEquals("First byte should be preserved", firstByte, finalBytes[0]);
    assertEquals("Second byte should be preserved", secondByte, finalBytes[1]);
  }

  // ===== Integration Tests =====

  @Test
  public void testCopyConstructorAndSetByteLength_Integration_WorkTogether() {
    // Arrange
    Utf8 original = new Utf8("Hello");
    Utf8 copy = new Utf8(original);

    // Act
    copy.setByteLength(10);

    // Assert
    assertNotEquals("Copy length should differ from original", original.getByteLength(), copy.getByteLength());
    assertEquals("Original length should be unchanged", 5, original.getByteLength());
    assertEquals("Copy should have new length", 10, copy.getByteLength());
  }

  @Test
  public void testSetByteLength_AfterCopyConstructor_OriginalUnaffected() {
    // Arrange
    Utf8 original = new Utf8("Test");
    Utf8 copy = new Utf8(original);
    int originalLength = original.getByteLength();

    // Act
    copy.setByteLength(20);

    // Assert
    assertEquals("Original length should be unchanged", originalLength, original.getByteLength());
    assertEquals("Copy should have new length", 20, copy.getByteLength());
  }

  @Test
  public void testCopyConstructor_WithUtf8ModifiedBySetByteLength_CopiesCorrectly() {
    // Arrange
    Utf8 original = new Utf8("Hi");
    original.setByteLength(10); // Expand the original

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as modified original", original.getByteLength(), copy.getByteLength());
    assertNotSame("Copy should have independent byte array", original.getBytes(), copy.getBytes());
  }

  // ===== Edge Cases and Boundary Conditions =====

  @Test
  public void testCopyConstructor_WithEmptyByteArray_HandlesCorrectly() {
    // Arrange
    Utf8 original = new Utf8(new byte[0]);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have zero length", 0, copy.getByteLength());
    assertNotNull("Copy should have non-null byte array", copy.getBytes());
  }

  @Test
  public void testSetByteLength_BoundaryValue_One() {
    // Arrange
    Utf8 utf8 = new Utf8();

    // Act
    utf8.setByteLength(1);

    // Assert
    assertEquals("Should handle length of 1", 1, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_LargeButValidSize_Works() {
    // Arrange
    Utf8 utf8 = new Utf8();

    // Act
    utf8.setByteLength(10000);

    // Assert
    assertEquals("Should handle large valid size", 10000, utf8.getByteLength());
    assertTrue("Should allocate sufficient space", utf8.getBytes().length >= 10000);
  }

  @Test
  public void testCopyConstructor_WithEmojiCharacters_CopiesCorrectly() {
    // Arrange - Emojis are multi-byte UTF-8 characters
    Utf8 original = new Utf8("Hello 👋 World 🌍");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same content", original.toString(), copy.toString());
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
    assertNotSame("Copy should have different byte array", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testSetByteLength_DecreaseAndIncrease_WorksCorrectly() {
    // Arrange
    Utf8 utf8 = new Utf8("HelloWorld");

    // Act - decrease then increase
    utf8.setByteLength(5);
    utf8.setByteLength(15);

    // Assert
    assertEquals("Should handle decrease and increase", 15, utf8.getByteLength());
  }
}
