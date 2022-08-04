import Database.HerokuConnection;
import org.apache.commons.dbcp2.BasicDataSource;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args){
            try {
                BasicDataSource connectionPool = HerokuConnection.connect();
                //instantiate bot
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                //register bot
                botsApi.registerBot(new Bot(connectionPool));
                System.out.println("Bot started...");
            } catch (TelegramApiException | SQLException | URISyntaxException e){
                e.printStackTrace();
            }


    }
}
