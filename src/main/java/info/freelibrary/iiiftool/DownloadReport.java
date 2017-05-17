
package info.freelibrary.iiiftool;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A simple reporting of the timing of the download tasks.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class DownloadReport {

    private long myManifestTime;

    private final CopyOnWriteArrayList<Long> myThumbnailTimes;

    private final CopyOnWriteArrayList<Long> myTileTimes;

    /**
     * Creates a new download report.
     */
    public DownloadReport() {
        myThumbnailTimes = new CopyOnWriteArrayList<>();
        myTileTimes = new CopyOnWriteArrayList<>();
    }

    /**
     * Sets the time it took the manifest to download.
     *
     * @param aTime The number of milliseconds it took the manifest to download.
     */
    public void setManifestTime(final long aTime) {
        myManifestTime = aTime;
    }

    /**
     * Gets the time it took the manifest to download.
     *
     * @return The number of milliseconds it took the manifest to download.
     */
    public long getManifestTime() {
        return myManifestTime;
    }

    /**
     * Add a download time for a thumbnail request.
     *
     * @param aTime The number of milliseconds it took to download a thumbnail.
     * @return True if the download time could be recorded; else, false
     */
    public boolean addThumbnailTime(final long aTime) {
        return myThumbnailTimes.add(aTime);
    }

    /**
     * Gets the download times for the thumbnails.
     *
     * @return The thumbnail download times expressed in milliseconds
     */
    public List<Long> getThumbnailTimes() {
        return myThumbnailTimes;
    }

    /**
     * Add a download time for a tile request.
     *
     * @param aTime The number of milliseconds it took to download a tile.
     * @return True if the download time could be recorded; else, false
     */
    public boolean addTileTime(final long aTime) {
        return myTileTimes.add(aTime);
    }

    /**
     * Gets the download times for the tiles.
     *
     * @return The tile download times expressed in milliseconds
     */
    public List<Long> getTileTimes() {
        return myTileTimes;
    }

}
