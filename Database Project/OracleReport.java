import java.util.Scanner;
import java.sql.*;
import java.util.Properties;

/**
 * Simple Java Program to connect Oracle database by using Oracle JDBC 
thin driver
 * Make sure you have Oracle JDBC thin driver in your classpath before 
running this program
 * @author
 */
public class OracleReport {

    // get connection to DB with username and password info
    public static Connection getConnection(String u, String p) throws SQLException {
    
        Connection conn = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", u);
        connectionProps.put("password", p);

        conn = DriverManager.getConnection("jdbc:orcl:jasonbi7;create=true", connectionProps);

        System.out.println("Connected to database");
        return conn;
    }

    // print exceptions for errors
    public static void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                if (ignoreSQLException(((SQLException)e).getSQLState()) == false) {
                    e.printStackTrace(System.err);
                    System.err.println("SQLState: " + ((SQLException)e).getSQLState());
                    System.err.println("Error Code: " + ((SQLException)e).getErrorCode());
                    System.err.println("Message: " + e.getMessage());
                    Throwable t = ex.getCause();
                    while (t != null) {
                        System.out.println("Cause: " + t);
                        t = t.getCause();
                    } // end of while
                } // end of if
            } // end of if
        } // end of for
    } // end of method printSQLException

    // ignore certain sql exceptions
    public static boolean ignoreSQLException(String sqlState) {
    
        if (sqlState == null) {
            System.out.println("The SQL state is not defined!");
            return false;
        }
    
        // X0Y32: Jar file already exists in schema
        if (sqlState.equalsIgnoreCase("X0Y32"))
            return true;
    
        // 42Y55: Table already exists in schema
        if (sqlState.equalsIgnoreCase("42Y55"))
            return true;
    
        return false;
    } // end of method ignoreSQLException

    public static void getPatientReport(Connection conn, int pNo) throws SQLException {
        
        String pName = null;
        int ccID = 0;
        String ccName = null;
        int nicID = 0;
        String nicName = null;
        int tid = 0;
        String tName = null;
        int phid = 0;
        Timestamp tDate = null;
        String query = null;

        // get patient info
        Statement stmt = null;
        query = "select name, care_centre_id from Patients_t where pid = " + pNo;
        try {        
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                pName = rs.getString("name");
                ccID = rs.getInt("care_centre_id");
            }
        } catch (SQLException e) {
            printSQLException(e);
        } finally {
            if (stmt != null) { stmt.close(); }
        }  // end of try/catch/finally
        
        // get care centre info
        if (ccID != 0) {
            stmt = null;
            query = "select name, nurse_charge_id from Care_centres_t where cid = " + ccID;
            try {        
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    ccName = rs.getString("name");
                    nicID = rs.getInt("nurse_charge_id");
                }
            } catch (SQLException e) {
                printSQLException(e);
            } finally {
                if (stmt != null) { stmt.close(); }
            } // end of try/catch/finally
        } // end of if
        
        // get nurse in charge info
        if (nicID != 0) {
            stmt = null;
            query = "select name from Nurses_t where nid = " + nicID;
            try {        
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    nicName = rs.getString("name");
                }
            } catch (SQLException e) {
                printSQLException(e);
            } finally {
                if (stmt != null) { stmt.close(); }
            } // end of try/catch/finally
        } // end of if
        
        // print report information so far
        System.out.println("Patient Number: " + pNo);
        System.out.println("Patient Name: " + pName);
        System.out.println("Care Centre Name: " + ccName);
        System.out.println("Name of Nurse-in-Charge: " + nicName);
        System.out.printf("%-20s\t%20s\t%20s\t%20s\n" , "Treatment ID", "Treatment Name", "Physician ID", "Date");
        
        // get treatment info
        try {        
            stmt = null;
            query = "select tid, physician_id, treatment_name, treatment_date from "
                    + "Treatments_t where patient_id = " + pNo;
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                tid = rs.getInt("tid");
                tName = rs.getString("treatment_name");
                phid = rs.getInt("physician_id");
                tDate = rs.getTimestamp("treatment_date");
                if (tid != 0) {
                    System.out.printf("%-20s\t%20s\t%20s\t%20s\n" , tid, tName, phid, tDate);
                } else {
                    System.out.print("No treatments on record.\n");
                } // end of if
            } // end of while
        } catch (SQLException e) {
            printSQLException(e);
        } finally {
            if (stmt != null) { stmt.close(); }
        } // end of try/catch/finally
        
        System.out.println("\nEnd of report.");
        
    } // end of getPatientReport
    
    public static void main(String args[]) throws SQLException {    
        
        String userName;
        String password;
        int patientNo;
                
        Scanner in = new Scanner(System.in);
        
        // get connection details for DB access
        System.out.println("Connect to database:\nPlease enter username: ");
        userName = in.nextLine();
        System.out.println("\nPlease enter password: ");
        password = in.nextLine();
        
        // attempt DB connection, close when finished
        Connection conn = null;
        try {
            conn = getConnection(userName, password);  // attempt DB connection
            
            // get patient number information
            System.out.println("\nPlease enter patient number: ");
            patientNo = in.nextInt();
            in.close();
            
            getPatientReport(conn, patientNo);  // display report
        } catch (SQLException e) {
                printSQLException(e);
        } finally {
            if (conn != null) { conn.close(); } // close connection
        } // end of try/catch/finally
    } // end of main
} // end of class OracleReport
