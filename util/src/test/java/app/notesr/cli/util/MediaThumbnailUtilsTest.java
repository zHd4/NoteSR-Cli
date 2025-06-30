package app.notesr.cli.util;

import app.notesr.cli.exception.ThumbnailExtractionException;
import org.jcodec.api.JCodecException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaThumbnailUtilsTest {
    private static final int TEST_THUMBNAIL_WIDTH = 100;
    private static final int TEST_THUMBNAIL_HEIGHT = 100;
    private static final int TEST_VIDEO_SECONDS = 1;

    @TempDir
    private Path tempDir;

    @ParameterizedTest
    @ValueSource(strings = {"test_image.jpeg", "test_image.jpg", "test_image.png", "test_image.webp"})
    void testGetImageThumbnail(String fixtureFileName) throws IOException {
        File imageFile = getFixturePath(fixtureFileName, tempDir).toFile();
        byte[] thumbnailBytes = MediaThumbnailUtils.getImageThumbnail(imageFile,
                TEST_THUMBNAIL_WIDTH, TEST_THUMBNAIL_HEIGHT);

        assertNotNull(thumbnailBytes, "Thumbnail byte array must be not null");
        assertTrue(thumbnailBytes.length > 0, "Thumbnail byte array must be not empty");

        BufferedImage thumbnail = ImageIO.read(new ByteArrayInputStream(thumbnailBytes));
        assertEquals(TEST_THUMBNAIL_WIDTH, thumbnail.getWidth(), "Unexpected width");
        assertEquals(TEST_THUMBNAIL_HEIGHT, thumbnail.getHeight(), "Unexpected height");
    }

    @Test
    void testGetImageThumbnailWithCorruptedImage() {
        File corruptedFile = new File("/corrupted_image.jpg");
        Exception exception = assertThrows(ThumbnailExtractionException.class, () ->
                MediaThumbnailUtils.getImageThumbnail(corruptedFile, TEST_THUMBNAIL_WIDTH, TEST_THUMBNAIL_HEIGHT));

        assertInstanceOf(IOException.class, exception.getCause(), "Unexpected exception instance");
    }

    @Test
    void testGetVideoThumbnail() throws IOException {
        File videoFile = getFixturePath("test_video.mp4", tempDir).toFile();
        byte[] thumbnailBytes = MediaThumbnailUtils.getVideoThumbnail(videoFile, TEST_THUMBNAIL_WIDTH,
                TEST_THUMBNAIL_HEIGHT, TEST_VIDEO_SECONDS);

        assertNotNull(thumbnailBytes, "Thumbnail byte array must be not null");
        assertTrue(thumbnailBytes.length > 0, "Thumbnail byte array must be not empty");

        BufferedImage thumbnail = ImageIO.read(new ByteArrayInputStream(thumbnailBytes));
        assertEquals(TEST_THUMBNAIL_WIDTH, thumbnail.getWidth(), "Unexpected width");
        assertEquals(TEST_THUMBNAIL_HEIGHT, thumbnail.getHeight(), "Unexpected height");
    }

    @Test
    void testGetVideoThumbnailWithCorruptedVideo() {
        File fakeVideo = new File("/not_a_video.txt");

        Exception exception = assertThrows(ThumbnailExtractionException.class, () ->
                MediaThumbnailUtils.getVideoThumbnail(fakeVideo, TEST_THUMBNAIL_WIDTH, TEST_THUMBNAIL_HEIGHT,
                TEST_VIDEO_SECONDS));

        Throwable cause = exception.getCause();
        assertTrue(cause instanceof JCodecException || cause instanceof IOException,
                "Unexpected exception instance");
    }
}
