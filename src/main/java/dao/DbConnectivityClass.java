package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Person;
import service.MyLogger;

import java.sql.*;
import java.util.regex.Pattern;

public class DbConnectivityClass {
    final static String DB_NAME = "CSC311sherrcserver2";
    MyLogger lg = new MyLogger();
    final static String SQL_SERVER_URL = "jdbc:mysql://csc311sherrcserver.mysql.database.azure.com"; // Update this server name
    final static String DB_URL = "jdbc:mysql://csc311sherrcserver.mysql.database.azure.com/" + DB_NAME; // Update this database name
    final static String USERNAME = "csc311admin"; // Update this username
    final static String PASSWORD = "Farmingdale311"; // Update this password

    private final ObservableList<Person> data = FXCollections.observableArrayList();

    //Regex patterns for validation
    private static final String USERNAME_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";

    private static final Pattern usernamePattern = Pattern.compile(USERNAME_REGEX);
    private static final Pattern passwordPattern = Pattern.compile(PASSWORD_REGEX);

    public ObservableList<Person> getData() {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                lg.makeLog("No data found in 'users' table.");
            }
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                String major = resultSet.getString("major");
                String email = resultSet.getString("email");
                String imageURL = resultSet.getString("imageURL");
                data.add(new Person(id, first_name, last_name, department, major, email, imageURL));
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    //Method to insert a new user account with regex validation
    public void insertUserAccount(String username, String password) {
        connectToDatabase();

        //Validate username
        if (!usernamePattern.matcher(username).matches()) {
            lg.makeLog("Invalid username format: " + username);
            return;
        }

        //Validate password
        if (!passwordPattern.matcher(password).matches()) {
            lg.makeLog("Password does not meet complexity requirements.");
            return;
        }

        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "INSERT INTO user_accounts (username, password) VALUES (?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            int row = preparedStatement.executeUpdate();
            if (row > 0) {
                lg.makeLog("A new user account was inserted successfully.");
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Method to authenticate a user account
    public boolean authenticateUser(String username, String password) {
        connectToDatabase();
        boolean isAuthenticated = false;
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM user_accounts WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                isAuthenticated = true;
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isAuthenticated;
    }

    //Method to check if a username already exists
    public boolean emailExists(String username) {
        connectToDatabase();
        boolean exists = false;
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT COUNT(*) FROM user_accounts WHERE username = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                exists = resultSet.getInt(1) > 0;
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
    }

    public boolean connectToDatabase() {
        boolean hasRegisteredUsers = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // First, connect to MYSQL server and create the database if not created
            Connection conn = DriverManager.getConnection(SQL_SERVER_URL, USERNAME, PASSWORD);
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME + "");
            statement.close();
            conn.close();

            // Second, connect to the database and create the tables if not created
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            statement = conn.createStatement();

            // Create 'users' table if it doesn't exist
            String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT(10) NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                    "first_name VARCHAR(200) NOT NULL," +
                    "last_name VARCHAR(200) NOT NULL," +
                    "department VARCHAR(200)," +
                    "major VARCHAR(200)," +
                    "email VARCHAR(200) NOT NULL UNIQUE," +
                    "imageURL VARCHAR(200))";
            statement.executeUpdate(sqlUsers);

            //Create user_accounts table if it doesn't exist (this is what im using to store user names and passwords)
            String sqlUserAccounts = "CREATE TABLE IF NOT EXISTS user_accounts (" +
                    "id INT(10) NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                    "username VARCHAR(200) NOT NULL UNIQUE," +
                    "password VARCHAR(200) NOT NULL)";
            statement.executeUpdate(sqlUserAccounts);

            //Check if we have users in the 'users' table
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");

            if (resultSet.next()) {
                int numUsers = resultSet.getInt(1);
                if (numUsers > 0) {
                    hasRegisteredUsers = true;
                }
            }

            //Check if we have user accounts in user_accounts table
            ResultSet resultSetAccounts = statement.executeQuery("SELECT COUNT(*) FROM user_accounts");

            if (resultSetAccounts.next()) {
                int numAccounts = resultSetAccounts.getInt(1);
                if (numAccounts > 0) {
                    hasRegisteredUsers = true;
                }
            }

            statement.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hasRegisteredUsers;
    }

    public ObservableList<Person> queryUserByLastName(String lastName) {
        connectToDatabase();
        ObservableList<Person> searchResults = FXCollections.observableArrayList();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users WHERE last_name LIKE ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, "%" + lastName + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name_db = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                String major = resultSet.getString("major");
                String email = resultSet.getString("email");
                String imageURL = resultSet.getString("imageURL");
                searchResults.add(new Person(id, first_name, last_name_db, department, major, email, imageURL));
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return searchResults;
    }

    public ObservableList<Person> listAllUsers() {
        return getData();
    }

    public void insertUser(Person person) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "INSERT INTO users (first_name, last_name, department, major, email, imageURL) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, person.getFirstName());
            preparedStatement.setString(2, person.getLastName());
            preparedStatement.setString(3, person.getDepartment());
            preparedStatement.setString(4, person.getMajor());
            preparedStatement.setString(5, person.getEmail());
            preparedStatement.setString(6, person.getImageURL());
            int row = preparedStatement.executeUpdate();
            if (row > 0) {
                lg.makeLog("A new user was inserted successfully.");
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void editUser(int id, Person person) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "UPDATE users SET first_name = ?, last_name = ?, department = ?, major = ?, email = ?, imageURL = ? WHERE id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, person.getFirstName());
            preparedStatement.setString(2, person.getLastName());
            preparedStatement.setString(3, person.getDepartment());
            preparedStatement.setString(4, person.getMajor());
            preparedStatement.setString(5, person.getEmail());
            preparedStatement.setString(6, person.getImageURL());
            preparedStatement.setInt(7, id);
            int row = preparedStatement.executeUpdate();
            if (row > 0) {
                lg.makeLog("User with ID " + id + " was updated successfully.");
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteRecord(Person person) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "DELETE FROM users WHERE id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, person.getId());
            int row = preparedStatement.executeUpdate();
            if (row > 0) {
                lg.makeLog("User with ID " + person.getId() + " was deleted successfully.");
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Method to retrieve id from database where it is auto-incremented.
    public int retrieveId(Person p) {
        connectToDatabase();
        int id = -1;
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT id FROM users WHERE email=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, p.getEmail());

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("id");
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }
}
