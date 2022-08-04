package Database;

import org.apache.commons.dbcp2.BasicDataSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class HerokuConnection {
    public static BasicDataSource connect() throws URISyntaxException, SQLException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";
        BasicDataSource connectionPool = new BasicDataSource();

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        connectionPool.setUsername(username);
        connectionPool.setPassword(password);
        connectionPool.setDriverClassName("org.postgresql.Driver");
        connectionPool.setUrl(dbUrl);
        connectionPool.setInitialSize(3);
        connectionPool.setMaxTotal(15);
        return connectionPool;
    }
}
