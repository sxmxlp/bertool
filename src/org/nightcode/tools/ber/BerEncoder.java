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

import java.nio.ByteBuffer;

/**
 * The BerEncoder performs encoding BER packet.
 */
public class BerEncoder {

  /**
   * Encode the BER data which contains in the supplied builder.
   *
   * @param builder which contains BER tags
   * @param dst the destination of encoded content
   */
  public void encode(BerBuilder builder, byte[] dst) {
    final ByteBuffer byteBuffer = ByteBuffer.wrap(dst);
    encode(builder, byteBuffer);
  }

  /**
   * Encode the BER data which contains in the supplied builder.
   *
   * @param builder which contains BER tags
   * @param dstBuffer the destination of encoded content
   */
  public void encode(BerBuilder builder, ByteBuffer dstBuffer) {
    encode(builder, dstBuffer, 0);
  }

  /**
   * Encode the BER data which contains in the supplied builder.
   *
   * @param builder which contains BER tags
   * @param dstBuffer the destination of encoded content
   * @param offset in the supplied dstBuffer
   */
  public void encode(BerBuilder builder, ByteBuffer dstBuffer, int offset) {
    final BerBuffer berBuffer = BerBufferUtil.create(dstBuffer);
    berBuffer.checkLimit(offset + builder.length());
    builder.writeTo(berBuffer, offset);
  }
}
