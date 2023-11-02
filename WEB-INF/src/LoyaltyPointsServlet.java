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

public class LoyaltyPointsServlet extends HttpServlet {

    // Method to get user details from the front end
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Get username from UserLoginServlet
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");
        String action = null;
        // Gets users input from https request and initialize to variables
        if (request.getParameter("showBalance") != null) {
            action = "showBalance";
        }

        else if (request.getParameter("addPoints") != null) {
            action = "addPoints";

        }

        else if (request.getParameter("removePoints") != null) {
            action = "removePoints";
        }

        else if (request.getParameter("spendPoints") != null) {
            action = "spendPoints";
        }

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
        boolean showPointsBalance = false;
        boolean addPoints = false;
        boolean removePoints = false;
        boolean spendPoints = false;

        if (action != null) {
            PreparedStatement accessUserDetails = null;
            ResultSet rs = null;
            try {
                // Prepare statement to access passed in user details
                accessUserDetails = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
                accessUserDetails.setString(1, username);
                // Executes sql query to access passed in user details and store within a
                // resultset
                rs = accessUserDetails
                        .executeQuery();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try {
                try {
                    // Checks if resultset is returned
                    if (rs.next()) {
                        // Perform actions based off the passed in 'action' value
                        if (action.equals("showBalance")) {
                            // Handles "Show Balance" action
                            showPointsBalance = true; // Set spendPoints boolean true
                        } else if (action.equals("addPoints")) {
                            // Handles "Add Points" action
                            addPoints = true;// Set addPoints boolean true
                        } else if (action.equals("removePoints")) {
                            // Handles "Remove Points" action
                            removePoints = true;// Set removePoints boolean true
                        } else if (action.equals("spendPoints")) {
                            // Handles "Spend Points" action
                            spendPoints = true;// Set spendPoints boolean true
                        }

                    }
                } catch (SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                // If action selected was to show loyalty points balance - send reponse to user
                if (showPointsBalance == true) {
                    // Get user details from database
                    String usernameDB = rs.getString(1);
                    int pointsDB = rs.getInt(3);

                    out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                            + "<body> <h1> Hi: " + usernameDB + "! - Here is Your Loyalty Points Balance!"
                            + "<br>Loyalty Points Balance: " + pointsDB
                            + "</h1><h3>Press Button To Go Back To The LoyaltyPointsApp Homepage...<br>"
                            + "<br><input type='button' value='Homepage' onclick=\"window.location.href='loyaltypoints.html'\"/></h3> </body></html>");
                }
                // If action selected was to add loyalty points - send reponse to user
                else if (addPoints == true) {

                    response.sendRedirect("addpoints.html");

                }
                // If action selected was to remove loyalty points - send reponse to user
                else if (removePoints == true) {

                    response.sendRedirect("removepoints.html");

                }
                // If action selected was to spend loyalty points - send reponse to user
                else if (spendPoints == true) {

                    response.sendRedirect("spendpoints.html");

                }
                // Else reponse - if user details are not found/ return to homepage
                else {
                    out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                            + "<body> <h1> Error!!! - User Details Are Not Retrievable At This Current Time, Sorry For Any Inconvience..."
                            + "</h1><h3>Press Button To Go Back To The LoyaltyPointsApp Homepage...<br>"
                            + "<br><input type='button' value='Homepage' onclick=\"window.location.href='loyaltypoints.html'\"/></h3>"
                            + "<script>setTimeout(function () {window.location.href = 'loyaltypoints.html';}, 8000);</script></body></html>");

                }
                // Close statement/rs
                rs.close();
                accessUserDetails.close();

            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        } else {
            // print error accessing your request
            out.println("<html><head><title>LoyaltyAppWebsite </title></head>"
                    + "<body> <h1> Error!!! - We Could Not Process Your Action Request, Sorry For Any Inconvience..."
                    + "</h1><h3>Press Button To Go Back To The LoyaltyPointsApp Homepage...<br>"
                    + "<br><input type='button' value='Homepage' onclick=\"window.location.href='loyaltypoints.html'\"/></h3>"
                    + "<script>setTimeout(function () {window.location.href = 'loyaltypoints.html';}, 8000);</script></body></html>");

        }

    }
}
