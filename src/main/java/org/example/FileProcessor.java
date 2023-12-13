package org.example;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class FileProcessor {
    private DbManager dbManager;
    private String dirPath;
    private ArrayList<Metadata> filesList;
    private FileWatcher fileWatcher;
    ExecutorService executorService;
    public FileProcessor(){}
    public FileProcessor(String dirPath) throws SQLException{
        filesList = new ArrayList<>();
        this.dirPath = dirPath;
        dbManager = new DbManager();
        dbManager.createTable();
        fileWatcher = new FileWatcher(false);
    }
    public void processFiles() throws SQLException {
        if(dbManager.dirAlreadyExists(dirPath)){
            System.out.println("Directory already exists!");
        }
        else {
            try {
                processFilesConcurrently();
                fileWatcher.addPathForTracking(dirPath);
                fileWatcher.track();
            }catch (IOException e){
                System.out.println("Invalid path, please try again.");
            }
        }
    }
    private void processFilesConcurrently() throws IOException {
        ArrayList<Metadata> filesList = new ArrayList<>();
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(dirPath), "*.txt");
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        for (Path filePath : directoryStream) {
            Metadata metadata = readBasicMetadata(filePath);
            filesList.add(metadata);
        }
        directoryStream.close();
        // Execute dbManager.insertNewRow() in a thread pool
        for (Metadata metadata : filesList) {
            executorService.execute(() -> {
                try {
                    dbManager.insertNewRow(metadata);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        executorService.shutdown();
    }
    public Metadata readBasicMetadata(Path filePath) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
        long millis = attributes.lastModifiedTime().toMillis();
        return new Metadata(
                filePath.getFileName()+Long.toString(attributes.lastModifiedTime().toMillis()),
                filePath.toFile().getName(),
                filePath.toFile().getParent(),
                attributes.size(),
                new Timestamp(millis),
                "READ"
        );
    }
}
