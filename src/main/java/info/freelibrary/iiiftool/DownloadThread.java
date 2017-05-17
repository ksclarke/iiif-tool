
package info.freelibrary.iiiftool;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.time.StopWatch;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * A downloader thread that downloads requested URLs, recording how long the download took.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class DownloadThread implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadThread.class);

    private final String myURL;

    private final DownloadReport myReport;

    /**
     * Creates a new downloader thread.
     *
     * @param aURL
     * @param aReport
     */
    public DownloadThread(final String aURL, final DownloadReport aReport) {
        myReport = aReport;
        myURL = aURL;
    }

    @Override
    public void run() {
        final StopWatch stopWatch = new StopWatch();

        LOGGER.debug("Downloading: {}", myURL);

        stopWatch.start();

        try {
            final URL url = new URL(myURL);
            final BufferedImage image = ImageIO.read(url);
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final long time;

            // We need to write the image into memory in order to get its size
            ImageIO.write(image, FileUtils.getExt(myURL), bytes);
            bytes.close();

            stopWatch.stop();
            time = stopWatch.getTime();

            LOGGER.debug("Downloaded '{}' bytes in '{}' ms", bytes.size(), time);

            myReport.addThumbnailTime(new Long(time));
        } catch (final IOException details) {
            throw new RuntimeException(details);
        }
    }

    @Override
    public String toString() {
        return myURL;
    }
}
