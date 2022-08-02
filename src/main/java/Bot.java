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
    private int state = 0;
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
        if (update.hasMessage() && update.getMessage().hasText())
            stateHandler(update);
    }

    private void stateHandler(Update update){
        if(this.state == 1)
            switch (update.getMessage().getText()){
                case "Add order":
                    this.state = 2;
                    break;
                case "View order":
                    this.state = 5;
                    break;
            }
        if(update.getMessage().getText() == "/start")
            this.state = 1;
        switch (state){
            case 1:
                startupMessage(update);
                break;
            case 2:
                getOrder();
                break;
            case 3:
                addOrder(update);
                break;
            case 4:
                notifyCouriers();
                break;
            case 5:
                viewOrder(update);
                break;
        }

    }
    //state 1
    private void startupMessage(Update update) {
        //build message
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("Welcome!");

        //build keyboard
        //buttons
        KeyboardButton buttonOrder = new KeyboardButton("Add order");
        KeyboardButton buttonViewOrders = new KeyboardButton("View orders");
        //row
        KeyboardRow row = new KeyboardRow();
        row.add(buttonOrder);
        row.add(buttonViewOrders);
        //keyboard
        List<KeyboardRow> list = Collections.singletonList(row);
        ReplyKeyboardMarkup startMessageRKM = new ReplyKeyboardMarkup();
        startMessageRKM.setKeyboard(list);
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

    //state 2
    private void getOrder() {
        SendMessage message = new SendMessage();
        message.setText("Writing down your order...");
        try{
            execute(message);
            this.state = 3;
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    //state 3
    private void addOrder(Update update) {
        String order = update.getMessage().getText();
        /*TODO
            connect to db and add order to orders table
         */
        SendMessage message = new SendMessage();
        message.setText("Order added.");
        try{
            execute(message);
            this.state = 4;
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    //state 4
    private void notifyCouriers() {
        /*TODO
            notify method
         */
        this.state = 1;
    }

    //state 5
    private void viewOrder(Update update){

    }


    //echo
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
