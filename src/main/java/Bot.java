import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;

public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "EatsFastBot";
    }

    @Override
    public String getBotToken() {
        return "5143426005:AAGMFGEUBEUkb96MwLSvtraZRsw8B2zIjR0";
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            switch (text) {
                case "/start":
                    startupMessage(update);
                    break;
                default:
                    echo(update);
            }
        }

    }

    public void startupMessage(Update update) {
        //build message
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("Welcome!");

        KeyboardButton button = new KeyboardButton("Button");
        ReplyKeyboardMarkup startMessageRKM = new ReplyKeyboardMarkup();

        message.setReplyMarkup();

        //execute
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void echo(Update update) {
        // We check if the update has a message and the message has text
        SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText(update.getMessage().getText());

        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}
