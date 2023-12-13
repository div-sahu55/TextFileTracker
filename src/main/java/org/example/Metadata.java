package org.example;

import java.sql.Timestamp;
public class Metadata {
    private final String name;
    private final String id;
    private final String dirPath;
    private final long size;
    private final Timestamp lastModified;
    private final String status;
    public Metadata(String id, String name, String path, long size, Timestamp lastModified, String status) {
        this.id = id;
        this.name = name;
        this.dirPath = path;
        this.size = size;
        this.lastModified = lastModified;
        this.status = status;
    }
    public String getID(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public String getPath(){
        return this.dirPath;
    }
    public long getSize(){
        return this.size;
    }
    public Timestamp getLastModified(){
        return this.lastModified;
    }
    public String getStatus(){return this.status;}
    public void display(){
        System.out.println("Id: "+this.id);
        System.out.println("Name: "+this.name);
        System.out.println("path: "+this.dirPath);
        System.out.println("Size: "+this.size+" bytes");
        System.out.println("LM: "+this.lastModified);
        System.out.println("Status: "+this.status);
    }
}

