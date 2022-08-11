package Database;

import java.sql.*;


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

    public static long insertNewOrder(Connection herokuConn, String orderText) {
        String insertString =
                "INSERT INTO orders (order_text) " +
                "VALUES(?);";
        long key = 0;
        try (PreparedStatement ps =
                     herokuConn.prepareStatement(insertString, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, orderText);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if(keys.next()) {
                    key = keys.getLong(1);
                }
            }
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }

        return key;
    }




}
