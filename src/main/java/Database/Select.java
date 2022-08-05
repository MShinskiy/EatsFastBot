package Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Select {
    public static List<String> selectAllOrders(Connection herokuConn){
        ArrayList<String> orders = new ArrayList<>();
        String selectQuery =
                "SELECT order_id, courier_id, order_text, price, status, insert_time " +
                "FROM orders;";
        try (Statement stmt = herokuConn.createStatement()) {
            ResultSet rs = stmt.executeQuery(selectQuery);
            while(rs.next()){
                //build message
                String order = "Order id: " + rs.getLong("order_id") + "\n" +
                        "Courier id: " + rs.getLong("courier_id") + "\n" +
                        "Order: \n" +
                        rs.getString("order_text") + "\n" +
                        "Price: " + rs.getInt("price") + "\n" +
                        "Status: " + rs.getInt("status") + "\n" +
                        "Time: " + rs.getTimestamp("insert_time");

                //add to list of orders
                orders.add(order);
            }
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }
        return orders;
    }

    public static boolean isIdInOrderTable(Connection herokuConn, long id){
        String selectQuery =
                "SELECT * " +
                "FROM orders " +
                "WHERE order_id = " + id + ";";
        try (Statement stmt = herokuConn.createStatement()) {
            ResultSet rs = stmt.executeQuery(selectQuery);
            return rs.next();
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }
        return false;
    }

    public static boolean isInCourierTable(Connection herokuConn, long chat_id) {
        String selectQuery =
                "SELECT * " +
                "FROM couriers " +
                "WHERE chat_id = " + chat_id + ";";
        try(Statement stmt = herokuConn.createStatement()) {
            ResultSet rs = stmt.executeQuery(selectQuery);
            return rs.next();
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }
        return false;
    }
}