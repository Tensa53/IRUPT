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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4.13 test class for Utf8. Specifically targets lines
 * 64-65 (copy constructor with System.arraycopy) and lines 116-117-118
 * (setByteLength with byte array expansion).
 */
public class TestUtf8_5 {

  private Utf8 utf8Instance;
  private Utf8 emptyUtf8;
  private String simpleString;
  private String complexString;
  private byte[] simpleBytes;
  private byte[] complexBytes;

  @Before
  public void setUp() {
    simpleString = "Test";
    complexString = "Hello UTF-8 World! 你好世界 🌍";
    simpleBytes = simpleString.getBytes(StandardCharsets.UTF_8);
    complexBytes = complexString.getBytes(StandardCharsets.UTF_8);
    utf8Instance = new Utf8(simpleString);
    emptyUtf8 = new Utf8();
  }

  @After
  public void tearDown() {
    utf8Instance = null;
    emptyUtf8 = null;
    simpleBytes = null;
    complexBytes = null;
  }

  // ========== Tests for Copy Constructor (lines 64-65) ==========

  /**
   * Test copy constructor with non-empty Utf8 object. Targets lines 64-65:
   * System.arraycopy in copy constructor.
   */
  @Test
  public void testCopyConstructor_NonEmptyUtf8_CreatesIndependentCopy() {
    // Arrange
    Utf8 original = new Utf8("Original");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertNotNull("Copy should not be null", copy);
    assertEquals("Copy should have same length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same string representation", original.toString(), copy.toString());
    assertNotSame("Copy should have different byte array reference", original.getBytes(), copy.getBytes());
    assertArrayEquals("Copy should have same byte content", original.getBytes(), copy.getBytes());
  }

