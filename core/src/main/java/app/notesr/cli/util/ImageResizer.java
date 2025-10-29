package app.notesr.cli.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageResizer {
    public static BufferedImage cropAndScale(BufferedImage source, int targetWidth, int targetHeight) {
        double sourceRatio = (double) source.getWidth() / source.getHeight();
        double targetRatio = (double) targetWidth / targetHeight;

        int cropWidth = source.getWidth();
        int cropHeight = source.getHeight();

        if (sourceRatio > targetRatio) {
            cropWidth = (int) (source.getHeight() * targetRatio);
        } else {
            cropHeight = (int) (source.getWidth() / targetRatio);
        }

        int x = (source.getWidth() - cropWidth) / 2;
        int y = (source.getHeight() - cropHeight) / 2;

        BufferedImage cropped = source.getSubimage(x, y, cropWidth, cropHeight);

        Image scaled = cropped.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = output.createGraphics();

        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        return output;
    }
}
