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

/**
 * Test class for Utf8 Covers lines 64-65 (copy constructor with
 * System.arraycopy) and lines 116-117-118 (setByteLength with array expansion)
 * of Utf8.java
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

  // ========== Tests for Copy Constructor (lines 64-65) ==========

  @Test
  public void testCopyConstructor_SimpleString_CreatesIndependentCopy() {
    // Arrange
    Utf8 original = new Utf8("Hello");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same content as original", original.toString(), copy.toString());
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
    assertNotSame("Copy should have different byte array reference", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_EmptyUtf8_CopiesCorrectly() {
    // Arrange
    Utf8 original = new Utf8("");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy of empty Utf8 should be empty", "", copy.toString());
    assertEquals("Copy should have zero byte length", 0, copy.getByteLength());
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
    copy.set("Modified");

    // Assert
    assertEquals("Original should remain unchanged", "Original", original.toString());
    assertEquals("Copy should be modified", "Modified", copy.toString());
  }

  @Test
  public void testCopyConstructor_UnicodeString_CopiesCorrectly() {
    // Arrange
    Utf8 original = new Utf8("Hello 世界 🌍");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should preserve Unicode characters", original.toString(), copy.toString());
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
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
    assertEquals("Copy should have same content for large strings", original.toString(), copy.toString());
    assertEquals("Copy should have correct byte length", original.getByteLength(), copy.getByteLength());
  }

  @Test
  public void testCopyConstructor_ByteArrayIndependence_VerifyDeepCopy() {
    // Arrange
    Utf8 original = new Utf8("Test");
    Utf8 copy = new Utf8(original);
    byte[] originalBytes = original.getBytes();
    byte[] copyBytes = copy.getBytes();

    // Act - Manually modify original's byte array
    originalBytes[0] = (byte) 'X';

    // Assert - verify arrays are independent
    assertNotSame("Copy should have different byte array reference", originalBytes, copyBytes);
    assertNotEquals("Byte arrays should be independent", originalBytes[0], copyBytes[0]);
    assertEquals("Copy's first byte should remain unchanged", (byte) 'T', copyBytes[0]);
  }

  @Test
  public void testCopyConstructor_SpecialCharacters_CopiesCorrectly() {
    // Arrange
    Utf8 original = new Utf8("Tab\tNewline\nCarriageReturn\r");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should preserve special characters", original.toString(), copy.toString());
  }

  // ========== Tests for setByteLength Method (lines 116-117-118) ==========

  @Test
  public void testSetByteLength_ExpandArray_CreatesNewArray() {
    // Arrange
    utf8Instance = new Utf8("Small");
    int originalLength = utf8Instance.getByteLength();
    int newLength = originalLength + 100;

    // Act
    Utf8 result = utf8Instance.setByteLength(newLength);

    // Assert
    assertEquals("setByteLength should return this for chaining", utf8Instance, result);
    assertEquals("Byte length should be updated", newLength, utf8Instance.getByteLength());
    assertTrue("Byte array should be expanded", utf8Instance.getBytes().length >= newLength);
  }

  @Test
  public void testSetByteLength_SameLength_UpdatesLength() {
    // Arrange
    utf8Instance = new Utf8("Test");
    int currentLength = utf8Instance.getByteLength();

    // Act
    utf8Instance.setByteLength(currentLength);

    // Assert
    assertEquals("Byte length should remain same", currentLength, utf8Instance.getByteLength());
  }

  @Test
  public void testSetByteLength_SmallerLength_KeepsArray() {
    // Arrange
    utf8Instance = new Utf8("LongString");
    byte[] originalArray = utf8Instance.getBytes();
    int newLength = 4;

    // Act
    utf8Instance.setByteLength(newLength);

    // Assert
    assertEquals("Byte length should be reduced", newLength, utf8Instance.getByteLength());
    assertSame("Byte array reference should remain same when reducing length", originalArray, utf8Instance.getBytes());
  }

  @Test
  public void testSetByteLength_ZeroLength_UpdatesCorrectly() {
    // Arrange
    utf8Instance = new Utf8("SomeText");

    // Act
    utf8Instance.setByteLength(0);

    // Assert
    assertEquals("Byte length should be zero", 0, utf8Instance.getByteLength());
    assertEquals("toString should return empty string", "", utf8Instance.toString());
  }

  @Test
  public void testSetByteLength_PreservesExistingBytes_WhenExpanding() {
    // Arrange
    String originalText = "Hello";
    utf8Instance = new Utf8(originalText);
    byte[] originalBytes = originalText.getBytes(StandardCharsets.UTF_8);
    int originalLength = utf8Instance.getByteLength();
    int newLength = originalLength + 50;

    // Act
    utf8Instance.setByteLength(newLength);
    byte[] newBytes = utf8Instance.getBytes();

    // Assert
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Existing bytes should be preserved at index " + i, originalBytes[i], newBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_ClearsStringCache_AfterExpansion() {
    // Arrange
    utf8Instance = new Utf8("Test");
    String cachedString = utf8Instance.toString(); // Cache the string
    int newLength = utf8Instance.getByteLength() + 10;

    // Act
    utf8Instance.setByteLength(newLength);

    // Assert - the string cache should be cleared
    // We can't directly test the cache, but we can verify the byte length changed
    assertEquals("Byte length should be updated", newLength, utf8Instance.getByteLength());
  }

  @Test
  public void testSetByteLength_MultipleExpansions_WorksCorrectly() {
    // Arrange
    utf8Instance = new Utf8("A");

    // Act & Assert - expand multiple times
    utf8Instance.setByteLength(10);
    assertEquals("First expansion should work", 10, utf8Instance.getByteLength());

    utf8Instance.setByteLength(50);
    assertEquals("Second expansion should work", 50, utf8Instance.getByteLength());

    utf8Instance.setByteLength(100);
    assertEquals("Third expansion should work", 100, utf8Instance.getByteLength());
  }

  @Test
  public void testSetByteLength_AfterCopyConstructor_IndependentArrays() {
    // Arrange
    Utf8 original = new Utf8("Test");
    Utf8 copy = new Utf8(original);
    int newLength = original.getByteLength() + 20;

    // Act
    copy.setByteLength(newLength);

    // Assert
    assertEquals("Copy should have expanded length", newLength, copy.getByteLength());
    assertNotEquals("Original should retain original length", newLength, original.getByteLength());
    assertNotSame("Arrays should be independent", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testSetByteLength_ChainedCalls_ReturnsThis() {
    // Arrange
    utf8Instance = new Utf8("Test");

    // Act
    Utf8 result = utf8Instance.setByteLength(10).setByteLength(20);

    // Assert
    assertSame("Chained calls should return same instance", utf8Instance, result);
    assertEquals("Final length should be applied", 20, utf8Instance.getByteLength());
  }

  @Test
  public void testSetByteLength_LargeExpansion_HandlesCorrectly() {
    // Arrange
    utf8Instance = new Utf8("Small");
    int largeLength = 10000;

    // Act
    utf8Instance.setByteLength(largeLength);

    // Assert
    assertEquals("Should handle large expansions", largeLength, utf8Instance.getByteLength());
    assertTrue("Array should accommodate large length", utf8Instance.getBytes().length >= largeLength);
  }

  // ========== Integration Tests ==========

  @Test
  public void testCopyConstructorAndSetByteLength_Combined_WorkCorrectly() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);

    // Act
    copy.setByteLength(copy.getByteLength() + 10);
    original.set("Modified");

    // Assert
    assertEquals("Original should be modified", "Modified", original.toString());
    assertNotEquals("Copy byte length should be different", original.getByteLength(), copy.getByteLength());
  }

  @Test
  public void testSetByteLength_WithUnicodeBytes_PreservesData() {
    // Arrange
    String unicodeText = "Hello 世界";
    utf8Instance = new Utf8(unicodeText);
    int originalLength = utf8Instance.getByteLength();

    // Act
    utf8Instance.setByteLength(originalLength + 20);
    utf8Instance.setByteLength(originalLength); // Restore original length

    // Assert
    assertEquals("Unicode text should be preserved", unicodeText, utf8Instance.toString());
  }

  @Test
  public void testCopyConstructor_EmptyByteArray_HandlesCorrectly() {
    // Arrange
    Utf8 original = new Utf8(new byte[0]);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have zero length", 0, copy.getByteLength());
    assertEquals("Copy should produce empty string", "", copy.toString());
  }

  @Test
  public void testSetByteLength_FromZeroToNonZero_ExpandsCorrectly() {
    // Arrange
    utf8Instance = new Utf8();

    // Act
    utf8Instance.setByteLength(10);

    // Assert
    assertEquals("Should expand from zero to specified length", 10, utf8Instance.getByteLength());
  }

  @Test
  public void testCopyConstructor_WithCachedString_CopiesCache() {
    // Arrange
    Utf8 original = new Utf8("Test");
    original.toString(); // Ensure string is cached

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should produce same string", original.toString(), copy.toString());
  }

  // ========== Edge Cases and Boundary Tests ==========

  @Test
  public void testSetByteLength_MinimumValue_HandlesZero() {
    // Arrange
    utf8Instance = new Utf8("Something");

    // Act
    utf8Instance.setByteLength(0);

    // Assert
    assertEquals("Should handle zero length", 0, utf8Instance.getByteLength());
  }

  @Test
  public void testCopyConstructor_SingleByteCharacter_CopiesCorrectly() {
    // Arrange
    Utf8 original = new Utf8("A");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Single character should be copied", "A", copy.toString());
    assertEquals("Byte length should be 1", 1, copy.getByteLength());
  }

  @Test
  public void testCopyConstructor_MultiByteCharacter_CopiesCorrectly() {
    // Arrange
    Utf8 original = new Utf8("€");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Multi-byte character should be copied", "€", copy.toString());
    assertEquals("Byte length should match", original.getByteLength(), copy.getByteLength());
  }

  @Test
  public void testSetByteLength_IncrementByOne_WorksCorrectly() {
    // Arrange
    utf8Instance = new Utf8("Hi");
    int originalLength = utf8Instance.getByteLength();

    // Act
    utf8Instance.setByteLength(originalLength + 1);

    // Assert
    assertEquals("Should increment by one", originalLength + 1, utf8Instance.getByteLength());
  }
}
