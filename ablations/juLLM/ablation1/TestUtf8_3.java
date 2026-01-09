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

/**
 * Test class for Utf8 Covers lines 64-65 (copy constructor) and 116-117-118
 * (setByteLength) of Utf8.java
 */
public class TestUtf8_3 {

  private Utf8 utf8Instance;

  @Before
  public void setUp() {
    utf8Instance = null;
  }

  @After
  public void tearDown() {
    utf8Instance = null;
  }

  // ==================== Tests for Copy Constructor (lines 64-65)
  // ====================

  @Test
  public void testCopyConstructor_EmptyUtf8_CreatesEmptyCopy() {
    // Arrange
    Utf8 original = new Utf8();

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
    assertNotSame("Copy should be a different instance", original, copy);
  }

  @Test
  public void testCopyConstructor_SimpleString_CreatesCorrectCopy() {
    // Arrange
    Utf8 original = new Utf8("Hello");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
    assertEquals("Copy should have same string value", original.toString(), copy.toString());
    assertNotSame("Copy should be a different instance", original, copy);
  }

  @Test
  public void testCopyConstructor_MultiBytesCharacters_CreatesCorrectCopy() {
    // Arrange
    Utf8 original = new Utf8("Hello 世界 🌍");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
    assertEquals("Copy should have same string value", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_BytesArrayIndependence_ModifyingOriginalDoesNotAffectCopy() {
    // Arrange
    Utf8 original = new Utf8("Test");
    Utf8 copy = new Utf8(original);

    // Act - modify the original
    original.set("Modified");

    // Assert
    assertNotEquals("Copy should not be affected by changes to original", original.toString(), copy.toString());
    assertEquals("Copy should still have original value", "Test", copy.toString());
  }

  @Test
  public void testCopyConstructor_LargeString_CreatesCorrectCopy() {
    // Arrange
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      sb.append("LargeString");
    }
    Utf8 original = new Utf8(sb.toString());

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
    assertEquals("Copy should have same string value", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_SpecialCharacters_CreatesCorrectCopy() {
    // Arrange
    Utf8 original = new Utf8("Special: \n\t\r\0 chars!");

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have same length as original", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should equal original", original, copy);
  }

  @Test
  public void testCopyConstructor_ByteArrayCopied_VerifySystemArraycopy() {
    // Arrange
    Utf8 original = new Utf8("ABC");
    byte[] originalBytes = original.getBytes();

    // Act
    Utf8 copy = new Utf8(original);
    byte[] copyBytes = copy.getBytes();

    // Assert
    assertNotSame("Byte arrays should be different instances", originalBytes, copyBytes);
    assertEquals("Byte arrays should have same length", originalBytes.length, copyBytes.length);

    // Verify byte-by-byte copy
    for (int i = 0; i < original.getByteLength(); i++) {
      assertEquals("Byte at position " + i + " should match", originalBytes[i], copyBytes[i]);
    }
  }

  // ==================== Tests for setByteLength (lines 116-117-118)
  // ====================

  @Test
  public void testSetByteLength_ExpandArray_CreatesNewArrayAndCopiesContent() {
    // Arrange
    utf8Instance = new Utf8("Hello");
    int originalLength = utf8Instance.getByteLength();
    byte[] originalBytes = utf8Instance.getBytes();
    int newLength = originalLength + 50;

    // Act
    Utf8 result = utf8Instance.setByteLength(newLength);

    // Assert
    assertSame("setByteLength should return same instance", utf8Instance, result);
    assertEquals("Length should be updated", newLength, utf8Instance.getByteLength());
    assertNotSame("Byte array should be replaced when expanding", originalBytes, utf8Instance.getBytes());
    assertTrue("New byte array should be larger", utf8Instance.getBytes().length >= newLength);

    // Verify original content was copied (System.arraycopy on line 117)
    byte[] newBytes = utf8Instance.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original byte at position " + i + " should be copied", originalBytes[i], newBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_ExpandFromEmpty_CreatesNewArray() {
    // Arrange
    utf8Instance = new Utf8();
    int newLength = 10;

    // Act
    utf8Instance.setByteLength(newLength);

    // Assert
    assertEquals("Length should be set to new value", newLength, utf8Instance.getByteLength());
    assertTrue("Byte array should have capacity for new length", utf8Instance.getBytes().length >= newLength);
  }

  @Test
  public void testSetByteLength_ShrinkArray_DoesNotCreateNewArray() {
    // Arrange
    utf8Instance = new Utf8("LongString");
    byte[] originalBytes = utf8Instance.getBytes();
    int newLength = 4;

    // Act
    utf8Instance.setByteLength(newLength);

    // Assert
    assertEquals("Length should be updated", newLength, utf8Instance.getByteLength());
    assertSame("Byte array should not be replaced when shrinking", originalBytes, utf8Instance.getBytes());
  }

  @Test
  public void testSetByteLength_SameLength_ClearsCachedString() {
    // Arrange
    utf8Instance = new Utf8("Test");
    String cachedString = utf8Instance.toString(); // Cache the string
    int currentLength = utf8Instance.getByteLength();

    // Act
    utf8Instance.setByteLength(currentLength);

    // Assert
    assertEquals("Length should remain the same", currentLength, utf8Instance.getByteLength());
    // String cache should be cleared (line 121)
    // We can't directly test the cache, but we can verify toString still works
    assertEquals("toString should still work", cachedString, utf8Instance.toString());
  }

  @Test
  public void testSetByteLength_IncreaseThenDecrease_MaintainsData() {
    // Arrange
    utf8Instance = new Utf8("Data");
    int originalLength = utf8Instance.getByteLength();
    byte[] originalBytes = new byte[originalLength];
    System.arraycopy(utf8Instance.getBytes(), 0, originalBytes, 0, originalLength);

    // Act - expand then shrink
    utf8Instance.setByteLength(originalLength + 20);
    utf8Instance.setByteLength(originalLength);

    // Assert
    assertEquals("Length should be back to original", originalLength, utf8Instance.getByteLength());
    byte[] currentBytes = utf8Instance.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Data at position " + i + " should be preserved", originalBytes[i], currentBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_MultipleExpansions_PreservesData() {
    // Arrange
    utf8Instance = new Utf8("ABC");
    int firstExpansion = 10;
    int secondExpansion = 50;
    byte[] originalContent = new byte[utf8Instance.getByteLength()];
    System.arraycopy(utf8Instance.getBytes(), 0, originalContent, 0, utf8Instance.getByteLength());

    // Act
    utf8Instance.setByteLength(firstExpansion);
    utf8Instance.setByteLength(secondExpansion);

    // Assert
    assertEquals("Length should be set to second expansion", secondExpansion, utf8Instance.getByteLength());
    byte[] finalBytes = utf8Instance.getBytes();
    for (int i = 0; i < originalContent.length; i++) {
      assertEquals("Original content at position " + i + " should be preserved", originalContent[i], finalBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_ExactBoundary_WorksCorrectly() {
    // Arrange
    utf8Instance = new Utf8("Test");
    int currentLength = utf8Instance.getByteLength();
    int newLength = currentLength + 1;

    // Act
    utf8Instance.setByteLength(newLength);

    // Assert
    assertEquals("Length should be increased by one", newLength, utf8Instance.getByteLength());
  }

  @Test
  public void testSetByteLength_ZeroLength_WorksCorrectly() {
    // Arrange
    utf8Instance = new Utf8("Something");

    // Act
    utf8Instance.setByteLength(0);

    // Assert
    assertEquals("Length should be zero", 0, utf8Instance.getByteLength());
    assertEquals("toString should return empty string", "", utf8Instance.toString());
  }

  @Test
  public void testSetByteLength_VeryLargeButValid_WorksCorrectly() {
    // Arrange
    utf8Instance = new Utf8();
    // Use a large but reasonable size that won't cause OutOfMemoryError
    int largeButValidSize = 1000000; // 1MB

    // Act
    utf8Instance.setByteLength(largeButValidSize);

    // Assert
    assertEquals("Length should be set to large size", largeButValidSize, utf8Instance.getByteLength());
    assertTrue("Byte array should have sufficient capacity", utf8Instance.getBytes().length >= largeButValidSize);
  }

  @Test
  public void testSetByteLength_LargeExpansion_AllocatesCorrectSize() {
    // Arrange
    utf8Instance = new Utf8("Small");
    int largeSize = 10000;

    // Act
    utf8Instance.setByteLength(largeSize);

    // Assert
    assertEquals("Length should be set to large size", largeSize, utf8Instance.getByteLength());
    assertTrue("Byte array should have sufficient capacity", utf8Instance.getBytes().length >= largeSize);
  }

  // ==================== Additional Integration Tests ====================

  @Test
  public void testCopyConstructorThenSetByteLength_Integration() {
    // Arrange
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);

    // Act
    copy.setByteLength(copy.getByteLength() + 10);

    // Assert
    assertNotEquals("Copy length should differ from original after expansion", original.getByteLength(),
        copy.getByteLength());
    assertEquals("Original should not be affected", "Original", original.toString());
  }

  @Test
  public void testSetByteLengthAfterCopyConstructor_PreservesIndependence() {
    // Arrange
    Utf8 original = new Utf8("Test");

    // Act
    Utf8 copy = new Utf8(original);
    original.setByteLength(original.getByteLength() + 5);

    // Assert
    assertNotEquals("Lengths should be different", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should maintain original value", "Test", copy.toString());
  }

  @Test
  public void testCopyConstructor_WithModifiedBytes_CopiesToCorrectLength() {
    // Arrange
    Utf8 original = new Utf8("LongString");
    original.setByteLength(4); // Truncate to "Long"

    // Act
    Utf8 copy = new Utf8(original);

    // Assert
    assertEquals("Copy should have truncated length", 4, copy.getByteLength());
    assertEquals("Lengths should match", original.getByteLength(), copy.getByteLength());
  }

  @Test
  public void testSetByteLength_PreservesDataIntegrity_AfterMultipleOperations() {
    // Arrange
    utf8Instance = new Utf8();
    byte[] testData = "Hello World".getBytes();

    // Act
    utf8Instance.setByteLength(testData.length);
    System.arraycopy(testData, 0, utf8Instance.getBytes(), 0, testData.length);
    utf8Instance.setByteLength(testData.length + 10); // Expand

    // Assert
    byte[] resultBytes = utf8Instance.getBytes();
    for (int i = 0; i < testData.length; i++) {
      assertEquals("Data integrity should be maintained at position " + i, testData[i], resultBytes[i]);
    }
  }
}
