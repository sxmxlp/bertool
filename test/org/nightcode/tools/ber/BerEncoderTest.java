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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.nightcode.tools.ber.BerUtil.hexToByteArray;

public class BerEncoderTest {

  private static byte[] get(ByteBuffer buffer, int offset, int length) {    
    buffer.limit(offset + length);
    buffer.position(offset);
    byte[] frame = new byte[length];
    buffer.get(frame);
    return frame;
  }

  @Test
  public void testEncodePrimitive() throws IOException {
    final byte[] expected = hexToByteArray("9F2608C2C12B098F3DA6E3");
    final byte[] content = hexToByteArray("C2C12B098F3DA6E3");

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(0x9F26, content);

    ByteBuffer buffer = ByteBuffer.allocate(1024);
    BerEncoder berEncoder = new BerEncoder();
    berEncoder.encode(builder, buffer); 

    assertArrayEquals(expected, get(buffer, 0, builder.length()));
  }

  @Test
  public void testEncodeConstructed() {
    final byte[] expected = hexToByteArray(
        "6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060");

    BerBuilder builderA5 = BerBuilder.newInstance();
    builderA5.add(0x88, new byte[] {0x02});
    builderA5.addAsciiString(0x5F2D, "en");

    BerBuilder builder6F = BerBuilder.newInstance();
    builder6F.addHexString(0x84, "315041592E5359532E4444463031");
    builder6F.add(0xA5, builderA5);

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(0x6F, builder6F);
    builder.add(0x9F36, new byte[] {0x00, 0x60});

    ByteBuffer buffer = ByteBuffer.allocate(1024);
    BerEncoder berEncoder = new BerEncoder();
    berEncoder.encode(builder, buffer);

    assertArrayEquals(expected, get(buffer, 0, builder.length()));
  }

  @Test
  public void testEncodeConstructedByteArray() {
    final byte[] expected = hexToByteArray(
        "6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060");

    BerBuilder builderA5 = BerBuilder.newInstance();
    builderA5.add(0x88, new byte[] {0x02});
    builderA5.addAsciiString(0x5F2D, "en");

    BerBuilder builder6F = BerBuilder.newInstance();
    builder6F.addHexString(0x84, "315041592E5359532E4444463031");
    builder6F.add(0xA5, builderA5);

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(0x6F, builder6F);
    builder.add(0x9F36, new byte[] {0x00, 0x60});

    byte[] buffer = new byte[builder.length()];
    BerEncoder berEncoder = new BerEncoder();
    berEncoder.encode(builder, buffer);

    assertArrayEquals(expected, buffer);
  }

  @Test
  public void testEncodeConstructedWithOffset() {
    final byte[] expected = hexToByteArray(
        "6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060");

    BerBuilder builderA5 = BerBuilder.newInstance();
    builderA5.add(0x88, hexToByteArray("02"));
    builderA5.addAsciiString(0x5F2D, "en");

    BerBuilder builder6F = BerBuilder.newInstance();
    builder6F.addHexString(0x84, "315041592E5359532E4444463031");
    builder6F.add(0xA5, builderA5);

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(0x6F, builder6F);
    builder.add(0x9F36, hexToByteArray("0060"));

    final int offset = 10;
    final ByteBuffer buffer = ByteBuffer.allocate(expected.length + offset);
    buffer.put((byte) 0xE1);
    buffer.position(offset);
    BerEncoder berEncoder = new BerEncoder();
    berEncoder.encode(builder, buffer, offset); 

    assertArrayEquals(expected, get(buffer, offset, builder.length()));
  }

  @Test
  public void testEncodeDefiniteLongForm() {
    final byte[] identifier = hexToByteArray("84");
    Random random = new Random();
    final byte[] content = new byte[435];
    random.nextBytes(content);

    final byte[] expected = new byte[content.length + 4];
    expected[0] = (byte) 0x84;
    expected[1] = (byte) 0x82;
    expected[2] = (byte) 0x01;
    expected[3] = (byte) 0xB3;
    System.arraycopy(content, 0, expected, 4, content.length);

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(identifier, content);

    ByteBuffer buffer = ByteBuffer.allocate(1024);
    BerEncoder berEncoder = new BerEncoder();
    berEncoder.encode(builder, buffer); 

    assertArrayEquals(expected, get(buffer, 0, builder.length()));
  }
}
