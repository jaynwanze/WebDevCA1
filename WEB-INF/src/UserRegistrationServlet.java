import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserRegistrationServlet extends HttpServlet {

    // Method to get user registration details from the front end
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Gets users input from https request and initialize to variables
        String username = request.getParameter("username").toLowerCase();
        String password = request.getParameter("password");
        String passConfirmation = request.getParameter("passwordConfirmation");
        int points = 100;// Default value of 100

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
        boolean passwordMatches = false;

        // Check if passed in user inputs is blank
        if (!(username.isBlank()) && !(password.isBlank()) && !(passConfirmation.isBlank())) {
            notBlankInput = true;
            // Check if password/password confirmation length is within correct range
            if (password.length() >= 8 && password.length() <= 20 && passConfirmation.length() >= 8
                    && passConfirmation.length() <= 20) {
                passwordCorrectLength = true;
                // Check if password is equal to password confirmation
                if (password.equals(passConfirmation)) {
                    passwordMatches = true;
                    // Create statement to check if username is taken
                    Statement checkIfUsernameIsTaken = null;
                    try {
                        checkIfUsernameIsTaken = connection.createStatement();

                    } catch (SQLException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    // Executes sql query to check if username already exists
                    ResultSet rs = null;
                    boolean usernameTaken = false;
                    try {
                        rs = checkIfUsernameIsTaken
                                .executeQuery("SELECT username FROM users WHERE username = '" + username + "'");
                    } catch (SQLException e1) {
                        // TODO: handle exception
                        e1.printStackTrace();
                    }
                    // Check to see if resultset is returned
                    try {
                        if (rs.next()) {
                            // Get username as string
                            String usernameDB = rs.getString(1);
                            // If username already exists set boolean to true
                            if (usernameDB != null && username.equalsIgnoreCase(usernameDB)) {
                                usernameTaken = true;
                            }
                        }

                    } catch (SQLException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    try {
                        // If username is not taken then execute query to create a new user within
                        // database
                        if (usernameTaken == false) {
                            PreparedStatement createUser = connection.prepareStatement(
                                    "INSERT into users "
                                            + "(username, password, points)" + " VALUES (?, ?, ?)");
                            // Pass in the values as paramaters into sql statement
                            createUser.setString(1, username);
                            createUser.setString(2, password);
                            createUser.setInt(3, points);

                            int rowsUpdated = createUser.executeUpdate();
                            createUser.close();

                            // If user was created - send reponse that user has been created sucessfully
                            // /direct to login page
                            if (rowsUpdated > 0) {
                                out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                                        + "<body> <h1> Welcome: " + username
                                        + "<br>You have Been Succesfully Created as a User!</h1><h3>Now Redirecting You To The Login Page - Please Wait a Few Seconds or Press Button To Go To The Login Page...<br>"
                                        + "<br><input type='button' value='Login Page' onclick=\"window.location.href='login.html'\"/></h3> <script>setTimeout(function () {window.location.href = 'login.html';}, 8000);</script></body></html>");

                            } else {
                                // if user user wasnt created sucessfully - send reponse/redirect back to
                                // registration page
                                out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                                        + "<body> <h1>User Was Not Created Sucessfully - Please Try Register Again!</h1><h3>Now Redirecting You Back To The Registration Page - Please Wait a Few Seconds or Press Button To Return To The Registration Page...<br>"
                                        + "<br><input type='button' value='Registration Page' onclick=\"window.location.href='index.html'\"/></h3> <script>setTimeout(function () {window.location.href = 'index.html';}, 8000);</script></body></html>");

                            }
                        }
                        // If username already in use - send reponse/redirect back to registration page
                        else {
                            out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                                    + "<body> <h1> The Username: " + username
                                    + "<br>Is Already In Use - Please Try Register Again!</h1><h3>Now Redirecting You Back To The Registration Page - Please Wait a Few Seconds or Press Button To Return To The Registration Page...<br>"
                                    + "<br><input type='button' value='Registration Page' onclick=\"window.location.href='index.html'\"/></h3> <script>setTimeout(function () {window.location.href = 'index.html';}, 8000);</script></body></html>");
                        }
                    } catch (SQLException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                }
            }

        }
        // If user input field/fields were blank - send reponse/redirect back to
        // registration page
        if (notBlankInput == false) {
            out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                    + "<body> <h1>Blank Input Field/Fields  - Please Try Register Again!</h1><h3>Now Redirecting You Back To The Registration Page - Please Wait a Few Seconds or Press Button To Return To The Registration Page...<br>"
                    + "<br><input type='button' value='Registration Page' onclick=\"window.location.href='index.html'\"/></h3> <script>setTimeout(function () {window.location.href = 'index.html';}, 8000);</script></body></html>");

        }
        // Else if password/password confirmation is not within correct length name -
        // send reponse/redirect back to registration page
        else if (passwordCorrectLength == false) {
            out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                    + "<body> <h1>Password/Password Confirmation Must Be Minimum 8 Characters/Maxmium 20 Characters<br>Please Try Register Again!</h1><h3>Now Redirecting You Back To The Registration Page - Please Wait a Few Seconds or Press Button To Return To The Registration Page...<br>"
                    + "<br><input type='button' value='Registration Page' onclick=\"window.location.href='index.html'\"/></h3> <script>setTimeout(function () {window.location.href = 'index.html';}, 8000);</script></body></html>");

        }
        // Else if password and password confirmation do not match - send
        // reponse/redirect back to registration page
        else if (passwordMatches == false) {
            out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                    + "<body> <h1> Your Password and Password Confirmation Don't Match - Please Try Register Again!</h1><h3>Now Redirecting You Back To The Registration Page - Please Wait a Few Seconds or Press Button To Return To The Registration Page...<br>"
                    + "<br><input type='button' value='Registration Page' onclick=\"window.location.href='index.html'\"/></h3> <script>setTimeout(function () {window.location.href = 'index.html';}, 8000);</script></body></html>");
        }

    }

}
