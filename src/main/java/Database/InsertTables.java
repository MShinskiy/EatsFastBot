package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertTables {

    public static boolean insertCourier(Connection conn) {
        return false;
    }

    public static boolean insertNewOrder(Connection herokuConn, String orderText) throws SQLException {
        String insertString = "INSERT INTO orders (order_text) VALUES(?);";
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
