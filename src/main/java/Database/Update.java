package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Update {
    public static boolean updatePrice(Connection herokuConn, int id, int price){
        String updateString = "UPDATE orders " +
                "SET price = ? " +
                "WHERE order_id = ? ;";

        try (PreparedStatement ps = herokuConn.prepareStatement(updateString)){
            ps.setInt(1, price);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }
            return false;
    }

    public static boolean updateOrderCourier(Connection herokuConn, long orderId, long chatId ) {
        String updateString =
                "UPDATE orders " +
                "SET courier_id = (" +
                        "SELECT id " +
                        "FROM couriers " +
                        "WHERE chat_id = ?) " +
                "WHERE order_id = ? ;";

        try (PreparedStatement ps = herokuConn.prepareStatement(updateString)){
            ps.setLong(1, chatId);
            ps.setLong(2, orderId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }
        return false;
    }

    public static boolean updateOrderStatus(Connection herokuConn, long orderId, int status) {
        String updateString =
                "UPDATE orders " +
                "SET status = ? " +
                "WHERE order_id = ? ;";

        try (PreparedStatement ps = herokuConn.prepareStatement(updateString)){
            ps.setInt(1, status);
            ps.setLong(2, orderId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }
        return false;
    }
}