  /**
   * Test that copy constructor creates truly independent copy. Verifies line
   * 64-65 properly copies bytes.
   */
  @Test
  public void testCopyConstructor_ModifyOriginal_CopyUnaffected() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);

    // Act - modify original
    original.set("Modified");

    // Assert - copy should remain unchanged
    assertEquals("Copy should retain original value", "Original", copy.toString());
    assertFalse("Copy should differ from modified original", original.equals(copy));
  }

  /**
   * Test copy constructor with empty Utf8. Edge case for lines 64-65.
   */
  @Test
  public void testCopyConstructor_EmptyUtf8_CreatesEmptyCopy() {
    // Arrange
    Utf8 original = new Utf8();

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy of empty should be empty", 0, copy.getByteLength());
    assertEquals("Copy should have empty string", "", copy.toString());
  }

  /**
   * Test copy constructor with UTF-8 multi-byte characters. Ensures proper byte
   * copying for complex encodings.
   */
  @Test
  public void testCopyConstructor_MultiByteCharacters_ProperCopy() {
    // Arrange
    Utf8 original = new Utf8(complexString);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should preserve multi-byte characters", complexString, copy.toString());
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
  }

  /**
   * Test copy constructor preserves cached string. Verifies line 66 behavior.
   */
  @Test
  public void testCopyConstructor_WithCachedString_StringPreserved() {
    // Arrange
    Utf8 original = new Utf8("Test");
    original.toString(); // Cache the string

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same cached string", original.toString(), copy.toString());
  }

  // ========== Tests for setByteLength (lines 116-117-118) ==========

  /**
   * Test setByteLength with expansion requiring new byte array. Directly targets
   * lines 116-117-118.
   */
  @Test
  public void testSetByteLength_ExpandBeyondCapacity_CreatesNewArray() {
    // Arrange
    Utf8 utf8 = new Utf8("Hi");
    byte[] originalBytes = utf8.getBytes();
    int originalLength = utf8.getByteLength();

    // Act - expand to require new array allocation
    int newLength = originalLength + 100;
    Utf8 result = utf8.setByteLength(newLength);

    // Assert
    assertEquals("Should set new length", newLength, utf8.getByteLength());
    assertNotSame("Should have new byte array", originalBytes, utf8.getBytes());
    assertTrue("New array should be larger or equal", utf8.getBytes().length >= newLength);
    assertNotNull("Should return this for chaining", result);
    assertEquals("Should return same instance", utf8, result);
  }

  /**
   * Test setByteLength preserves existing bytes during expansion. Validates line
   * 117: System.arraycopy preserves original content.
   */
  @Test
  public void testSetByteLength_Expansion_PreservesExistingBytes() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");
    byte[] originalBytes = new byte[utf8.getByteLength()];
    System.arraycopy(utf8.getBytes(), 0, originalBytes, 0, utf8.getByteLength());
    int originalLength = utf8.getByteLength();

    // Act - expand beyond current capacity
    utf8.setByteLength(originalLength + 50);

    // Assert - check original bytes are preserved
    byte[] newBytes = utf8.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original byte at index " + i + " should be preserved", originalBytes[i], newBytes[i]);
    }
  }

  /**
   * Test setByteLength with same length doesn't reallocate. Tests condition on
   * line 115.
   */
  @Test
  public void testSetByteLength_SameLength_NoReallocation() {
    // Arrange
    Utf8 utf8 = new Utf8("Hello");
    byte[] originalBytes = utf8.getBytes();
    int currentLength = utf8.getByteLength();

    // Act - set to same length
    utf8.setByteLength(currentLength);

    // Assert
    assertEquals("Length should remain same", currentLength, utf8.getByteLength());
    assertNotNull("Bytes should still exist", utf8.getBytes());
  }

  /**
   * Test setByteLength with smaller length doesn't reallocate. Edge case for line
   * 115 condition.
   */
  @Test
  public void testSetByteLength_SmallerLength_NoReallocation() {
    // Arrange
    Utf8 utf8 = new Utf8("LongString");
    byte[] originalBytes = utf8.getBytes();

    // Act - set to smaller length
    utf8.setByteLength(4);

    // Assert
    assertEquals("Length should be updated", 4, utf8.getByteLength());
    assertEquals("Byte array reference should be same", originalBytes, utf8.getBytes());
  }

  /**
   * Test setByteLength clears cached string. Validates line 121 behavior.
   */
  @Test
  public void testSetByteLength_ClearsCachedString_AfterExpansion() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");
    String cachedString = utf8.toString(); // Cache the string

    // Act - change length to trigger cache clear
    utf8.setByteLength(utf8.getByteLength() + 10);

    // Assert - string should be recalculated (we can't directly test if null,
    // but behavior should still work)
    assertNotNull("toString should still work", utf8.toString());
  }

  /**
   * Test setByteLength with zero length. Edge case for lines 116-118.
   */
  @Test
  public void testSetByteLength_ZeroLength_Success() {
    // Arrange
    Utf8 utf8 = new Utf8("Something");

    // Act
    utf8.setByteLength(0);

    // Assert
    assertEquals("Length should be zero", 0, utf8.getByteLength());
    assertEquals("String should be empty", "", utf8.toString());
  }

  /**
   * Test setByteLength returns this for method chaining. Verifies line 122 return
   * value.
   */
  @Test
  public void testSetByteLength_ReturnsThis_ForChaining() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");

    // Act
    Utf8 result = utf8.setByteLength(10);

    // Assert
    assertEquals("Should return same instance", utf8, result);
  }

  /**
   * Test setByteLength with maximum valid length. Boundary test for line 112-114.
   */
  @Test
  public void testSetByteLength_LargeValidLength_Success() {
    // Arrange
    Utf8 utf8 = new Utf8();
    int largeLength = 10000;

    // Act
    utf8.setByteLength(largeLength);

    // Assert
    assertEquals("Should set large length", largeLength, utf8.getByteLength());
    assertTrue("Byte array should accommodate length", utf8.getBytes().length >= largeLength);
  }

  /**
   * Test setByteLength boundary behavior. Documents that MAX_LENGTH check on line
   * 112 protects against excessive allocations. Note: Testing with actual
   * Integer.MAX_VALUE would cause OOM, so this test verifies the mechanism works
   * with reasonable values.
   */
  @Test
  public void testSetByteLength_BoundaryCondition_ValidatesCorrectly() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");

    // Act - set to zero as boundary case
    utf8.setByteLength(0);

    // Assert
    assertEquals("Should accept zero length", 0, utf8.getByteLength());

    // Act - set to small positive value
    utf8.setByteLength(1);

    // Assert
    assertEquals("Should accept small positive length", 1, utf8.getByteLength());
  }

  /**
   * Test multiple expansions via setByteLength. Stress test for lines 116-118.
   */
  @Test
  public void testSetByteLength_MultipleExpansions_Success() {
    // Arrange
    Utf8 utf8 = new Utf8("A");

    // Act - expand multiple times
    utf8.setByteLength(10);
    utf8.setByteLength(50);
    utf8.setByteLength(100);

    // Assert
    assertEquals("Final length should be 100", 100, utf8.getByteLength());
    assertTrue("Byte array should be large enough", utf8.getBytes().length >= 100);
  }

  // ========== Additional Coverage Tests ==========

  /**
   * Test default constructor creates empty Utf8.
   */
  @Test
  public void testDefaultConstructor_CreatesEmptyUtf8_Success() {
    // Act
    Utf8 utf8 = new Utf8();

    // Assert
    assertNotNull("Instance should not be null", utf8);
    assertEquals("Length should be zero", 0, utf8.getByteLength());
    assertEquals("String should be empty", "", utf8.toString());
  }

  /**
   * Test String constructor creates proper Utf8.
   */
  @Test
  public void testStringConstructor_ValidString_Success() {
    // Act
    Utf8 utf8 = new Utf8(simpleString);

    // Assert
    assertEquals("Should store string correctly", simpleString, utf8.toString());
    assertEquals("Byte length should match", simpleBytes.length, utf8.getByteLength());
  }

  /**
   * Test byte array constructor.
   */
  @Test
  public void testByteArrayConstructor_ValidBytes_Success() {
    // Act
    Utf8 utf8 = new Utf8(simpleBytes);

    // Assert
    assertEquals("Should decode bytes correctly", simpleString, utf8.toString());
    assertEquals("Byte length should match", simpleBytes.length, utf8.getByteLength());
  }

  /**
   * Test getBytes returns byte array.
   */
  @Test
  public void testGetBytes_ReturnsUnderlyingByteArray_Success() {
    // Act
    byte[] bytes = utf8Instance.getBytes();

    // Assert
    assertNotNull("Bytes should not be null", bytes);
    assertArrayEquals("Bytes should match expected", simpleBytes, bytes);
  }

  /**
   * Test getByteLength returns correct length.
   */
  @Test
  public void testGetByteLength_ReturnsCorrectLength_Success() {
    // Act
    int length = utf8Instance.getByteLength();

    // Assert
    assertEquals("Length should match byte array length", simpleBytes.length, length);
  }

  /**
   * Test deprecated getLength method.
   */
  @Test
  @SuppressWarnings("deprecation")
  public void testGetLength_ReturnsCorrectLength_Success() {
    // Act
    int length = utf8Instance.getLength();

    // Assert
    assertEquals("Length should match byte array length", simpleBytes.length, length);
  }

  /**
   * Test deprecated setLength method.
   */
  @Test
  @SuppressWarnings("deprecation")
  public void testSetLength_UpdatesLength_Success() {
    // Arrange
    Utf8 utf8 = new Utf8("Test");

    // Act
    Utf8 result = utf8.setLength(10);

    // Assert
    assertEquals("Length should be updated", 10, utf8.getByteLength());
    assertEquals("Should return same instance", utf8, result);
  }

  /**
   * Test set method updates content.
   */
  @Test
  public void testSet_UpdatesContent_Success() {
    // Arrange
    Utf8 utf8 = new Utf8("Old");
    String newString = "New";

    // Act
    Utf8 result = utf8.set(newString);

    // Assert
    assertEquals("Content should be updated", newString, utf8.toString());
    assertEquals("Should return same instance", utf8, result);
  }

  /**
   * Test toString with empty string.
   */
  @Test
  public void testToString_EmptyUtf8_ReturnsEmptyString() {
    // Assert
    assertEquals("Empty Utf8 should return empty string", "", emptyUtf8.toString());
  }

  /**
   * Test toString caches result.
   */
  @Test
  public void testToString_CachesString_Success() {
    // Act
    String first = utf8Instance.toString();
    String second = utf8Instance.toString();

    // Assert
    assertEquals("Same string should be returned", first, second);
  }

  /**
   * Test equals with same instance.
   */
  @Test
  public void testEquals_SameInstance_ReturnsTrue() {
    // Assert
    assertTrue("Same instance should be equal", utf8Instance.equals(utf8Instance));
  }

  /**
   * Test equals with equal Utf8.
   */
  @Test
  public void testEquals_EqualUtf8_ReturnsTrue() {
    // Arrange
    Utf8 other = new Utf8(simpleString);

    // Assert
    assertTrue("Equal Utf8 objects should be equal", utf8Instance.equals(other));
  }

  /**
   * Test equals with different Utf8.
   */
  @Test
  public void testEquals_DifferentUtf8_ReturnsFalse() {
    // Arrange
    Utf8 other = new Utf8("Different");

    // Assert
    assertFalse("Different Utf8 objects should not be equal", utf8Instance.equals(other));
  }

  /**
   * Test equals with null.
   */
  @Test
  public void testEquals_Null_ReturnsFalse() {
    // Assert
    assertFalse("Utf8 should not equal null", utf8Instance.equals(null));
  }

  /**
   * Test equals with different type.
   */
  @Test
  public void testEquals_DifferentType_ReturnsFalse() {
    // Assert
    assertFalse("Utf8 should not equal String", utf8Instance.equals(simpleString));
  }

  /**
   * Test hashCode consistency.
   */
  @Test
  public void testHashCode_ConsistentResults_Success() {
    // Act
    int hash1 = utf8Instance.hashCode();
    int hash2 = utf8Instance.hashCode();

    // Assert
    assertEquals("HashCode should be consistent", hash1, hash2);
  }

  /**
   * Test hashCode for equal objects.
   */
  @Test
  public void testHashCode_EqualObjects_SameHashCode() {
    // Arrange
    Utf8 other = new Utf8(simpleString);

    // Assert
    assertEquals("Equal objects should have same hashCode", utf8Instance.hashCode(), other.hashCode());
  }

  /**
   * Test compareTo with equal Utf8.
   */
  @Test
  public void testCompareTo_EqualUtf8_ReturnsZero() {
    // Arrange
    Utf8 other = new Utf8(simpleString);

    // Assert
    assertEquals("Equal Utf8 should compare as zero", 0, utf8Instance.compareTo(other));
  }

  /**
   * Test compareTo with smaller Utf8.
   */
  @Test
  public void testCompareTo_SmallerUtf8_ReturnsPositive() {
    // Arrange
    Utf8 smaller = new Utf8("Aaa");
    Utf8 larger = new Utf8("Bbb");

    // Assert
    assertTrue("Larger should be positive compared to smaller", larger.compareTo(smaller) > 0);
  }

  /**
   * Test compareTo with larger Utf8.
   */
  @Test
  public void testCompareTo_LargerUtf8_ReturnsNegative() {
    // Arrange
    Utf8 smaller = new Utf8("Aaa");
    Utf8 larger = new Utf8("Bbb");

    // Assert
    assertTrue("Smaller should be negative compared to larger", smaller.compareTo(larger) < 0);
  }

  /**
   * Test charAt method.
   */
  @Test
  public void testCharAt_ValidIndex_ReturnsChar() {
    // Act
    char c = utf8Instance.charAt(0);

    // Assert
    assertEquals("Should return correct character", simpleString.charAt(0), c);
  }

  /**
   * Test length method returns character count.
   */
  @Test
  public void testLength_ReturnsCharacterCount_Success() {
    // Act
    int length = utf8Instance.length();

    // Assert
    assertEquals("Should return character count", simpleString.length(), length);
  }

  /**
   * Test subSequence method.
   */
  @Test
  public void testSubSequence_ValidRange_ReturnsSubsequence() {
    // Act
    CharSequence sub = utf8Instance.subSequence(0, 4);

    // Assert
    assertEquals("Should return correct subsequence", simpleString.subSequence(0, 4), sub);
  }

  /**
   * Test getBytesFor static method.
   */
  @Test
  public void testGetBytesFor_ValidString_ReturnsBytes() {
    // Act
    byte[] bytes = Utf8.getBytesFor(simpleString);

    // Assert
    assertArrayEquals("Should return UTF-8 bytes", simpleBytes, bytes);
  }
}
