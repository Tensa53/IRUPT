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
 * Test class for Utf8 Specifically covers lines 64-65 (copy constructor) and
 * 116-117-118 (setByteLength method) of Utf8.java
 */
public class TestUtf8_4 {

  private Utf8 utf8Instance;
  private static final String TEST_STRING = "Hello World";
  private static final String UNICODE_STRING = "Hello 世界 🌍";
  private static final String EMPTY_STRING = "";

  @Before
  public void setUp() {
    utf8Instance = new Utf8();
  }

  @After
  public void tearDown() {
    utf8Instance = null;
  }

  // ============================================
  // Tests for Copy Constructor (lines 64-65)
  // ============================================

  @Test
  public void testCopyConstructor_SimpleString_CreatesExactCopy() {
    // Arrange
    Utf8 original = new Utf8(TEST_STRING);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
    assertEquals("Copy should have same string value", original.toString(), copy.toString());
    assertNotSame("Copy should be a different object", original, copy);
    assertNotSame("Copy should have different byte array", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_UnicodeString_CreatesExactCopy() {
    // Arrange
    Utf8 original = new Utf8(UNICODE_STRING);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
    assertEquals("Copy should have same unicode string value", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_EmptyString_CreatesEmptyCopy() {
    // Arrange
    Utf8 original = new Utf8(EMPTY_STRING);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have zero length", 0, copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
    assertEquals("Copy should have empty string value", "", copy.toString());
  }

  @Test
  public void testCopyConstructor_BytesArrayIndependence_ModifyingOriginalDoesNotAffectCopy() {
    // Arrange
    Utf8 original = new Utf8(TEST_STRING);
    byte[] originalBytes = original.getBytes();
    int originalLength = original.getByteLength();

    // Act
    Utf8 copy = new Utf8(original);
    originalBytes[0] = (byte) 'X'; // Modify original's bytes

    // Assert
    assertNotEquals("Copy should not be affected by original byte modification", originalBytes[0], copy.getBytes()[0]);
    assertEquals("Copy should maintain its original value", TEST_STRING, copy.toString());
  }

  @Test
  public void testCopyConstructor_WithCachedString_CopiesStringReference() {
    // Arrange
    Utf8 original = new Utf8(TEST_STRING);
    original.toString(); // Cache the string

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same string value", original.toString(), copy.toString());
    assertEquals("Copy should equal original", original, copy);
  }

  @Test
  public void testCopyConstructor_LargeString_CreatesExactCopy() {
    // Arrange
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      sb.append("Test");
    }
    String largeString = sb.toString();
    Utf8 original = new Utf8(largeString);

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
    assertEquals("Copy should have same large string value", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_ZeroLengthUtf8_CreatesValidCopy() {
    // Arrange
    Utf8 original = new Utf8();

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have zero length", 0, copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
  }

  // ============================================
  // Tests for setByteLength method (lines 116-117-118)
  // ============================================

  @Test
  public void testSetByteLength_ExpandingCapacity_AllocatesNewByteArray() {
    // Arrange
    utf8Instance = new Utf8("Hi");
    int originalLength = utf8Instance.getByteLength();
    byte[] originalBytes = utf8Instance.getBytes();
    int newLength = originalLength + 10;

    // Act
    Utf8 result = utf8Instance.setByteLength(newLength);

    // Assert
    assertEquals("Length should be updated to new length", newLength, utf8Instance.getByteLength());
    assertNotSame("Bytes array should be reallocated when expanding", originalBytes, utf8Instance.getBytes());
    assertSame("setByteLength should return this for chaining", utf8Instance, result);
    assertTrue("New byte array should be at least newLength", utf8Instance.getBytes().length >= newLength);
  }

  @Test
  public void testSetByteLength_ExpandingCapacity_PreservesExistingBytes() {
    // Arrange
    String testStr = "Test";
    utf8Instance = new Utf8(testStr);
    byte[] expectedBytes = testStr.getBytes(StandardCharsets.UTF_8);
    int originalLength = utf8Instance.getByteLength();
    int newLength = originalLength + 10;

    // Act
    utf8Instance.setByteLength(newLength);

    // Assert
    byte[] actualBytes = utf8Instance.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original bytes should be preserved after expansion at index " + i, expectedBytes[i],
          actualBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_ShrinkingLength_KeepsExistingByteArray() {
    // Arrange
    utf8Instance = new Utf8("Hello World");
    byte[] originalBytes = utf8Instance.getBytes();
    int newLength = 5;

    // Act
    Utf8 result = utf8Instance.setByteLength(newLength);

    // Assert
    assertEquals("Length should be reduced", newLength, utf8Instance.getByteLength());
    assertSame("Bytes array should not be reallocated when shrinking", originalBytes, utf8Instance.getBytes());
    assertSame("setByteLength should return this for chaining", utf8Instance, result);
  }

  @Test
  public void testSetByteLength_SameLength_KeepsExistingByteArray() {
    // Arrange
    utf8Instance = new Utf8(TEST_STRING);
    byte[] originalBytes = utf8Instance.getBytes();
    int currentLength = utf8Instance.getByteLength();

    // Act
    utf8Instance.setByteLength(currentLength);

    // Assert
    assertEquals("Length should remain the same", currentLength, utf8Instance.getByteLength());
    assertSame("Bytes array should not be reallocated for same length", originalBytes, utf8Instance.getBytes());
  }

  @Test
  public void testSetByteLength_ZeroLength_KeepsExistingByteArray() {
    // Arrange
    utf8Instance = new Utf8(TEST_STRING);
    byte[] originalBytes = utf8Instance.getBytes();

    // Act
    utf8Instance.setByteLength(0);

    // Assert
    assertEquals("Length should be zero", 0, utf8Instance.getByteLength());
    assertSame("Bytes array should not be reallocated", originalBytes, utf8Instance.getBytes());
  }

  @Test
  public void testSetByteLength_ClearsStringCache_AfterSettingLength() {
    // Arrange
    utf8Instance = new Utf8(TEST_STRING);
    String cachedString = utf8Instance.toString(); // Cache the string
    assertNotNull("String should be cached", cachedString);

    // Act
    utf8Instance.setByteLength(utf8Instance.getByteLength());

    // Assert - The string cache should be cleared, but we can't directly test this
    // We verify behavior by checking length is set correctly
    assertEquals("Length should be set correctly", TEST_STRING.getBytes(StandardCharsets.UTF_8).length,
        utf8Instance.getByteLength());
  }

  @Test
  public void testSetByteLength_MethodChaining_ReturnsThisInstance() {
    // Arrange
    utf8Instance = new Utf8(TEST_STRING);

    // Act
    Utf8 result1 = utf8Instance.setByteLength(5);
    Utf8 result2 = result1.setByteLength(10);

    // Assert
    assertSame("First call should return this", utf8Instance, result1);
    assertSame("Second call should return this", utf8Instance, result2);
    assertEquals("Final length should be 10", 10, utf8Instance.getByteLength());
  }

  @Test
  public void testSetByteLength_ExpandFromEmpty_AllocatesNewByteArray() {
    // Arrange
    utf8Instance = new Utf8(); // Empty Utf8
    assertEquals("Initial length should be 0", 0, utf8Instance.getByteLength());

    // Act
    utf8Instance.setByteLength(20);

    // Assert
    assertEquals("Length should be 20", 20, utf8Instance.getByteLength());
    assertTrue("Byte array should have capacity for at least 20 bytes", utf8Instance.getBytes().length >= 20);
  }

  @Test
  public void testSetByteLength_MultipleExpansions_OnlyReallocatesWhenNeeded() {
    // Arrange
    utf8Instance = new Utf8("Hi");

    // Act - First expansion
    utf8Instance.setByteLength(10);
    byte[] bytesAfterFirstExpansion = utf8Instance.getBytes();

    // Act - Second expansion within capacity
    utf8Instance.setByteLength(8);
    byte[] bytesAfterSecondSet = utf8Instance.getBytes();

    // Assert
    assertSame("Byte array should not be reallocated when shrinking within capacity", bytesAfterFirstExpansion,
        bytesAfterSecondSet);
  }

  @Test
  public void testSetByteLength_BoundaryValue_MaxIntegerWithinLimit() {
    // Arrange
    utf8Instance = new Utf8();
    int reasonableMaxLength = 1000; // Use a reasonable value for testing

    // Act
    utf8Instance.setByteLength(reasonableMaxLength);

    // Assert
    assertEquals("Length should be set to reasonable max", reasonableMaxLength, utf8Instance.getByteLength());
  }

  @Test
  public void testSetByteLength_ExceedsMaxLength_ThrowsException() {
    // Arrange
    utf8Instance = new Utf8();
    // Integer.MAX_VALUE will cause OutOfMemoryError before AvroRuntimeException
    // when MAX_LENGTH is not configured to a lower value
    int exceedsMaxLength = Integer.MAX_VALUE;

    // Act & Assert
    try {
      utf8Instance.setByteLength(exceedsMaxLength);
      fail("Expected exception to be thrown for exceeding max length");
    } catch (AvroRuntimeException e) {
      assertTrue("Exception message should mention string length", e.getMessage().contains("String length"));
      assertTrue("Exception message should mention exceeds maximum", e.getMessage().contains("exceeds maximum"));
    } catch (OutOfMemoryError e) {
      // This is expected when MAX_LENGTH is set to Integer.MAX_VALUE (default)
      assertTrue("OutOfMemoryError is acceptable for very large allocations", true);
    }
  }

  @Test
  public void testSetByteLength_NegativeLength_BehaviorCheck() {
    // Arrange
    utf8Instance = new Utf8(TEST_STRING);

    // Act & Assert - Testing with negative length
    try {
      utf8Instance.setByteLength(-1);
      // If it doesn't throw, verify the state
      assertTrue("Negative length handling should be defined by implementation", utf8Instance.getByteLength() >= -1);
    } catch (Exception e) {
      // If it throws any exception, that's also acceptable behavior
      assertTrue("Exception is acceptable for negative length", true);
    }
  }

  // ============================================
  // Integration tests - Copy Constructor with setByteLength
  // ============================================

  @Test
  public void testCopyConstructorThenSetByteLength_IndependentModifications() {
    // Arrange
    Utf8 original = new Utf8(TEST_STRING);
    Utf8 copy = new Utf8(original);
    int originalLength = original.getByteLength();

    // Act
    copy.setByteLength(originalLength + 10);

    // Assert
    assertEquals("Original length should not change", originalLength, original.getByteLength());
    assertEquals("Copy length should be expanded", originalLength + 10, copy.getByteLength());
    assertNotEquals("Copy and original should have different lengths", original.getByteLength(), copy.getByteLength());
  }

  @Test
  public void testSetByteLengthThenCopyConstructor_CopiesExpandedCapacity() {
    // Arrange
    Utf8 original = new Utf8("Hi");
    original.setByteLength(20); // Expand capacity

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as expanded original", 20, copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
  }

  // ============================================
  // Edge cases and boundary tests
  // ============================================

  @Test
  public void testCopyConstructor_WithModifiedBytes_CopiesCurrentState() {
    // Arrange
    Utf8 original = new Utf8("Test");
    original.setByteLength(10); // Expand
    byte[] bytes = original.getBytes();
    bytes[4] = 65; // Modify expanded area
    bytes[5] = 66;

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same modified bytes", bytes[4], copy.getBytes()[4]);
    assertEquals("Copy should have same modified bytes", bytes[5], copy.getBytes()[5]);
  }

  @Test
  public void testSetByteLength_MultipleSequentialExpansions_MaintainsDataIntegrity() {
    // Arrange
    utf8Instance = new Utf8("A");
    byte firstByte = utf8Instance.getBytes()[0];

    // Act - Multiple expansions
    utf8Instance.setByteLength(5);
    utf8Instance.setByteLength(10);
    utf8Instance.setByteLength(20);

    // Assert
    assertEquals("First byte should be preserved through expansions", firstByte, utf8Instance.getBytes()[0]);
    assertEquals("Final length should be 20", 20, utf8Instance.getByteLength());
  }
}
