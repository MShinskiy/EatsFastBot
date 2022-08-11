package Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Select {
    public static List<String> selectAllOrders(Connection herokuConn){
        String selectQuery =
                "SELECT order_id, courier_id, order_text, price, status, insert_time " +
                "FROM orders;";
        return getOrders(herokuConn, selectQuery);
    }

    public static List<String> selectMyOrders(Connection herokuConn, long chatId) {
        String selectQuery =
                "SELECT order_id, courier_id, order_text, price, status, insert_time " +
                "FROM orders o JOIN couriers c ON o.courier_id = c.id " +
                "WHERE c.chat_id = " + chatId + " ;";
        return getOrders(herokuConn, selectQuery);
    }

    public static List<String> selectUncheckedOrders(Connection herokuConn) {
        String selectQuery =
                "SELECT * " +
                "FROM orders " +
                "WHERE courier_id IS NULL";
        return getOrders(herokuConn, selectQuery);
    }

    private static List<String> getOrders(Connection herokuConn, String selectQuery) {
        List<String> orders = new ArrayList<>();
        try (Statement stmt = herokuConn.createStatement()) {
            ResultSet rs = stmt.executeQuery(selectQuery);
            while(rs.next()) {
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

    public static List<Long> selectAllCouriers (Connection herokuConn) {
        String selectQuery =
                "SELECT chat_id " +
                "FROM couriers;";
        List<Long> ids = new ArrayList<>();
        try (Statement stmt = herokuConn.createStatement()){
            ResultSet rs = stmt.executeQuery(selectQuery);
            while(rs.next())
                ids.add(rs.getLong(1));

        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }
        return ids;
    }

    public static boolean isOrderCourierAssigned(Connection herokuConn, long id) {
        String selectQuery =
                "SELECT courier_id " +
                "FROM orders " +
                "WHERE order_id = " + id + ";";

        try (Statement stmt = herokuConn.createStatement()) {
            ResultSet rs = stmt.executeQuery(selectQuery);
            long courierId = 0;
            if(rs.next())
                courierId = rs.getLong(1);
            return courierId > 0;
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }
        return false;
    }

    public static boolean isIdInOrderTable(Connection herokuConn, long id){
        String selectQuery =
                "SELECT * " +
                "FROM orders " +
                "WHERE order_id = " + id + ";";
        return executeIsInTableSelectQuery(herokuConn, selectQuery);
    }

    public static boolean isInCourierTable(Connection herokuConn, long chat_id) {
        String selectQuery =
                "SELECT * " +
                "FROM couriers " +
                "WHERE chat_id = " + chat_id + ";";
        return executeIsInTableSelectQuery(herokuConn, selectQuery);
    }

    private static boolean executeIsInTableSelectQuery(Connection herokuConn, String selectQuery) {
        try (Statement stmt = herokuConn.createStatement()) {
            ResultSet rs = stmt.executeQuery(selectQuery);
            return rs.next();
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }
        return false;
    }

    public static int selectOrderPrice(Connection herokuConn, long id) {
        String selectQuery =
                "SELECT price " +
                "FROM orders " +
                "WHERE order_id = " + id + ";";
        int price = -1;
        try (Statement stmt = herokuConn.createStatement()){
            ResultSet rs = stmt.executeQuery(selectQuery);
            if(rs.next())
                price = rs.getInt(1);
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }
        return price;
    }
}
