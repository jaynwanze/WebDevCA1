import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class UserLoginServlet extends HttpServlet {

    // Method to get user login details from the front end
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Gets users input from https request and initialize to variables
        String username = request.getParameter("username").toLowerCase();
        String password = request.getParameter("password");

        // Declaring/intializing object called connection from the Connection class
        // provided by mysql-connector-jar
        Connection connection = null;
        // Connection to database
        try {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/loyaltypoints?serverTimezone=UTC", "root", "root");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Declaring/Initializing functionality to send reponse to user
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        boolean notBlankInput = false;
        boolean passwordCorrectLength = false;
        boolean usernameExists = false;
        boolean passwordMatches = false;

        // Check if user inputs is blank
        if (!(username.isBlank()) && !(username.matches(".*\\s.*")) && !(password.isBlank())
                && !(password.matches(".*\\s.*"))) {
            notBlankInput = true;
            // Check if password length is within range
            if (password.length() >= 8 && password.length() <= 20) {
                passwordCorrectLength = true;

                PreparedStatement checkUserLoginDetails = null;
                ResultSet rs = null;
                try {
                    // Prepared statement to check to if user exists
                    checkUserLoginDetails = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
                    checkUserLoginDetails.setString(1, username);
                    // Executes sql query tocheck if username already exists /stores resutls within
                    // rs
                    rs = checkUserLoginDetails
                            .executeQuery();
                } catch (SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                try {
                    // Checks if resultset is returned
                    if (rs.next()) {
                        String usernameDB = rs.getString(1);// Get username within database as string
                        // Check if username exists within database
                        if (usernameDB != null && username.equals(usernameDB)) {
                            usernameExists = true; // set boolean to true if username exists
                            String passwordDB = rs.getString(2);// Gets password within database as string
                            // If password matches the password within database
                            if (passwordDB != null && password.equals(passwordDB)) {
                                passwordMatches = true; // set boolean to true if password matches
                            }
                        }
                        // Close statement / resultset
                        rs.close();
                        checkUserLoginDetails.close();
                    }
                    // If username exists within database
                    if (usernameExists == true) {

                        // If password matches/login was sucessful - send reponse/redirect to loyalty
                        // points homepage
                        if (passwordMatches == true) {
                            out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                                    + "<body> <h1> Welcome: " + username
                                    + "<br>You have Been Succesfully Logged in!</h1><h3>Now Redirecting You To The LoyaltyPointsApp Homepage - Please Wait a Few Seconds or Press Button To Go To The LoyaltyPointsApp Homepage...<br>"
                                    + "<br><input type='button' value='Homepage' onclick=\"window.location.href='loyaltypoints.html'\"/></h3> <script>setTimeout(function () {window.location.href = 'loyaltypoints.html';}, 8000);</script></body></html>");
                            // Setting username as an attritube that be requested by LoyaltyPointsServlet
                            HttpSession session = request.getSession();
                            session.setAttribute("username", username);
                        }
                        // If password does not match/login was not sucessful - send reponse/redirect
                        // back to login page
                        // homepage
                        else {
                            out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                                    + "<body> <h1>Login Was Not Sucessful - Password Was Incorrect!<br>Please Try Login Again!</h1><h3>Now Redirecting You Back To The Login Page - Please Wait a Few Seconds or Press Button To Return To The Login Page...<br>"
                                    + "<br><input type='button' value='Login Page' onclick=\"window.location.href='login.html'\"/></h3> <script>setTimeout(function () {window.location.href = 'login.html';}, 8000);</script></body></html>");
                        }

                    }
                } catch (SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }

        // If user input field/fields were blank or have whitespaces - send
        // reponse/redirect back to
        // login page
        if (notBlankInput == false)

        {
            out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                    + "<body> <h1>Whitespaces/Blank Input Field or Fields Not Permitted - Please Try Login Again!</h1><h3>Now Redirecting You Back To The Login Page - Please Wait a Few Seconds or Press Button To Return To The Login Page...<br>"
                    + "<br><input type='button' value='Login Page' onclick=\"window.location.href='login.html'\"/></h3> <script>setTimeout(function () {window.location.href = 'login.html';}, 8000);</script></body></html>");
        }
        // Else if password/password confirmation is not within correct length name -
        // send reponse/redirect back to registration page
        else if (passwordCorrectLength == false) {
            out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                    + "<body> <h1>Password/Password Confirmation Must Be Minimum 8 Characters/Maxmium 20 Characters<br>Please Try Login Again!</h1><h3>Now Redirecting You Back To The Login Page - Please Wait a Few Seconds or Press Button To Return To The Login Page...<br>"
                    + "<br><input type='button' value='Login Page' onclick=\"window.location.href='login.html'\"/></h3> <script>setTimeout(function () {window.location.href = 'login.html';}, 8000);</script></body></html>");
        }
        // Else if username doesnt exist within database -
        // send reponse/redirect back to login page
        else if (usernameExists == false) {
            out.println("<html><head><title>LoyaltyAppWebsite </title></head>" + "<body> <h1>Username: " + username
                    + "<br>Doesn't Exist/Not A User Of The LoyaltyPointApp Website - Please Try Login Again!</h1><h3>Now Redirecting You Back To The Login Page - Please Wait a Few Seconds or Press Button To Return To The Login Page...<br>"
                    + "<br><input type='button' value='Login Page' onclick=\"window.location.href='login.html'\"/></h3> <script>setTimeout(function () {window.location.href = 'login.html';}, 8000);</script></body></html>");
        }
    }
}