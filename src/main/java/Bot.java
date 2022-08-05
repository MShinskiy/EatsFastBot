import Database.*;
import org.apache.commons.dbcp2.BasicDataSource;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.*;


public class Bot extends TelegramLongPollingBot {

    private int state = 0;
    private int newPrice = 0;
    private int orderId = 0;
    //K - user, V - state
    private final HashMap<Courier, Integer> usersState = new HashMap<>();
    private final BasicDataSource connectionPool;

    public Bot(BasicDataSource connectionPool){
        this.connectionPool = connectionPool;
    }

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
        if(usersState.isEmpty() || !isRegistered(update.getMessage().getChatId()) )
            registrationHandler(update);
        else if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println("Message received: " + update.getMessage().getText());
            stateHandler(update);
            System.out.println("Message answered, current state = " + state);
        }
    }

    //do registration for a courier
    private void registrationHandler(Update update) {
        Courier courier = new Courier(
                update.getMessage().getChatId(),
                update.getMessage().getChat().getUserName(),
                update.getMessage().getChat().getUserName(),
                Privilege.COURIER);

        usersState.put(courier, 0);
        try{
            if(!Select.isInCourierTable(connectionPool.getConnection(), courier.getChatId()))
                Insert.insertCourier(
                        connectionPool.getConnection(),
                        courier.getName(),
                        courier.getUsername(),
                        courier.getPrivilege().toString(),
                        courier.getChatId()
                );
        } catch (SQLException sqlE){
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }
    }

    //check registration
    private boolean isRegistered(long chatId) {
        for (Courier user : usersState.keySet()) {
            if(user.getChatId() == chatId)
                return true;
        }
        for(Courier user : usersState.keySet())
            try {
                if (Select.isInCourierTable(connectionPool.getConnection(), user.getChatId()))
                    return true;
            } catch (SQLException sqlE ){
                System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
                sqlE.printStackTrace();
            }
        return false;
    }



    //handle users states
    private void stateHandler(Update update) {
        long state = update.getMessage().getChatId();
        System.out.println(update.getMessage().getChat().getUserName());
        System.out.println(state);
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
                case "Change price":
                    this.state = 6;
                    break;
            }

        else if(this.state == 5)
            switch (update.getMessage().getText()) {
                case "All orders":
                    viewAllOrders(update);
                    break;
                case "My orders":
                    viewMyOrders(update);
                    break;
                case "Unchecked orders":
                    viewUncheckedOrders(update);
                    break;
            }

        else if(this.state == 7)
            if(isValidId(update))
                orderId = Integer.parseInt(update.getMessage().getText());
            else {
                sendInvalidInputMessage(update, "Invalid id.");
                return;
            }

        else if(this.state == 8)
            if (isValidPrice(update))
                newPrice = Integer.parseInt(update.getMessage().getText());
            else {
                sendInvalidInputMessage(update, "Invalid price.");
                return;
            }

        switch (this.state) {
            case 2:
                getOrders(update);
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
            case 6:
                askOrderId(update);
                break;
            case 7:
                askNewPrice(update);
                break;
            case 8:
                boolean success = updatePrice(update, this.orderId, this.newPrice);
                showNewPriceMessage(update, success);
                break;
        }
    }

    //state 1
    private void startupMessage(Update update) {
        sendMessageForState1(update, "Welcome!");
    }

    //state 2 -> 3
    private void getOrders(Update update) {
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

    //state 3 -> 1
    private void addOrder(Update update){
        String order = update.getMessage().getText();
        boolean success = false;
        try {
            long s = System.currentTimeMillis();
            success = Insert.insertNewOrder(connectionPool.getConnection(), order);
            System.out.println("Added in: "  + (System.currentTimeMillis() - s));
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
        }
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        if(success)
            message.setText("Order added.");
        else
            message.setText("Something went wrong.");

        try {
            execute(message);
            if(success)
                notifyCouriers(update);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //state 4 -> 5
    private void viewOrder(Update update) {
        //build message
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("What orders would you like to view?");

        //build keyboard
        List<KeyboardRow> list = Arrays.asList(
                new KeyboardRow(new ArrayList<>(Collections.singletonList(new KeyboardButton("All orders")))),
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

    //state 5 -> 1
    private void showOrdersMessage(Update update) {
        sendMessageForState1(update, "Your orders.");
    }

    //state 6 -> 7
    private void askOrderId(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("Enter order id:");

        try{
            execute(message);
            this.state = 7;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //state 7 -> 8
    private void askNewPrice(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("Enter new price:");

        try{
            execute(message);
            this.state = 8;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //state 8 -> 1
    private void showNewPriceMessage(Update update, boolean updateSuccess){
        String message;
        if (updateSuccess)
            message = "Price updated. Price: " + newPrice + " ID: " + orderId;
        else
            message = "Something went wrong when updating price";

        sendMessageForState1(update, message);
    }

    //Support methods=======================================================
    //send message for option state (state = 1)
    private void sendMessageForState1(Update update, String messageText) {
        //build message
        SendMessage message = new SendMessage();
        message.setText(messageText);
        message.setChatId(update.getMessage().getChatId().toString());

        //build keyboard
        List<KeyboardRow> list = Arrays.asList(
                new KeyboardRow(new ArrayList<>(Collections.singletonList(new KeyboardButton("Add order")))),
                new KeyboardRow(new ArrayList<>(Collections.singletonList(new KeyboardButton("View orders")))),
                new KeyboardRow(new ArrayList<>(Collections.singletonList(new KeyboardButton("Change price"))))
        );
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

    private void sendInvalidInputMessage(Update update, String messageText) {
        //build message
        SendMessage message = new SendMessage();
        message.setText(messageText);
        message.setChatId(update.getMessage().getChatId().toString());

        //execute message
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //view orders methods---------------------------------------------------
    private void viewAllOrders(Update update) {
        List<String> orders = new ArrayList<>();
        try {
            //get orders from db
            orders = Select.selectAllOrders(connectionPool.getConnection());
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }

        //send each order as a message
        for (String order : orders) {
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId().toString());
            //if (!order.equals(""))
                message.setText(order);
            //else
            //    continue;
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
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

    //update price in db
    private boolean updatePrice(Update update, int orderId, int newPrice){
        try {
            return Database.Update.updatePrice(connectionPool.getConnection(), orderId, newPrice);
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
            return false;
        }
    }

    //input validation methods----------------------------------------------
    private boolean isValidPrice(Update update){
        try{
            Integer.parseInt(update.getMessage().getText());
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

    private boolean isValidId(Update update){
        try{
            long id = Long.parseLong(update.getMessage().getText());
            return Select.isIdInOrderTable(connectionPool.getConnection(), id);
        } catch (NumberFormatException | SQLException e){
            return false;
        }
    }
    //----------------------------------------------------------------------

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
