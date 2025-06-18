package app.notesr.cli.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageUtils {
    public static byte[] bufferedImageToBytes(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        return outputStream.toByteArray();
    }
}
