package compressor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * CryptrCompressor combines and compresses multiple files into a zip archive.
 */
public class CryptrCompressor {

    private static final int BUFFER_SIZE = 1024;

    /**
     * Combines and compresses multiple files into a zip archive.
     *
     * @param files - list of file paths for each file to compress
     * @param zipName - name for the created zip archive
     * @return -2 on error creating archive
     *         -1 on success
     *         else returns the index of the file from files which failed to compress
     */
    public static int compress(String[] files, String zipName) {
        byte[] buffer = new byte[BUFFER_SIZE];

        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipName))) {
            for (int i = 0; i < files.length; i++) {
                try {
                    File file = new File(files[i]);
                    FileInputStream fin = new FileInputStream(file);

                    zout.putNextEntry(new ZipEntry(file.getName()));

                    int length;
                    while ((length = fin.read(buffer)) > 0) {
                        zout.write(buffer, 0, length);
                    }

                    zout.closeEntry();
                    fin.close();
                }
                catch (IOException e) {
                    return i;
                }
            }
        }
        catch (IOException e) {
            return -2;
        }

        return -1;
    }

}
