package com.example.BackendArchitectureLab.Util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class AudioProcessUtilTest {

    @Test
    @DisplayName("NullValue 單例應正確運作")
    void nullValueInstance() {
        assertNotNull(NullValue.INSTANCE);
        assertSame(NullValue.INSTANCE, NullValue.INSTANCE);
    }

    @Test
    @DisplayName("空的 WAV 應回傳空陣列")
    void emptyWavShouldReturnEmptyArray() throws Exception {
        AudioProcessUtil util = new AudioProcessUtil();
        java.lang.reflect.Method method = AudioProcessUtil.class.getDeclaredMethod("parseWavToFloatArray", java.io.File.class);
        method.setAccessible(true);

        java.io.File tempWav = java.io.File.createTempFile("empty_", ".wav");
        try {
            java.nio.file.Files.write(tempWav.toPath(), new byte[10]);
            float[] result = (float[]) method.invoke(util, tempWav);
            assertNotNull(result);
            assertEquals(0, result.length);
        } finally {
            tempWav.delete();
        }
    }

    @Test
    @DisplayName("有效的 WAV 應正確解析 PCM 資料")
    void validWavShouldParseCorrectly() throws Exception {
        AudioProcessUtil util = new AudioProcessUtil();
        java.lang.reflect.Method method = AudioProcessUtil.class.getDeclaredMethod("parseWavToFloatArray", java.io.File.class);
        method.setAccessible(true);

        byte[] wavBytes = createMinimalWav();
        java.io.File tempWav = java.io.File.createTempFile("test_", ".wav");
        try {
            java.nio.file.Files.write(tempWav.toPath(), wavBytes);
            float[] result = (float[]) method.invoke(util, tempWav);
            assertNotNull(result);
            assertEquals(2, result.length);
            assertEquals(0.5f, result[0], 0.001);
            assertEquals(-0.5f, result[1], 0.001);
        } finally {
            tempWav.delete();
        }
    }

    private byte[] createMinimalWav() {
        int numSamples = 2;
        int bytesPerSample = 2;
        int dataSize = numSamples * bytesPerSample;
        int fileSize = 44 + dataSize;

        ByteBuffer buf = ByteBuffer.allocate(fileSize);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        buf.put((byte) 'R').put((byte) 'I').put((byte) 'F').put((byte) 'F');
        buf.putInt(fileSize - 8);
        buf.put((byte) 'W').put((byte) 'A').put((byte) 'V').put((byte) 'E');
        buf.put((byte) 'f').put((byte) 'm').put((byte) 't').put((byte) ' ');
        buf.putInt(16);
        buf.putShort((short) 1);
        buf.putShort((short) 1);
        buf.putInt(16000);
        buf.putInt(16000 * bytesPerSample);
        buf.putShort((short) bytesPerSample);
        buf.putShort((short) (bytesPerSample * 8));
        buf.put((byte) 'd').put((byte) 'a').put((byte) 't').put((byte) 'a');
        buf.putInt(dataSize);
        buf.putShort((short) (0.5f * 32768));
        buf.putShort((short) (-0.5f * 32768));

        return buf.array();
    }
}
