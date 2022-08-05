package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class Insert {

    public static boolean insertCourier(Connection herokuConn, String name, String username, String privilege, long chat_id) {
        String insertString =
                "INSERT INTO couriers (name, username, privilege, chat_id) " +
                "VALUES(?, ?, ?, ?);";
        int row = -1;
        try(PreparedStatement ps = herokuConn.prepareStatement(insertString)){
            ps.setString(1, name);
            ps.setString(2, username);
            ps.setString(3, privilege);
            ps.setLong(4, chat_id);
            row = ps.executeUpdate();
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }
        return row > -1;
    }

    public static boolean insertNewOrder(Connection herokuConn, String orderText) {
        String insertString =
                "INSERT INTO orders (order_text) " +
                "VALUES(?);";
        int row = -1;
        try (PreparedStatement ps = herokuConn.prepareStatement(insertString)) {

            ps.setString(1, orderText);
            row = ps.executeUpdate();

        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }
        return row > -1;
    }




}
