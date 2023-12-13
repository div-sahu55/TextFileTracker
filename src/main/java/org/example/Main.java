package org.example;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.ArrayList;
public class Main {
    private static Scanner scanner;
    private static DbManager dbManager;
    public static void main(String[] args){
        startTracker();
        dbManager= new DbManager();
        scanner = new Scanner(System.in);
        while(true) {
            System.out.println("Enter 1 to add new directory path "+"\nEnter 2 to display all the transactions "+"\nEnter 3 to exit "+"\nYour input: ");
            String choice = scanner.nextLine();
            switch(choice){
                case "1": {
                    addDir();
                    break;
                }
                case "2": {
                    printAllTransactions();
                    break;
                }
                case "3": {
                    return;
                }
                default: {
                    System.out.println("Invalid Choice!");
                    break;
                }
            }
        }
    }
    public static void addDir(){
        System.out.println("Enter Path of the directory: ");
        String path = scanner.nextLine();
        try {
            FileProcessor fileProcessor = new FileProcessor(path);
            fileProcessor.processFiles();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    public static void printAllTransactions(){
        ArrayList<Metadata> rows = dbManager.getAllRows();
        for(Metadata metadata : rows){
            metadata.display();
            System.out.println("----------------------------------------");
        }
    }
    // Start tracking the existing directories in a new thread:
    public static void startTracker(){
        Thread trackerThread = new Thread(() -> {
            try {
                FileWatcher fileWatcher = new FileWatcher(true);
                fileWatcher.track();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        trackerThread.start();
    }
}