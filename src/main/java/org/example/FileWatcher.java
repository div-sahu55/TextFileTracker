package org.example;

import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class FileWatcher {
    private final DbManager dbManager;
    private ArrayList<String> dirPaths;
    private WatchService watchService;
    private final FileProcessor fileProcessor;
    public FileWatcher(Boolean readFromDB) throws SQLException {
        this.dirPaths = new ArrayList<>();
        this.dbManager = new DbManager();
        this.fileProcessor = new FileProcessor();
        if(readFromDB) {
            readPathsFromDatabase();
        }
        initializeWatchService();
    }
    private void initializeWatchService() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create WatchService");
        }
    }

    private void readPathsFromDatabase() throws SQLException {
        ArrayList<String> paths = dbManager.getColumndata(TableSchema.DIR_PATH);
        dirPaths.addAll(paths);
    }
    public void addPathForTracking(String dirPath){
        dirPaths.add(dirPath);
    }
    public void track(){
            for (String dirPath : dirPaths) {
                registerPath(dirPath);
            }
            Thread watchThread = new Thread(() -> {
                try {
                    handleWatchServiceEvents();
                } catch (Exception e) {
                    System.out.println(e.getLocalizedMessage());
                } finally {
                    try{
                        watchService.close();
                    }catch (IOException e){
                        System.out.println("Failed to close WatchService");
                    }
                }
            });
            watchThread.setDaemon(true);
            watchThread.start();
        }
        private void registerPath(String dirPath){
            try {
                Path path = Paths.get(dirPath);
                path.register(watchService,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_CREATE);
            }catch (IOException e){
                System.out.println("Failed to register path with Watch service");
            }
        }
        private void handleWatchServiceEvents() throws IOException, InterruptedException {
            while (true) {
                WatchKey key = watchService.take(); // Wait for events
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path modifiedFileRelative = ev.context();
                        Path directoryPath = (Path) key.watchable();
                        Path modifiedFilePath = directoryPath.resolve(modifiedFileRelative);
                        dbManager.insertNewRow(fileProcessor.readBasicMetadata(modifiedFilePath));
                    } else if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path createdFile = (Path) event.context();
                        if (createdFile.toString().endsWith(".txt")) {
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path modifiedFileRelative = ev.context();
                            Path directoryPath = (Path) key.watchable();
                            Path modifiedFilePath = directoryPath.resolve(modifiedFileRelative);
                            dbManager.insertNewRow(fileProcessor.readBasicMetadata(modifiedFilePath));
                        }
                    }
                }
                key.reset(); // Reset the key to receive further events
            }
        }
}
