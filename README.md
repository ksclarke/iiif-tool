# IIIF Tools

This is just a simple (probably ephemeral) project in which to temporarily put some IIIF related things.

# Building

To build the project's executable jar file:

    git clone https://github.com/ksclarke/iiif-tools.git
    cd iiif-tools
    mvn install

If you want to build with the logging set to DEBUG level, then replace `mvn install` with:

    mvn -Dlogging.level=DEBUG install

# Running

To run the project's executable jar file against a particular IIIF manifest (after you've built it):

    java -jar target/iiif-tool-0.0.1-SNAPSHOT-exec.jar "https://your.iiif.server/iiif" "ark:/99999/z1kk9tdk"

Output should look like:

    [INFO] info.freelibrary.iiiftool.DownloadTimer | Total actual time: 22 secs (22362 ms)
    [INFO] info.freelibrary.iiiftool.DownloadTimer | Total perceived time: 5 secs (5602 ms)    

# What it does

Right now, it just takes a IIIF server URL (with a service prefix -- in the example: /iiif) and a manifest ID. It uses that info to download all the thumbnails that Mirador would show on load and the first four tiles that OpenSeadragon shows. It times how long it took to do that. That's it.

If you want to control how many threads it uses to download the thumbnails, you can run:

    java -jar target/iiif-tool-0.0.1-SNAPSHOT-exec.jar "https://your.iiif.server/iiif" "ark:/99999/z1kk9tdk" 8

By default it uses ten. It's attempting to mimic Mirador's downloading of a page's worth of images. Different browsers may use a different number of threads to download, so it's configurable.

The "actual time" it reports is how long it took to download all the images as if it had done them sequentially and the "perceived time" it reports is the total amount of time it took, threaded.

# Why?

We'd like to compare the impact of putting our server in different AWS regions, pulling images from S3 vs. local disk, etc. We'd like to approximate the unit of measurement as a single Mirador page (realizing, though, that Mirador is doing more than just downloading images).

# License

[The 3-Clause BSD License](https://opensource.org/licenses/BSD-3-Clause)

# Contact

Feel free to <a href="mailto:ksclarke@ksclarke.io">contact me</a> if you notice I've done something horribly wrong in the code (or for any other reason).
