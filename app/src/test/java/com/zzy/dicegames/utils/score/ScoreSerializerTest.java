package com.zzy.dicegames.utils.score;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.zzy.dicegames.utils.score.TestData.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class ScoreSerializerTest {
    @Test
    public void testSerialize() throws IOException {
        try (var outputStream = new ByteArrayOutputStream()) {
            var serializer = new ScoreSerializer(outputStream, scoresDTO);
            serializer.serialize();
            assertEquals(xmlString, outputStream.toString(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testSerializeEmptyData() throws IOException {
        try (var outputStream = new ByteArrayOutputStream()) {
            var serializer = new ScoreSerializer(outputStream, emptyScoresDTO);
            serializer.serialize();
            assertEquals(emptyXmlString, outputStream.toString(StandardCharsets.UTF_8));
        }
    }
}
