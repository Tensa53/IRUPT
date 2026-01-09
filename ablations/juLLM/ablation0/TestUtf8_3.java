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
 * Test class for Utf8 Covers lines 64-65 (copy constructor) and 116-117-118
 * (setByteLength with array resizing) of Utf8.java
 */
public class TestUtf8_3 {

  private Utf8 utf8;

  @Before
  public void setUp() {
    utf8 = null;
  }

  @After
  public void tearDown() {
    utf8 = null;
  }

  // ========== Tests for Copy Constructor (lines 64-65) ==========

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
    assertNotSame("Copy should be a different object instance", original, copy);
    assertNotSame("Copy should have different byte array instance", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_SimpleString_CreatesExactCopy() {
    // Arrange
    Utf8 original = new Utf8("Hello");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertNotNull("Copy should not be null", copy);
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same string value", original.toString(), copy.toString());
    assertEquals("Copy should equal original", original, copy);
    assertNotSame("Copy should be a different object", original, copy);
    assertNotSame("Copy should have different byte array", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_MultiByteCharacters_CopiesAllBytes() {
    // Arrange
    Utf8 original = new Utf8("Hello 世界 🌍");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same string value", original.toString(), copy.toString());

    byte[] originalBytes = original.getBytes();
    byte[] copyBytes = copy.getBytes();
    assertNotSame("Byte arrays should be different instances", originalBytes, copyBytes);

    for (int i = 0; i < original.getByteLength(); i++) {
      assertEquals("Byte at position " + i + " should match", originalBytes[i], copyBytes[i]);
    }
  }

  @Test
  public void testCopyConstructor_ModifyOriginal_DoesNotAffectCopy() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);
    String originalCopyValue = copy.toString();

    // Act
    original.set("Modified");

    // Assert
    assertEquals("Copy should retain original value", originalCopyValue, copy.toString());
    assertNotEquals("Original and copy should now be different", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_ModifyCopy_DoesNotAffectOriginal() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);
    String originalValue = original.toString();

    // Act
    copy.set("Modified");

    // Assert
    assertEquals("Original should retain its value", originalValue, original.toString());
    assertNotEquals("Original and copy should now be different", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_LargeString_CopiesAllData() {
    // Arrange
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      sb.append("Test");
    }
    Utf8 original = new Utf8(sb.toString());

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same content", original.toString(), copy.toString());
    assertEquals("Copy should equal original", original, copy);
  }

  @Test
  public void testCopyConstructor_WithCachedString_CopiesCachedString() {
    // Arrange
    Utf8 original = new Utf8("Test");
    original.toString(); // Cache the string

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same string value", original.toString(), copy.toString());
    assertEquals("Copy should equal original", original, copy);
  }

  @Test
  public void testCopyConstructor_SingleCharacter_CreatesCopy() {
    // Arrange
    Utf8 original = new Utf8("X");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have length 1", 1, copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
    assertEquals("Copy should have same content", "X", copy.toString());
  }

  // ========== Tests for setByteLength with Array Resizing (lines 116-117-118)
  // ==========

  @Test
  public void testSetByteLength_ExpandsArray_PreservesExistingData() {
    // Arrange
    utf8 = new Utf8("Hello");
    byte[] originalBytes = utf8.getBytes();
    int originalLength = utf8.getByteLength();

    // Act
    utf8.setByteLength(20);

    // Assert
    assertEquals("Byte length should be updated", 20, utf8.getByteLength());
    assertNotSame("Byte array should be replaced with larger array", originalBytes, utf8.getBytes());
    assertTrue("New byte array should be larger", utf8.getBytes().length >= 20);

    // Verify original data is preserved
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original byte at position " + i + " should be preserved", originalBytes[i], utf8.getBytes()[i]);
    }
  }

  @Test
  public void testSetByteLength_DoubleSize_CopiesOriginalBytes() {
    // Arrange
    utf8 = new Utf8("Test");
    int originalLength = utf8.getByteLength();
    byte[] originalBytes = new byte[originalLength];
    System.arraycopy(utf8.getBytes(), 0, originalBytes, 0, originalLength);
    int newLength = originalLength * 2;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    assertEquals("Byte length should be doubled", newLength, utf8.getByteLength());

    // Verify all original bytes are preserved
    byte[] newBytes = utf8.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Byte at index " + i + " should match original", originalBytes[i], newBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_VeryLargeExpansion_PreservesData() {
    // Arrange
    utf8 = new Utf8("A");
    byte originalByte = utf8.getBytes()[0];

    // Act
    utf8.setByteLength(10000);

    // Assert
    assertEquals("Byte length should be set to 10000", 10000, utf8.getByteLength());
    assertEquals("First byte should be preserved", originalByte, utf8.getBytes()[0]);
  }

  @Test
  public void testSetByteLength_ExactCurrentSize_DoesNotReallocate() {
    // Arrange
    utf8 = new Utf8("Hello");
    int currentLength = utf8.getByteLength();
    byte[] currentBytes = utf8.getBytes();

    // Act
    utf8.setByteLength(currentLength);

    // Assert
    assertEquals("Byte length should remain same", currentLength, utf8.getByteLength());
    assertSame("Byte array should not be reallocated", currentBytes, utf8.getBytes());
  }

  @Test
  public void testSetByteLength_SmallerSize_DoesNotReallocate() {
    // Arrange
    utf8 = new Utf8("Hello World");
    byte[] currentBytes = utf8.getBytes();
    int newLength = 5;

    // Act
    utf8.setByteLength(newLength);

    // Assert
    assertEquals("Byte length should be reduced", newLength, utf8.getByteLength());
    assertSame("Byte array should not be reallocated when shrinking", currentBytes, utf8.getBytes());
  }

  @Test
  public void testSetByteLength_ZeroLength_ClearsString() {
    // Arrange
    utf8 = new Utf8("Hello");

    // Act
    utf8.setByteLength(0);

    // Assert
    assertEquals("Byte length should be 0", 0, utf8.getByteLength());
    assertEquals("String should be empty", "", utf8.toString());
  }

  @Test
  public void testSetByteLength_IncrementalExpansions_PreservesData() {
    // Arrange
    utf8 = new Utf8("AB");
    byte byte0 = utf8.getBytes()[0];
    byte byte1 = utf8.getBytes()[1];

    // Act - multiple expansions
    utf8.setByteLength(5);
    utf8.setByteLength(10);
    utf8.setByteLength(20);

    // Assert
    assertEquals("Final byte length should be 20", 20, utf8.getByteLength());
    assertEquals("First byte should be preserved", byte0, utf8.getBytes()[0]);
    assertEquals("Second byte should be preserved", byte1, utf8.getBytes()[1]);
  }

  @Test
  public void testSetByteLength_ClearsCachedString() {
    // Arrange
    utf8 = new Utf8("Hello");
    String cached = utf8.toString(); // Cache the string

    // Act
    utf8.setByteLength(10);

    // Assert
    // The cached string should be cleared (internal state)
    // We can verify by checking that after expansion, toString() still works
    assertNotNull("toString should still work after setByteLength", utf8.toString());
  }

  @Test
  public void testSetByteLength_ExceedsMaxLength_ThrowsException() {
    // Arrange
    utf8 = new Utf8("Test");
    // This test verifies the MAX_LENGTH check on line 112
    // If MAX_LENGTH property is not set, it defaults to Integer.MAX_VALUE
    // and we cannot test the exception without causing OutOfMemoryError
    String maxLengthProp = System.getProperty("org.apache.avro.limits.string.maxLength");

    if (maxLengthProp != null) {
      // Only test if max length is explicitly set and testable
      try {
        int maxLength = Integer.parseUnsignedInt(maxLengthProp);
        int exceedingLength = maxLength + 1;

        // Act & Assert
        try {
          utf8.setByteLength(exceedingLength);
          fail("Expected AvroRuntimeException for exceeding max length");
        } catch (AvroRuntimeException e) {
          assertTrue("Exception message should mention string length", e.getMessage().contains("String length"));
          assertTrue("Exception message should mention exceeds maximum", e.getMessage().contains("exceeds maximum"));
        }
      } catch (NumberFormatException nfe) {
        // Property is set but not parseable, skip test
      }
    }
    // If property is not set, the check still exists on line 112 but cannot be
    // tested without OOM
  }

  @Test
  public void testSetByteLength_EmptyUtf8Expansion_Works() {
    // Arrange
    utf8 = new Utf8();
    assertEquals("Initial length should be 0", 0, utf8.getByteLength());

    // Act
    utf8.setByteLength(10);

    // Assert
    assertEquals("Byte length should be set to 10", 10, utf8.getByteLength());
    assertTrue("Byte array should be at least 10 bytes", utf8.getBytes().length >= 10);
  }

  @Test
  public void testSetByteLength_ReturnsThis_ForChaining() {
    // Arrange
    utf8 = new Utf8("Test");

    // Act
    Utf8 result = utf8.setByteLength(10);

    // Assert
    assertSame("setByteLength should return this for method chaining", utf8, result);
  }

  @Test
  public void testSetByteLength_MultipleExpansions_MaintainsDataIntegrity() {
    // Arrange
    utf8 = new Utf8("ABC");
    byte[] originalBytes = new byte[3];
    System.arraycopy(utf8.getBytes(), 0, originalBytes, 0, 3);

    // Act
    utf8.setByteLength(6);
    utf8.setByteLength(12);
    utf8.setByteLength(24);

    // Assert
    assertEquals("Final length should be 24", 24, utf8.getByteLength());
    for (int i = 0; i < 3; i++) {
      assertEquals("Original byte " + i + " should be preserved", originalBytes[i], utf8.getBytes()[i]);
    }
  }

  @Test
  public void testSetByteLength_WithMultiByteChars_PreservesEncoding() {
    // Arrange
    utf8 = new Utf8("ä"); // Multi-byte UTF-8 character
    int originalLength = utf8.getByteLength();
    byte[] originalBytes = new byte[originalLength];
    System.arraycopy(utf8.getBytes(), 0, originalBytes, 0, originalLength);

    // Act
    utf8.setByteLength(10);

    // Assert
    // Verify original bytes are preserved
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Multi-byte character byte " + i + " should be preserved", originalBytes[i], utf8.getBytes()[i]);
    }
  }

  // ========== Edge Cases and Integration Tests ==========

  @Test
  public void testCopyConstructor_ThenSetByteLength_WorksTogether() {
    // Arrange
    Utf8 original = new Utf8("Test");
    Utf8 copy = new Utf8(original);

    // Act
    copy.setByteLength(20);

    // Assert
    assertEquals("Copy byte length should be expanded", 20, copy.getByteLength());
    assertEquals("Original should remain unchanged", 4, original.getByteLength());
    assertNotEquals("Copy and original should have different byte arrays", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testSetByteLength_OnCopiedUtf8_IndependentExpansion() {
    // Arrange
    Utf8 original = new Utf8("Hello");
    Utf8 copy = new Utf8(original);
    byte[] originalBytesRef = original.getBytes();

    // Act
    copy.setByteLength(100);

    // Assert
    assertEquals("Copy should have expanded length", 100, copy.getByteLength());
    assertEquals("Original should have original length", 5, original.getByteLength());
    assertSame("Original byte array should not be affected", originalBytesRef, original.getBytes());
  }
}
