package org.example;

import java.sql.*;
import java.util.ArrayList;

public class DbManager {
    private final String username = "postgres";
    private final String password = "divsahu55";
    private final String dbName = "test";
    public Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/"+dbName, this.username, this.password);
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC driver not found!");
        } catch (SQLException e) {
            System.out.println("Connection failed!");
        }
        return conn;
    }
    public void closeConnection(Connection conn) {
        try {
            conn.close();
        } catch (Exception e) {
            System.out.println("Error occurred in closing connection");
        }
    }
    public void createTable(){
        try {
            Connection conn = getConnection();
            String sqlCommand = "CREATE TABLE IF NOT EXISTS " + TableSchema.TABLE_NAME + " ( " +
                    TableSchema.ID + " TEXT PRIMARY KEY, " +
                    TableSchema.FILE_NAME + " TEXT NOT NULL, " +
                    TableSchema.DIR_PATH + " TEXT NOT NULL, " +
                    TableSchema.SIZE + " BIGINT NOT NULL, " +
                    TableSchema.LAST_MODIFIED_TIME + " TIMESTAMP NOT NULL, " +
                    TableSchema.STATUS + " TEXT NOT NULL )";
            PreparedStatement preparedStatement = conn.prepareStatement(sqlCommand);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            closeConnection(conn);
        }
        catch (Exception e){
            System.out.println("Unable to create table in database.");
            throw new RuntimeException(e);
        }
    }
    public synchronized void insertNewRow(Metadata metadata){
        Connection conn = getConnection();
        try{
            String sqlCommand = "INSERT INTO " + TableSchema.TABLE_NAME + " VALUES(?,?,?,?,?,?) " +"ON CONFLICT (" + TableSchema.ID + ") DO NOTHING";
            PreparedStatement preparedStatement = conn.prepareStatement(sqlCommand);
            preparedStatement.setString(1, metadata.getID());
            preparedStatement.setString(2, metadata.getName());
            preparedStatement.setString(3, metadata.getPath());
            preparedStatement.setLong(4, metadata.getSize());
            preparedStatement.setTimestamp(5, metadata.getLastModified());
            preparedStatement.setString(6, metadata.getStatus());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            closeConnection(conn);
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
            System.out.println("Unable to insert Data!");
        }
    }
    public boolean dirAlreadyExists(String dirPath) throws SQLException{
        Connection conn = getConnection();
        String sql = "SELECT EXISTS (SELECT 1 FROM "+TableSchema.TABLE_NAME+" WHERE "+TableSchema.DIR_PATH +"= ?)";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, dirPath);
        ResultSet resultSet = preparedStatement.executeQuery();
        boolean exists = false;
        if(resultSet.next()){
            exists = resultSet.getBoolean(1);
        }
        resultSet.close();
        closeConnection(conn);
        return exists;
    }
    public ArrayList<String> getColumndata(String colName) throws SQLException{
        Connection conn = getConnection();
        ArrayList<String> directoryPaths = new ArrayList<>();
        String sql = "SELECT DISTINCT "+colName+" FROM "+TableSchema.TABLE_NAME;
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            directoryPaths.add(resultSet.getString(colName));
        }
        resultSet.close();
        preparedStatement.close();
        closeConnection(conn);
        return directoryPaths;
    }
    public ArrayList<Metadata> getAllRows(){
        ArrayList<Metadata> allRows = new ArrayList<>();
        try {
            Connection conn = getConnection();
            String sqlCommand = "SELECT * FROM "+TableSchema.TABLE_NAME;
            PreparedStatement preparedStatement = conn.prepareStatement(sqlCommand);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                allRows.add(new Metadata(resultSet.getString(TableSchema.ID), resultSet.getString(TableSchema.FILE_NAME), resultSet.getString(TableSchema.DIR_PATH), resultSet.getLong(TableSchema.SIZE), resultSet.getTimestamp(TableSchema.LAST_MODIFIED_TIME), resultSet.getString(TableSchema.STATUS)));
            }
            resultSet.close();
            preparedStatement.close();
            closeConnection(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return allRows;
    }
}
