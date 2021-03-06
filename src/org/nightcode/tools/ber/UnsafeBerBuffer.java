/*
 * Copyright (C) 2019 The NightCode Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.nightcode.tools.ber;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import sun.misc.Unsafe;

final class UnsafeBerBuffer implements BerBuffer {

  private static final Unsafe UNSAFE;
  private static final ByteOrder NATIVE_BYTE_ORDER = ByteOrder.nativeOrder();

  static {
    try {
      final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>() {
        @Override public Unsafe run() throws Exception {
          final Field field = Unsafe.class.getDeclaredField("theUnsafe");
          field.setAccessible(true);
          return (Unsafe) field.get(null);
        }
      };
      UNSAFE = AccessController.doPrivileged(action);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private static final long BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);

  private final byte[] array;
  private final ByteBuffer buffer;
  private final long addressOffset;
  private final int capacity;

  UnsafeBerBuffer(byte[] src) {
    addressOffset = BYTE_ARRAY_OFFSET;
    array = src;
    buffer = null;
    capacity = array.length;
  }

  UnsafeBerBuffer(ByteBuffer src) {
    if (src.hasArray()) {
      addressOffset = BYTE_ARRAY_OFFSET + src.arrayOffset();
      array = src.array();
    } else {
      addressOffset = ((sun.nio.ch.DirectBuffer) src).address();
      array = null;
    }
    buffer = src;
    capacity = src.capacity();
  }

  @Override public int capacity() {
    return capacity;
  }

  @Override public int checkIndex(final int index) {
    if (index >= capacity) {
      throw new IndexOutOfBoundsException(String.format("index is beyond bound (i=%d; b=%d)"
          , index, capacity - 1));
    }
    return index;
  }

  @Override public int checkLimit(final int limit) {
    if (limit > capacity) {
      throw new IndexOutOfBoundsException(String.format("limit is beyond capacity (l=%d; c=%d)"
          , limit, capacity));
    }
    return limit;
  }

  @Override public ByteBuffer duplicateByteBuffer() {
    return (buffer != null) ? buffer.duplicate() : ByteBuffer.wrap(array);
  }

  @Override public byte getByte(final int index) {
    return UNSAFE.getByte(array, addressOffset + index);
  }

  @Override public int getBytes(final int index, final byte[] dst) {
    return getBytes(index, dst, 0, dst.length);
  }

  @Override public int getBytes(final int index, final byte[] dst, final int offset,
      final int length) {
    final int count = Math.min(length, capacity - index);
    UNSAFE.copyMemory(array, addressOffset + index, dst, BYTE_ARRAY_OFFSET + offset, count);
    return count;
  }

  @Override public int getBytes(final int index, final ByteBuffer dstBuffer, final int length) {
    int count = Math.min(dstBuffer.remaining(), capacity - index);
    count = Math.min(count, length);

    final byte[] dstArray;
    final long dstOffset;
    if (dstBuffer.hasArray()) {
      dstArray = dstBuffer.array();
      dstOffset = BYTE_ARRAY_OFFSET + dstBuffer.arrayOffset();
    } else {
      dstArray = null;
      dstOffset = ((sun.nio.ch.DirectBuffer) dstBuffer).address();
    }

    UNSAFE.copyMemory(array, addressOffset + index
        , dstArray, dstOffset + dstBuffer.position(), count);
    dstBuffer.position(dstBuffer.position() + count);
    return count;
  }

  @Override public void putByte(final int index, final byte value) {
    UNSAFE.putByte(array, addressOffset + index, value);
  }

  @Override public int putBytes(final int index, final byte[] src) {
    return putBytes(index, src, 0, src.length);
  }

  @Override public int putBytes(final int index, final byte[] src, final int offset,
      final int length) {
    final int count = Math.min(length, capacity - index);
    UNSAFE.copyMemory(src, BYTE_ARRAY_OFFSET + offset, array, addressOffset + index, count);
    return count;
  }

  @Override public int putBytes(final int index, final ByteBuffer srcBuffer, final int length) {
    int count = Math.min(srcBuffer.remaining(), capacity - index);
    count = Math.min(count, length);

    final byte[] srcArray;
    final long srcOffset;
    if (srcBuffer.hasArray()) {
      srcArray = srcBuffer.array();
      srcOffset = BYTE_ARRAY_OFFSET + srcBuffer.arrayOffset();
    } else {
      srcArray = null;
      srcOffset = ((sun.nio.ch.DirectBuffer) srcBuffer).address();
    }

    UNSAFE.copyMemory(srcArray, srcOffset + srcBuffer.position()
        , array, addressOffset + index, count);
    srcBuffer.position(srcBuffer.position() + count);
    return count;
  }

  @Override public void putInt(final int index, final int value) {
    int ordered = value;
    if (NATIVE_BYTE_ORDER == ByteOrder.LITTLE_ENDIAN) {
      ordered = Integer.reverseBytes(value);
    }
    UNSAFE.putInt(array, addressOffset + index, ordered);
  }
}
