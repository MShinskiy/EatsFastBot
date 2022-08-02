import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Bot extends TelegramLongPollingBot {

    private final ConcurrentHashMap<Integer, Integer> userState = new ConcurrentHashMap<>();

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
                case "Add order":
                    break;
                default:
                    echo(update);
            }
        }

    }

    private void startupMessage(Update update) {
        //build message
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("Welcome!");

        //build keyboard
        KeyboardButton buttonOrder = new KeyboardButton("Add order");
        KeyboardButton buttonViewOrders = new KeyboardButton("View Orders");
        KeyboardRow row = new KeyboardRow();
        row.add(buttonOrder);
        row.add(buttonViewOrders);
        List<KeyboardRow> l = Collections.singletonList(row);
        ReplyKeyboardMarkup startMessageRKM = new ReplyKeyboardMarkup();
        startMessageRKM.setKeyboard(l);
        startMessageRKM.setInputFieldPlaceholder("Placeholder");
        startMessageRKM.setOneTimeKeyboard(true);
        message.setReplyMarkup(startMessageRKM);

        //execute
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void getOrder(Update update) {
        SendMessage message = new SendMessage();
        message.setText("Writing down your order...");

    }

    private void addOrder(Update update){

    }

    private void echo(Update update) {
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
