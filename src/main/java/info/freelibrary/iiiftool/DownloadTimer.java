
package info.freelibrary.iiiftool;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * A timed downloader that downloads what the first view within Mirador downloads. Used as a baseline for changes to the
 * image server.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class DownloadTimer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadTimer.class);

    private static final int DEFAULT_DOWNLOADER_COUNT = 10;

    private static final int DEFAULT_TILE_SIZE = 1024;

    private static final int OSD_INITIAL_TILE_COUNT = 4;

    /**
     * A timed downloader that downloads what the first view within Mirador downloads.
     *
     * @param args The arguments to the downloader
     * @throws MalformedURLException If the supplied IIIF server URL is invalid
     * @throws IOException If there is trouble reading the manifest from the IIIF server
     */
    public static void main(final String[] args) throws MalformedURLException, IOException {
        final DownloadReport report = new DownloadReport();
        final StopWatch stopWatch = new StopWatch();

        ExecutorService executor;
        long totalTime;

        // We need at least IIIF server URL and ID as arguments; can also supply number of threads to use
        if (args.length < 2) {
            LOGGER.error("Please supply a IIIF server and manifest ID");
            System.exit(1);
        } else {
            final String dlURL = getURL(args[0], args[1]);
            final int dlCount = getDownloaderCount(args);
            final double height;
            final double width;

            Iterator<Long> iterator;
            List<String> results;
            String infoID;
            String json;

            LOGGER.debug("Getting manifest: {}", dlURL);

            stopWatch.start();
            json = IOUtils.toString(new URL(dlURL), StandardCharsets.UTF_8);
            stopWatch.stop();
            report.setManifestTime(stopWatch.getTime());
            stopWatch.reset();

            // Not timing because we're interested in download times, not parsing times
            results = XQueryUtils.getList(json, "?sequences?*?canvases?*?thumbnail");

            LOGGER.debug("Requesting {} thumbnail images", results.size());

            stopWatch.start();
            executor = Executors.newFixedThreadPool(dlCount);

            for (int thumbCount = 0; thumbCount < results.size(); thumbCount += dlCount) {
                for (int index = 0; index < dlCount; index++) {
                    final int tnIndex = thumbCount + index;

                    if (tnIndex >= results.size()) {
                        break;
                    }

                    executor.execute(new DownloadThread(results.get(tnIndex), report));
                }
            }

            try {
                executor.shutdown();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
            } catch (final InterruptedException details) {
                System.err.println(details.getMessage());
                System.exit(1);
            }

            // We also want to load the initial tiles for the image that OSD will load
            infoID = XQueryUtils.getValue(json, "?sequences?*?canvases?1?images?1?resource?item?1?service?('@id')");
            json = IOUtils.toString(new URL(infoID + "/info.json"), StandardCharsets.UTF_8);
            width = Double.parseDouble(XQueryUtils.getValue(json, "?width"));
            height = Double.parseDouble(XQueryUtils.getValue(json, "?height"));
            infoID = infoID.substring(infoID.lastIndexOf('/') + 1);
            results = ImageUtils.getTilePaths(args[0], infoID, DEFAULT_TILE_SIZE, width, height);

            LOGGER.debug("Requesting {} tile images from: {}", OSD_INITIAL_TILE_COUNT, infoID);

            // We just want the ones that OpenSeadragon loads first, not all of them
            results = results.subList(0, OSD_INITIAL_TILE_COUNT);
            executor = Executors.newFixedThreadPool(OSD_INITIAL_TILE_COUNT);

            for (int index = 0; index < OSD_INITIAL_TILE_COUNT; index++) {
                executor.execute(new DownloadThread(results.get(index), report));
            }

            try {
                executor.shutdown();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
            } catch (final InterruptedException details) {
                System.err.println(details.getMessage());
                System.exit(1);
            }

            LOGGER.debug("Generating download report");

            stopWatch.stop();
            totalTime = report.getManifestTime();
            iterator = report.getThumbnailTimes().iterator();

            while (iterator.hasNext()) {
                totalTime += iterator.next().longValue();
            }

            iterator = report.getTileTimes().iterator();

            // The actual time is longer than perceived time because our URL requests are threaded
            LOGGER.info("Total actual time: {} secs ({} ms)", TimeUnit.SECONDS.convert(totalTime,
                    TimeUnit.MILLISECONDS), totalTime);

            // We restarted the time after the manifest retrieval, so we need to add that to our total
            totalTime = stopWatch.getTime() + report.getManifestTime();
            LOGGER.info("Total perceived time: {} secs ({} ms)", TimeUnit.SECONDS.convert(totalTime,
                    TimeUnit.MILLISECONDS), totalTime);
        }
    }

    private static String getURL(final String aServer, final String aID) {
        try {
            final String iiifServer = new URL(aServer).toExternalForm();
            final String iiifID = URLEncoder.encode(aID, StandardCharsets.UTF_8.name());
            final String url;

            if (!iiifServer.endsWith("/")) {
                url = iiifServer + "/" + iiifID + "/manifest";
            } else {
                url = iiifServer + iiifID + "/manifest";
            }

            return url;
        } catch (final MalformedURLException | UnsupportedEncodingException details) {
            throw new RuntimeException(details);
        }
    }

    private static int getDownloaderCount(final String... args) {
        if (args.length == 3) {
            try {
                return Integer.parseInt(args[2]);
            } catch (final NumberFormatException details) {
                LOGGER.error("Third argument should be an integer for thread count");
                System.exit(1);
            }
        }

        return DEFAULT_DOWNLOADER_COUNT;
    }
}
