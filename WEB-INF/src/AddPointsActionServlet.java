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

public class AddPointsActionServlet extends HttpServlet {

    // Method to get user details from the front end
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Get username from the current http session
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");

        // Gets users input from https request and initialize to variables
        String strReceiptNumber = request.getParameter("receipt_number");
        String strPointsToAdd = request.getParameter("points_to_add");

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
        boolean validInput = false;
        boolean rsEmpty = false;
        boolean actionSuccessful = false;

        // Input Validation
        if (!(strReceiptNumber.isBlank()) && !(strReceiptNumber.matches(".*\\s.*"))
                && strReceiptNumber.matches("\\d+") && !(strPointsToAdd.isBlank())
                && !(strPointsToAdd.matches(".*\\s.*")) && strPointsToAdd.matches("\\d+")) {
            validInput = true;// set validInput boolean to true
            int pointsToAdd = Integer.parseInt(strPointsToAdd);// parse valid input to integer

            // Create Prepared statement and result to access User details within database
            // for user and store them in a resultset
            PreparedStatement accessUserDetails = null;
            ResultSet rs = null;
            try {
                // Execute sql query to access passed in user details and store within result
                // set
                accessUserDetails = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
                accessUserDetails.setString(1, username);
                rs = accessUserDetails.executeQuery();

            } catch (SQLException e1) {
                e1.printStackTrace();
            }

            // Create Prepared statement to add points/update new balance within database
            // for user
            PreparedStatement addPointsAction = null;
            try {
                if (rs.next()) {
                    // Code to add specified amount to balance and create updated balance
                    int newBalance = rs.getInt(3) + pointsToAdd;

                    // Execute sql prepared statement to add points/update new balance
                    addPointsAction = connection
                            .prepareStatement("UPDATE users SET points = ? WHERE username = ?");
                    addPointsAction.setInt(1, newBalance);
                    addPointsAction.setString(2, username);
                    int rowsUpdated = addPointsAction.executeUpdate();

                    // If add points action was sucessful
                    if (rowsUpdated > 0) {
                        actionSuccessful = true;// set boolean to true 
                    }

                    // Close statement
                    addPointsAction.close();

                    // Else if resultset returns empty
                } else {
                    rsEmpty = true;// set boolean to  true 

                }

            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            try {
                // If result set is returned
                if (!(rsEmpty)) {
                    // If action is successful
                    if (actionSuccessful) {
                        // Run new prepared statement to to access updated user details
                        rs = accessUserDetails.executeQuery();
                        if (rs.next()) {
                            // Get updated user details from database
                            String usernameDB = rs.getString(1);
                            int pointsDB = rs.getInt(3);
                            // If action was sucessful - send reponse to user with related updated
                            // information
                            out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                                    + "<body> <h1> Hi: " + usernameDB + "! - You Have Just Added: " + pointsToAdd
                                    + " Loyalty Points To Your Balance<br>New Loyalty Points Balance: " + pointsDB
                                    + "</h1><h3>Press Button To Go Back To The LoyaltyPointsApp Homepage...<br>"
                                    + "<br><input type='button' value='Homepage' onclick=\"window.location.href='loyaltypoints.html'\"/></h3></body></html>");
                        }

                    } else {
                        // If action wasnt executed - send reponse to user
                        out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                                + "<body> <h1> Error!!! - Action Incomplete/Unable To Add Points To Your Balance!<br>Sorry For Any Inconvenience..."
                                + "</h1><h3>Press Button To Go Back To The LoyaltyPointsApp Homepage...<br>"
                                + "<br><input type='button' value='Homepage' onclick=\"window.location.href='loyaltypoints.html'\"/></h3>"
                                + "<script>setTimeout(function () {window.location.href = 'loyaltypoints.html';}, 8000);</script></body></html>");

                    }
                } else {
                    // Error response when resultset returns empty
                    out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                            + "<body> <h1> Error!!! - Action Incomplete as User Details Were Not Found Database!<br>Sorry For Any Inconvenience..."
                            + "</h1><h3>Press Button To Go Back To The LoyaltyPointsApp Homepage...<br>"
                            + "<br><input type='button' value='Homepage' onclick=\"window.location.href='loyaltypoints.html'\"/></h3>"
                            + "<script>setTimeout(function () {window.location.href = 'loyaltypoints.html';}, 8000);</script></body></html>");
                }
                // Close prepared statement/resultset
                rs.close();
                accessUserDetails.close();

            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }
        // If input was not valid - send reponse/redirect
        if (!(validInput)) {
            out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                    + "<body> <h1>Only Integer Values Permitted and Whitespaces/Blank Input Field or Fields Not Permitted...<br>Please Try Enter Input Again Again!</h1><h3>Now Redirecting You Back To The Add Points Page - Please Wait a Few Seconds or Press Button To Return To The Add Points Page...<br>"
                    + "<br><input type='button' value='Add Points' onclick=\"window.location.href='addpoints.html'\"/></h3> <script>setTimeout(function () {window.location.href = 'addpoints.html';}, 8000);</script></body></html>");

        }

    }

}
