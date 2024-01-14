package org.example.demounzip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SpringBootApplication
public class DemoUnzipApplication {
    private static final int LINES_PER_FILE = 500000;
    public static final String D_DOWNLOADS_28_EX_CSV_ASVP_ZIP = "D:\\Downloads\\28-ex_csv_asvp.zip";
    public static final String D_DOWNLOADS_TEMP_OUT_CSV = "D:\\Downloads\\temp\\out.csv";
    public static final String WINDOWS_1251 = "Windows-1251";

    public static void main(String[] args) throws IOException {
        SpringApplication.run(DemoUnzipApplication.class, args);

        File destDir = new File(D_DOWNLOADS_TEMP_OUT_CSV);

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(D_DOWNLOADS_28_EX_CSV_ASVP_ZIP))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if (!zipEntry.isDirectory()) {
                    // Create a new text file with the same name as the zip entry
                    File newFile = new File(destDir, zipEntry.getName());

                    // Create parent directories if they don't exist
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // Write file content line by line
                    write(zis, newFile, destDir, zipEntry);
                }
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }

        }
    }

    private static void write(ZipInputStream zis, File newFile, File destDir, ZipEntry zipEntry) throws IOException {
        int linesWritten = 0;
        int fileIndex = 0;
        BufferedWriter writer = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(zis, Charset.forName(WINDOWS_1251)))) {
            writer = new BufferedWriter(new FileWriter(newFile));
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine(); // Add a newline after each line
                linesWritten++;

                if (linesWritten >= LINES_PER_FILE) {
                    // Start writing to a new file
                    writer.close();
                    linesWritten = 0;
                    fileIndex++;
                    newFile = new File(destDir, getNextFileName(zipEntry.getName(), fileIndex));
                    writer = new BufferedWriter(new FileWriter(newFile));
                }
            }
        } finally {
            if (writer != null) writer.close();
        }
    }

    private static String getNextFileName(String originalFileName, int chunkIndex) {
        int dotIndex = originalFileName.lastIndexOf('.');
        String baseName = (dotIndex == -1) ? originalFileName : originalFileName.substring(0, dotIndex);
        String extension = (dotIndex == -1) ? "" : originalFileName.substring(dotIndex);
        return String.format("%s_%d%s", baseName, chunkIndex, extension);
    }
}
