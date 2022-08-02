import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class Bot extends TelegramLongPollingBot {

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
        if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println("Message received: " + update.getMessage().getText());
            stateHandler(update);
            System.out.println("Message answered, current state = " + state);
        }
    }

    private void stateHandler(Update update) {
        if (Objects.equals(update.getMessage().getText(), "/start"))
            startupMessage(update);
        if (this.state == 1)
            switch (update.getMessage().getText()) {
                case "Add order":
                    this.state = 2;
                    break;
                case "View orders":
                    this.state = 4;
                    break;
            }
        else if(this.state == 5)
            switch (update.getMessage().getText()) {
                case "Today's orders":
                    viewTodayOrders(update);
                    break;
                case "My orders":
                    viewMyOrders(update);
                    break;
                case "Unchecked orders":
                    viewUncheckedOrders(update);
                    break;
            }

        switch (this.state) {
            case 2:
                getOrder(update);
                break;
            case 3:
                addOrder(update);
                break;
            case 4:
                viewOrder(update);
                break;
            case 5:
                showOrdersMessage(update);
                break;
        }

    }

    //state 1
    private void startupMessage(Update update) {
        sendMessageForState1(update, "Welcome!");
    }

    //state 2
    private void getOrder(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("Writing down your order...");
        try {
            execute(message);
            this.state = 3;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //state 3
    private void addOrder(Update update) {
        String order = update.getMessage().getText();
        /*
        TODO
            connect to db and add order to orders table
         */
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("Order added.");
        try {
            execute(message);
            notifyCouriers(update);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //state 4
    private void viewOrder(Update update) {
        //build message
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("What orders would you like to view?");

        //build keyboard
        List<KeyboardRow> list = Arrays.asList(
                new KeyboardRow(new ArrayList<>(Collections.singletonList(new KeyboardButton("Today's orders")))),
                new KeyboardRow(new ArrayList<>(Collections.singletonList(new KeyboardButton("My orders")))),
                new KeyboardRow(new ArrayList<>(Collections.singletonList(new KeyboardButton("Unchecked orders"))))
        );
        ReplyKeyboardMarkup ordersMessageRKM = new ReplyKeyboardMarkup();
        ordersMessageRKM.setKeyboard(list);
        ordersMessageRKM.setInputFieldPlaceholder("Placeholder");
        ordersMessageRKM.setOneTimeKeyboard(true);
        ordersMessageRKM.setResizeKeyboard(true);
        message.setReplyMarkup(ordersMessageRKM);

        //execute message
        try{
            execute(message);
            this.state = 5;
        } catch (TelegramApiException e){
            e.printStackTrace();
        }

    }

    //state 5
    private void showOrdersMessage(Update update) {
        sendMessageForState1(update, "Your orders.");
    }

    //Support methods=======================================================
    //send message for option state (state = 1)
    private void sendMessageForState1(Update update, String messageText) {
        //build message
        SendMessage message = new SendMessage();
        message.setText(messageText);
        message.setChatId(update.getMessage().getChatId().toString());

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

        //execute message
        try {
            execute(message);
            this.state = 1;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //view orders methods---------------------------------------------------
    private void viewTodayOrders(Update update) {
        /*
        TODO
            Show list of today's orders
         */
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("Today's orders.");
        try{
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void viewMyOrders(Update update) {
        /*
        TODO
            Show list of my orders
         */
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("My orders.");
        try{
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void viewUncheckedOrders(Update update) {
        /*
        TODO
            Show list of unchecked orders
         */
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("Unchecked orders.");
        try{
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------

    //Notifying couriers that new order has been placed
    private void notifyCouriers(Update update) {
        /*
        TODO
            notify method
         */
        sendMessageForState1(update, "Couriers notified!");
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
