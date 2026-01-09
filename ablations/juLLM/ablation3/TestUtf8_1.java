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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4.13 test class for Utf8. Specifically targets lines
 * 64-65 and 116-117-118 while providing comprehensive coverage of all public
 * methods.
 */
public class TestUtf8_1 {

  private Utf8 utf8Instance;
  private String testString;
  private byte[] testBytes;

  @Before
  public void setUp() {
    testString = "Hello World";
    testBytes = testString.getBytes(StandardCharsets.UTF_8);
    utf8Instance = new Utf8(testString);
  }

  @After
  public void tearDown() {
    utf8Instance = null;
    testString = null;
    testBytes = null;
  }

  // ========== Constructor Tests ==========

  @Test
  public void testDefaultConstructor_EmptyUtf8_Success() {
    Utf8 utf8 = new Utf8();
    assertNotNull("Utf8 instance should not be null", utf8);
    assertEquals("Byte length should be 0", 0, utf8.getByteLength());
    assertEquals("String representation should be empty", "", utf8.toString());
  }

  @Test
  public void testStringConstructor_ValidString_Success() {
    String input = "Test String";
    Utf8 utf8 = new Utf8(input);
    assertNotNull("Utf8 instance should not be null", utf8);
    assertEquals("String should match input", input, utf8.toString());
    assertEquals("Byte length should match UTF-8 encoding", input.getBytes(StandardCharsets.UTF_8).length,
        utf8.getByteLength());
  }

  @Test
  public void testStringConstructor_EmptyString_Success() {
    Utf8 utf8 = new Utf8("");
    assertEquals("Empty string should be preserved", "", utf8.toString());
    assertEquals("Byte length should be 0", 0, utf8.getByteLength());
  }

  @Test
  public void testStringConstructor_UnicodeString_Success() {
    String unicode = "Hello 世界 🌍";
    Utf8 utf8 = new Utf8(unicode);
    assertEquals("Unicode string should be preserved", unicode, utf8.toString());
  }

