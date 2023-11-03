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

public class SpendPointsActionServlet extends HttpServlet {

    // Method to get user details from the front end
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Get username from the current http session
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");

        // Gets users input from https request and initialize to variables
        String strPointsToSpend = request.getParameter("points_to_spend");

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
        boolean balanceGreaterThanZero = false;
        boolean balanceMoreThanSpendPoints = false;
        boolean rsEmpty = false;
        boolean actionSuccessful = false;

        // Input Validation
        if (!(strPointsToSpend.isBlank()) && !(strPointsToSpend.matches(".*\\s.*"))
                && strPointsToSpend.matches("\\d+")) {
            validInput = true;// set validInput boolean to true
            int pointsToSpend = Integer.parseInt(strPointsToSpend);// parse valid input to integer

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

            // Create Prepared statement to remove points/update new balance within database
            // for user
            PreparedStatement removePointsAction = null;
            try {
                // If result set is not empty
                if (rs.next()) {
                    // Get points balance from database
                    int pointsDB = rs.getInt(3);
                    // If balance greater than zero
                    if (pointsDB > 0) {
                        balanceGreaterThanZero = true;// set boolean true
                        // If users has enough points in their balance to spend
                        if (pointsDB >= pointsToSpend) {
                            balanceMoreThanSpendPoints = true;
                            // Code to spend specified amount from balance and create new balance
                            int newBalance = rs.getInt(3) - pointsToSpend;
                            // Execute sql prepared statement to spend points/update new balance
                            try {
                                removePointsAction = connection
                                        .prepareStatement("UPDATE users SET points = ? WHERE username = ?");
                                removePointsAction.setInt(1, newBalance);
                                removePointsAction.setString(2, username);
                                int rowsUpdated = removePointsAction.executeUpdate();
                                // If spend points action was sucessful
                                if (rowsUpdated > 0) {
                                    actionSuccessful = true;// set boolean to true
                                }
                                // Close statement
                                removePointsAction.close();
                            } catch (SQLException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                        }
                    }
                }
                // If resultset is empty
                else {
                    rsEmpty = true;// set boolean to true
                }

            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            try {
                // If resultset is returned
                if (!(rsEmpty)) {
                    // If balance is greater than zero
                    if (balanceGreaterThanZero && balanceMoreThanSpendPoints) {
                        // If action is successfull
                        if (actionSuccessful) {
                            // Rerun prepared statement to to access updated user details
                            rs = accessUserDetails.executeQuery();
                            if (rs.next()) {
                                // Get updated user details from database
                                String usernameDB = rs.getString(1);
                                int pointsDB = rs.getInt(3);
                                // If action was sucessfull - send reponse to user with related updated
                                // information
                                out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                                        + "<body> <h1> Hi: " + usernameDB + "! - You Have Just Spent: " + pointsToSpend
                                        + " Loyalty Points From Your Balance<br>New Loyalty Points Balance: " + pointsDB
                                        + "</h1><h3>Press Button To Go Back To The LoyaltyPointsApp Homepage...<br>"
                                        + "<br><input type='button' value='Homepage' onclick=\"window.location.href='loyaltypoints.html'\"/></h3> </body></html>");
                            }

                        } else {
                            // If action wasnt executed - send reponse to user
                            out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                                    + "<body> <h1> Error!!! - Action Incomplete/Unable To Spend Points From Your Balance!<br>Sorry For Any Inconvenience..."
                                    + "</h1><h3>Press Button To Go Back To The LoyaltyPointsApp Homepage...<br>"
                                    + "<br><input type='button' value='Homepage' onclick=\"window.location.href='loyaltypoints.html'\"/></h3>"
                                    + "<script>setTimeout(function () {window.location.href = 'loyaltypoints.html';}, 8000);</script></body></html>");
                        }
                    } else {
                        // Get updated user points balance from database
                        int pointsDB = rs.getInt(3);

                        if (!(balanceGreaterThanZero)) {
                            // If balance was less than zero - send reponse to user
                            out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                                    + "<body> <h1> Error!!! -  Action Incomplete As Your Balance is Zero/Less Than Zero!<br>Current Loyalty Points Balance: "
                                    + pointsDB + "<br>Please Add Points To Spend!"
                                    + "</h1><h3>Press Button To Go Back To The LoyaltyPointsApp Homepage...<br>"
                                    + "<br><input type='button' value='Homepage' onclick=\"window.location.href='loyaltypoints.html'\"/></h3>"
                                    + "</body></html>");
                        } else {
                            // If user doesnt have enough points to spend - send reponse to user
                            out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                                    + "<body> <h1> Error!!! - Action Incomplete!<br>You Do Not Have A Sufficient Amount Within Balance To Spend "
                                    + pointsToSpend + " points<br>Current Loyalty Points Balance: " + pointsDB
                                    + "<br>Please Add Points To Spend!"
                                    + "</h1><h3>Press Button To Go Back To The LoyaltyPointsApp Homepage...<br>"
                                    + "<br><input type='button' value='Homepage' onclick=\"window.location.href='loyaltypoints.html'\"/></h3>"
                                    +"</body></html>");

                        }

                    }
                } else {
                    // Error response when resultset returns empty
                    out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                            + "<body> <h1> Error!!! - Action Incomplete as User Details Were Not Found Database<br>Sorry For Any Inconvenience..."
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
                    + "<body> <h1>Only Integer Values Permitted and Whitespaces/Blank Input Field or Fields Not Permitted...<br>Please Try Enter Input Again Again!</h1><h3>Now Redirecting You Back To The Spend Points Page - Please Wait a Few Seconds or Press Button To Return To The Spend Points Page...<br>"
                    + "<br><input type='button' value='Spend Points' onclick=\"window.location.href='spendpoints.html'\"/></h3> <script>setTimeout(function () {window.location.href = 'spendpoints.html';}, 8000);</script></body></html>");

        }

    }

}
