package app.notesr.cli.util;

import app.notesr.cli.exception.ThumbnailExtractionException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MediaThumbnailUtils {
    private static final String OUTPUT_THUMBNAIL_FORMAT = "png";

    public static byte[] getImageThumbnail(File imageFile, int width, int height) {
        try {
            BufferedImage img = ImageIO.read(imageFile);
            BufferedImage thumbnail = ImageResizer.cropAndScale(img, width, height);

            return ImageUtils.bufferedImageToBytes(thumbnail, OUTPUT_THUMBNAIL_FORMAT);
        } catch (IOException e) {
            throw new ThumbnailExtractionException("Cannot read image", e);
        }
    }

    public static byte[] getVideoThumbnail(File videoFile, int width, int height, int seconds) {
        try (FileChannelWrapper channelWrapper = NIOUtils.readableChannel(videoFile)) {
            FrameGrab grab = FrameGrab.createFrameGrab(channelWrapper);
            grab.seekToSecondPrecise(seconds);

            Picture frame = grab.getNativeFrame();
            BufferedImage rawImage = AWTUtil.toBufferedImage(frame);

            BufferedImage thumbnail = ImageResizer.cropAndScale(rawImage, width, height);
            return ImageUtils.bufferedImageToBytes(thumbnail, OUTPUT_THUMBNAIL_FORMAT);
        } catch (IOException e) {
            throw new ThumbnailExtractionException("Cannot read video", e);
        } catch (JCodecException e) {
            throw new ThumbnailExtractionException("Cannot decode video", e);
        }
    }
}
