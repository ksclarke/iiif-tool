package info.freelibrary.iiiftool;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

/**
 * A utility class for some common image processing needs.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ImageUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);

    /* Template for the region part of the IIIF request */
    private static final String REGION = "{},{},{},{}";

    /* All out-of-the-box tiles are not rotated */
    private static final String LABEL = "0/default.jpg";

    private ImageUtils() {
    }

    /**
     * Return a list of derivative images to be pre-generated so that the OpenSeadragon viewer can use them.
     *
     * @return A list of derivative images to be pre-generated
     */
    public static List<String> getTilePaths(final String aService, final String aID, final int aTileSize,
            final double aWidth, final double aHeight) {
        return getTilePaths(aService, aID, aTileSize, (int) aWidth, (int) aHeight);
    }

    /**
     * Return a list of derivative images to be pre-generated so that the OpenSeadragon viewer can use them.
     *
     * @return A list of derivative images to be pre-generated
     */
    public static List<String> getTilePaths(final String aService, final String aID, final int aTileSize,
            final int aWidth, final int aHeight) {
        final ArrayList<String> list = new ArrayList<>();
        final int longDim = Math.max(aWidth, aHeight);
        final String id;

        // Object ID may need to be URL encoded for use on the Web
        try {
            id = URLEncoder.encode(aID, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException details) {
            throw new RuntimeException(details); // All JVMs required to support UTF-8
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Generating tile paths [ID: {}; Tile Size: {}; Width: {}; Height: {}]", aID, aTileSize, aWidth,
                    aHeight);
        }

        for (int multiplier = 1; multiplier * aTileSize < longDim; multiplier *= 2) {
            final int tileSize = multiplier * aTileSize;

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Creating tiles using multiplier of {}", multiplier);
            }

            int x = 0;
            int y = 0;
            int xTileSize;
            int yTileSize;

            String region;
            String path;
            String size;

            for (x = 0; x < aWidth + tileSize; x += tileSize) {
                xTileSize = x + tileSize < aWidth ? tileSize : aWidth - x;
                yTileSize = tileSize < aHeight ? tileSize : aHeight;

                if (xTileSize > 0 && yTileSize > 0) {
                    region = StringUtils.format(REGION, x, y, xTileSize, yTileSize);
                    size = getSize(multiplier, xTileSize, yTileSize);

                    // Support the canonical 2.0 Image API URI syntax
                    if (ratio(xTileSize, yTileSize).equals(ratio(size))) {
                        size = size.substring(0, size.indexOf(',') + 1);
                    }

                    path = StringUtils.toString('/', aService, id, region, size, LABEL);

                    if (!list.add(path)) {
                        LOGGER.warn("Tile path '{}' could not be added to queue", path);
                    }
                }

                for (y = tileSize; y < aHeight + tileSize; y += tileSize) {
                    xTileSize = x + tileSize < aWidth ? tileSize : aWidth - x;
                    yTileSize = y + tileSize < aHeight ? tileSize : aHeight - y;

                    if (xTileSize > 0 && yTileSize > 0) {
                        region = StringUtils.format(REGION, x, y, xTileSize, yTileSize);
                        size = getSize(multiplier, xTileSize, yTileSize);

                        // Support the canonical 2.0 Image API URI syntax
                        if (ratio(xTileSize, yTileSize).equals(ratio(size))) {
                            size = size.substring(0, size.indexOf(',') + 1);
                        }

                        path = StringUtils.toString('/', aService, id, region, size, LABEL);

                        if (!list.add(path)) {
                            LOGGER.warn("Tile path '{}' could not be added to queue", path);
                        }
                    }
                }

                y = 0;
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} tiles needed for {}", list.size(), aID);

            for (final Object path : list.toArray()) {
                LOGGER.debug("Tile path: {}", path);
            }
        }

        // Puts the tiles first requested at the top of the list
        Collections.reverse(list);
        return Collections.unmodifiableList(list);
    }

    /**
     * Gets the ratio of the supplied width and height.
     *
     * @param aWidth Width to use in getting the ratio
     * @param aHeight Height to use in getting the ratio
     * @return A string representation of the ratio
     */
    public static String ratio(final int aWidth, final int aHeight) {
        final int gcd = gcd(aWidth, aHeight);
        return aHeight / gcd + ":" + aHeight / gcd;
    }

    /**
     * Gets the ratio from the supplied IIIF size string.
     *
     * @param aSize A IIIF image size string
     * @return A string representation of the ratio
     */
    public static String ratio(final String aSize) {
        final String[] widthHeight = aSize.split("\\,");

        if (widthHeight.length != 2) {
            throw new IllegalArgumentException("Argument is not a comma delimited size: " + aSize);
        }

        return ratio(Integer.parseInt(widthHeight[0]), Integer.parseInt(widthHeight[1]));
    }

    private static String getSize(final double aMultiplier, final int aXTileSize, final int aYTileSize) {
        return (int) Math.ceil(aXTileSize / aMultiplier) + "," + (int) Math.ceil(aYTileSize / aMultiplier);
    }

    private static int gcd(final int aWidth, final int aHeight) {
        if (aHeight == 0) {
            return aWidth;
        } else {
            return gcd(aHeight, aWidth % aHeight);
        }
    }
}