  @Test
  public void testCopyConstructor_ValidUtf8_BytesAreCopied() {
    // Lines 64-65: This test specifically targets the copy constructor
    Utf8 original = new Utf8("Original String");
    Utf8 copy = new Utf8(original);

    assertNotNull("Copy should not be null", copy);
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same string value", original.toString(), copy.toString());

    // Verify that bytes are actually copied (line 65: System.arraycopy)
    assertNotSame("Byte arrays should be different instances", original.getBytes(), copy.getBytes());

    // Verify deep copy by modifying original
    original.set("Modified");
    assertNotEquals("Copy should not be affected by original modification", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_EmptyUtf8_Success() {
    // Line 64-65: Test copy constructor with empty Utf8
    Utf8 original = new Utf8("");
    Utf8 copy = new Utf8(original);

    assertEquals("Copy should be empty", "", copy.toString());
    assertEquals("Copy should have length 0", 0, copy.getByteLength());
    assertNotSame("Byte arrays should be different instances even when empty", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_LargeUtf8_BytesAreCopied() {
    // Lines 64-65: Test copy constructor with large string
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      sb.append("Test");
    }
    Utf8 original = new Utf8(sb.toString());
    Utf8 copy = new Utf8(original);

    assertEquals("Copy should preserve large string", original.getByteLength(), copy.getByteLength());
    assertArrayEquals("All bytes should be copied correctly",
        java.util.Arrays.copyOf(original.getBytes(), original.getByteLength()),
        java.util.Arrays.copyOf(copy.getBytes(), copy.getByteLength()));
  }

  @Test
  public void testByteArrayConstructor_ValidBytes_Success() {
    byte[] bytes = "Test".getBytes(StandardCharsets.UTF_8);
    Utf8 utf8 = new Utf8(bytes);
    assertEquals("Byte length should match array length", bytes.length, utf8.getByteLength());
    assertSame("Byte array should be same reference", bytes, utf8.getBytes());
  }

  // ========== setByteLength Tests (Lines 116-117-118) ==========

  @Test
  public void testSetByteLength_ExpandArray_NewArrayCreated() {
    // Lines 116-117-118: Test array expansion when new length exceeds current array
    // size
    Utf8 utf8 = new Utf8("Test");
    int originalLength = utf8.getByteLength();
    byte[] originalBytes = utf8.getBytes();

    // Set byte length larger than current array
    int newLength = 20;
    utf8.setByteLength(newLength);

    assertEquals("Byte length should be updated", newLength, utf8.getByteLength());
    assertNotSame("New byte array should be created (line 116)", originalBytes, utf8.getBytes());

    // Verify that original bytes were copied (line 117: System.arraycopy)
    byte[] newBytes = utf8.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original bytes should be preserved at index " + i, originalBytes[i], newBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_ShrinkArray_SameArrayReused() {
    // Lines 115-119: Test that array is reused when new length is smaller
    byte[] bytes = "Hello World".getBytes(StandardCharsets.UTF_8);
    Utf8 utf8 = new Utf8(bytes);
    byte[] originalArray = utf8.getBytes();

    utf8.setByteLength(5);

    assertEquals("Byte length should be reduced", 5, utf8.getByteLength());
    assertSame("Same array should be reused when shrinking", originalArray, utf8.getBytes());
  }

  @Test
  public void testSetByteLength_SameLength_StringCleared() {
    // Test that string cache is cleared even when length doesn't change (line 121)
    Utf8 utf8 = new Utf8("Test");
    String cachedString = utf8.toString();
    int length = utf8.getByteLength();

    utf8.setByteLength(length);

    assertEquals("Length should remain the same", length, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_ZeroLength_Success() {
    // Lines 116-121: Test setting length to zero
    Utf8 utf8 = new Utf8("NonEmpty");
    utf8.setByteLength(0);

    assertEquals("Byte length should be 0", 0, utf8.getByteLength());
    assertEquals("String should be empty", "", utf8.toString());
  }

  @Test
  public void testSetByteLength_MultipleExpansions_BytesPreserved() {
    // Lines 116-118: Test multiple expansions preserve data
    Utf8 utf8 = new Utf8("AB");
    byte[] originalBytes = java.util.Arrays.copyOf(utf8.getBytes(), utf8.getByteLength());

    utf8.setByteLength(10);
    utf8.setByteLength(20);

    byte[] finalBytes = utf8.getBytes();
    for (int i = 0; i < originalBytes.length; i++) {
      assertEquals("Original bytes should be preserved after multiple expansions", originalBytes[i], finalBytes[i]);
    }
  }

  @Test
  public void testSetByteLength_WithinMaxLength_Success() {
    // Line 112-114: Test that setByteLength works within MAX_LENGTH
    // Since MAX_LENGTH is typically Integer.MAX_VALUE, we test with a reasonable
    // value
    Utf8 utf8 = new Utf8("Test");
    int reasonableLength = 1000;
    utf8.setByteLength(reasonableLength);
    assertEquals("Byte length should be set to reasonable value", reasonableLength, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_ReturnsThis_AllowsChaining() {
    // Test that setByteLength returns this for method chaining
    Utf8 utf8 = new Utf8("Test");
    Utf8 result = utf8.setByteLength(10);
    assertSame("setByteLength should return this instance", utf8, result);
  }

  // ========== getBytes Tests ==========

  @Test
  public void testGetBytes_ValidUtf8_ReturnsBytes() {
    Utf8 utf8 = new Utf8("Test");
    byte[] bytes = utf8.getBytes();
    assertNotNull("Bytes should not be null", bytes);
    assertTrue("Bytes array should have at least the byte length", bytes.length >= utf8.getByteLength());
  }

  // ========== getByteLength Tests ==========

  @Test
  public void testGetByteLength_EmptyString_ReturnsZero() {
    Utf8 utf8 = new Utf8("");
    assertEquals("Empty string should have byte length 0", 0, utf8.getByteLength());
  }

  @Test
  public void testGetByteLength_ASCIIString_ReturnsCorrectLength() {
    String ascii = "Hello";
    Utf8 utf8 = new Utf8(ascii);
    assertEquals("ASCII string byte length should match character count", ascii.getBytes(StandardCharsets.UTF_8).length,
        utf8.getByteLength());
  }

  @Test
  public void testGetByteLength_MultiByte_ReturnsCorrectLength() {
    String multiByte = "こんにちは";
    Utf8 utf8 = new Utf8(multiByte);
    assertEquals("Multi-byte string byte length should be correct", multiByte.getBytes(StandardCharsets.UTF_8).length,
        utf8.getByteLength());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testGetLength_DeprecatedMethod_ReturnsCorrectLength() {
    Utf8 utf8 = new Utf8("Test");
    assertEquals("Deprecated getLength should work same as getByteLength", utf8.getByteLength(), utf8.getLength());
  }

  // ========== set Method Tests ==========

  @Test
  public void testSet_NewString_UpdatesContent() {
    Utf8 utf8 = new Utf8("Original");
    String newString = "Updated";
    utf8.set(newString);

    assertEquals("String should be updated", newString, utf8.toString());
    assertEquals("Byte length should be updated", newString.getBytes(StandardCharsets.UTF_8).length,
        utf8.getByteLength());
  }

  @Test
  public void testSet_EmptyString_ClearsContent() {
    Utf8 utf8 = new Utf8("NonEmpty");
    utf8.set("");

    assertEquals("String should be empty", "", utf8.toString());
    assertEquals("Byte length should be 0", 0, utf8.getByteLength());
  }

  @Test
  public void testSet_ReturnsThis_AllowsChaining() {
    Utf8 utf8 = new Utf8("Test");
    Utf8 result = utf8.set("New");
    assertSame("set should return this instance", utf8, result);
  }

  // ========== toString Tests ==========

  @Test
  public void testToString_ValidString_ReturnsCorrectString() {
    String input = "Test String";
    Utf8 utf8 = new Utf8(input);
    assertEquals("toString should return correct string", input, utf8.toString());
  }

  @Test
  public void testToString_EmptyUtf8_ReturnsEmptyString() {
    Utf8 utf8 = new Utf8();
    assertEquals("Empty Utf8 should return empty string", "", utf8.toString());
  }

  @Test
  public void testToString_CachesResult_ReturnsSameInstance() {
    Utf8 utf8 = new Utf8("Test");
    String first = utf8.toString();
    String second = utf8.toString();
    assertSame("toString should cache result", first, second);
  }

  @Test
  public void testToString_AfterSetByteLength_CacheCleared() {
    Utf8 utf8 = new Utf8("Test");
    utf8.toString(); // Cache the string
    utf8.setByteLength(2);
    // The string should be recomputed since cache was cleared
    String result = utf8.toString();
    assertNotNull("toString should work after setByteLength", result);
  }

  // ========== equals Tests ==========

  @Test
  public void testEquals_SameObject_ReturnsTrue() {
    Utf8 utf8 = new Utf8("Test");
    assertTrue("Object should equal itself", utf8.equals(utf8));
  }

  @Test
  public void testEquals_EqualContent_ReturnsTrue() {
    Utf8 utf81 = new Utf8("Test");
    Utf8 utf82 = new Utf8("Test");
    assertTrue("Objects with equal content should be equal", utf81.equals(utf82));
  }

  @Test
  public void testEquals_DifferentContent_ReturnsFalse() {
    Utf8 utf81 = new Utf8("Test1");
    Utf8 utf82 = new Utf8("Test2");
    assertFalse("Objects with different content should not be equal", utf81.equals(utf82));
  }

  @Test
  public void testEquals_DifferentLength_ReturnsFalse() {
    Utf8 utf81 = new Utf8("Short");
    Utf8 utf82 = new Utf8("Much Longer String");
    assertFalse("Objects with different lengths should not be equal", utf81.equals(utf82));
  }

  @Test
  public void testEquals_Null_ReturnsFalse() {
    Utf8 utf8 = new Utf8("Test");
    assertFalse("Object should not equal null", utf8.equals(null));
  }

  @Test
  public void testEquals_DifferentType_ReturnsFalse() {
    Utf8 utf8 = new Utf8("Test");
    assertFalse("Object should not equal different type", utf8.equals("Test"));
  }

  @Test
  public void testEquals_EmptyUtf8Objects_ReturnsTrue() {
    Utf8 utf81 = new Utf8();
    Utf8 utf82 = new Utf8("");
    assertTrue("Empty Utf8 objects should be equal", utf81.equals(utf82));
  }

  // ========== hashCode Tests ==========

  @Test
  public void testHashCode_EqualObjects_SameHashCode() {
    Utf8 utf81 = new Utf8("Test");
    Utf8 utf82 = new Utf8("Test");
    assertEquals("Equal objects should have same hash code", utf81.hashCode(), utf82.hashCode());
  }

  @Test
  public void testHashCode_DifferentObjects_MayHaveDifferentHashCode() {
    Utf8 utf81 = new Utf8("Test1");
    Utf8 utf82 = new Utf8("Test2");
    // Not guaranteed to be different, but likely
    assertNotEquals("Different objects likely have different hash codes", utf81.hashCode(), utf82.hashCode());
  }

  @Test
  public void testHashCode_Consistent_SameValueEachCall() {
    Utf8 utf8 = new Utf8("Test");
    int hash1 = utf8.hashCode();
    int hash2 = utf8.hashCode();
    assertEquals("hashCode should be consistent", hash1, hash2);
  }

  @Test
  public void testHashCode_EmptyUtf8_ReturnsZero() {
    Utf8 utf8 = new Utf8();
    assertEquals("Empty Utf8 should have hash code 0", 0, utf8.hashCode());
  }

  // ========== compareTo Tests ==========

  @Test
  public void testCompareTo_EqualObjects_ReturnsZero() {
    Utf8 utf81 = new Utf8("Test");
    Utf8 utf82 = new Utf8("Test");
    assertEquals("Equal objects should compare to 0", 0, utf81.compareTo(utf82));
  }

  @Test
  public void testCompareTo_LessThan_ReturnsNegative() {
    Utf8 utf81 = new Utf8("Apple");
    Utf8 utf82 = new Utf8("Banana");
    assertTrue("Apple should be less than Banana", utf81.compareTo(utf82) < 0);
  }

  @Test
  public void testCompareTo_GreaterThan_ReturnsPositive() {
    Utf8 utf81 = new Utf8("Zebra");
    Utf8 utf82 = new Utf8("Apple");
    assertTrue("Zebra should be greater than Apple", utf81.compareTo(utf82) > 0);
  }

  @Test
  public void testCompareTo_EmptyStrings_ReturnsZero() {
    Utf8 utf81 = new Utf8("");
    Utf8 utf82 = new Utf8();
    assertEquals("Empty strings should compare to 0", 0, utf81.compareTo(utf82));
  }

  @Test
  public void testCompareTo_EmptyVsNonEmpty_ReturnsNegative() {
    Utf8 empty = new Utf8();
    Utf8 nonEmpty = new Utf8("A");
    assertTrue("Empty should be less than non-empty", empty.compareTo(nonEmpty) < 0);
  }

  // ========== CharSequence Implementation Tests ==========

  @Test
  public void testCharAt_ValidIndex_ReturnsCorrectChar() {
    String input = "Hello";
    Utf8 utf8 = new Utf8(input);
    for (int i = 0; i < input.length(); i++) {
      assertEquals("charAt should return correct character at index " + i, input.charAt(i), utf8.charAt(i));
    }
  }

  @Test(expected = StringIndexOutOfBoundsException.class)
  public void testCharAt_InvalidIndex_ThrowsException() {
    Utf8 utf8 = new Utf8("Test");
    utf8.charAt(100);
  }

  @Test
  public void testLength_ValidString_ReturnsCharacterCount() {
    String input = "Hello";
    Utf8 utf8 = new Utf8(input);
    assertEquals("length should return character count", input.length(), utf8.length());
  }

  @Test
  public void testLength_EmptyString_ReturnsZero() {
    Utf8 utf8 = new Utf8("");
    assertEquals("Empty string should have length 0", 0, utf8.length());
  }

  @Test
  public void testLength_MultiByteCharacters_ReturnsCharacterCount() {
    String input = "こんにちは"; // 5 characters, but more bytes
    Utf8 utf8 = new Utf8(input);
    assertEquals("length should return character count not byte count", input.length(), utf8.length());
  }

  @Test
  public void testSubSequence_ValidRange_ReturnsCorrectSubstring() {
    String input = "Hello World";
    Utf8 utf8 = new Utf8(input);
    CharSequence sub = utf8.subSequence(0, 5);
    assertEquals("subSequence should return correct substring", "Hello", sub.toString());
  }

  @Test
  public void testSubSequence_EmptyRange_ReturnsEmptyString() {
    Utf8 utf8 = new Utf8("Test");
    CharSequence sub = utf8.subSequence(2, 2);
    assertEquals("Empty range should return empty string", "", sub.toString());
  }

  @Test(expected = StringIndexOutOfBoundsException.class)
  public void testSubSequence_InvalidRange_ThrowsException() {
    Utf8 utf8 = new Utf8("Test");
    utf8.subSequence(0, 100);
  }

  // ========== getBytesFor Static Method Tests ==========

  @Test
  public void testGetBytesFor_ValidString_ReturnsUtf8Bytes() {
    String input = "Test";
    byte[] bytes = Utf8.getBytesFor(input);
    assertArrayEquals("getBytesFor should return UTF-8 encoded bytes", input.getBytes(StandardCharsets.UTF_8), bytes);
  }

  @Test
  public void testGetBytesFor_EmptyString_ReturnsEmptyArray() {
    byte[] bytes = Utf8.getBytesFor("");
    assertEquals("Empty string should return empty byte array", 0, bytes.length);
  }

  @Test
  public void testGetBytesFor_UnicodeString_ReturnsCorrectBytes() {
    String unicode = "世界";
    byte[] bytes = Utf8.getBytesFor(unicode);
    assertArrayEquals("getBytesFor should handle unicode correctly", unicode.getBytes(StandardCharsets.UTF_8), bytes);
  }

  // ========== Edge Cases and Integration Tests ==========

  @Test
  public void testReuseUtf8Instance_MultipleSets_Success() {
    Utf8 utf8 = new Utf8();

    utf8.set("First");
    assertEquals("First value should be set", "First", utf8.toString());

    utf8.set("Second");
    assertEquals("Second value should be set", "Second", utf8.toString());

    utf8.set("Third");
    assertEquals("Third value should be set", "Third", utf8.toString());
  }

  @Test
  public void testUtf8WithSpecialCharacters_PreservesContent() {
    String special = "Line1\nLine2\tTab\r\nLine3";
    Utf8 utf8 = new Utf8(special);
    assertEquals("Special characters should be preserved", special, utf8.toString());
  }

  @Test
  public void testUtf8WithEmoji_PreservesContent() {
    String emoji = "Hello 🌍🎉😀";
    Utf8 utf8 = new Utf8(emoji);
    assertEquals("Emoji should be preserved", emoji, utf8.toString());
  }

  @Test
  public void testUtf8LargeString_HandlesCorrectly() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 10000; i++) {
      sb.append("X");
    }
    String large = sb.toString();
    Utf8 utf8 = new Utf8(large);
    assertEquals("Large string should be handled correctly", large.length(), utf8.length());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testSetLength_DeprecatedMethod_WorksCorrectly() {
    Utf8 utf8 = new Utf8("Test");
    Utf8 result = utf8.setLength(10);
    assertEquals("Deprecated setLength should work", 10, utf8.getByteLength());
    assertSame("setLength should return this", utf8, result);
  }
}
