import Database.*;
import org.apache.commons.dbcp2.BasicDataSource;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.*;


public class Bot extends TelegramLongPollingBot {
    private int newPrice = 0;
    private int orderId = 0;
    //K - user, V - state
    private final HashMap<Long, Integer> usersState = new HashMap<>();
    private final HashMap<Long, Courier> userInfo = new HashMap<>();
    private final BasicDataSource connectionPool;
    private static final int STATUS_UNASSIGNED = 0;
    private static final int STATUS_TAKEN = 1;
    private static final int STATUS_COMPLETE = 2;

    public Bot(BasicDataSource connectionPool) {
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
        if (update.hasCallbackQuery())
            callbackHandler(update);
        else if (update.hasMessage() && update.getMessage().hasText()) {
            if (usersState.isEmpty() || !isRegistered(update.getMessage().getChatId()))
                registrationHandler(update);
            else
                stateHandler(update);
        }
    }

    //do registration for a courier
    private void registrationHandler(Update update) {
        if (update.getMessage().getFrom().getUserName() == null) {
            SendMessage message = new SendMessage();
            message.setText("First create your telegram username!");
            message.setChatId(update.getMessage().getChatId());
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        Courier courier = new Courier(
                update.getMessage().getChatId(),
                update.getMessage().getChat().getUserName(),
                update.getMessage().getChat().getUserName(),
                Privilege.COURIER);

        usersState.put(courier.getChatId(), 0);
        userInfo.put(courier.getChatId(), courier);

        try {
            if (!Select.isInCourierTable(connectionPool.getConnection(), courier.getChatId()))
                Insert.insertCourier(
                        connectionPool.getConnection(),
                        courier.getName(),
                        courier.getUsername(),
                        courier.getPrivilege().name(),
                        courier.getChatId()
                );
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }

        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setText("Got you registered!\n"
                + courier +
                "\nType /start to continue.");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //check registration
    private boolean isRegistered(long chatId) {
        return usersState.containsKey(chatId);
    }

    //handle callback to add price and accept order
    private void callbackHandler(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String[] data = callbackQuery.getData().split(":");
        long orderId = Long.parseLong(data[2]);
        if (data[0].equals("order")) {
            if (data[1].equals("price")) {
                this.orderId = (int) orderId;
                askNewPrice(update);
            } else if (data[1].equals("take")) {
                int price = -1;
                boolean isOrderTaken = false;
                try {
                    price = Select.selectOrderPrice(connectionPool.getConnection(), orderId);
                    isOrderTaken = Select.isOrderCourierAssigned(connectionPool.getConnection(), orderId);
                } catch (SQLException sqlE) {
                    System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
                    sqlE.printStackTrace();
                }
                if (price <= 0) {
                    AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
                    answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
                    answerCallbackQuery.setText("Enter price first!");
                    answerCallbackQuery.setShowAlert(true);
                    try {
                        execute(answerCallbackQuery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if (isOrderTaken) {
                    AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
                    answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
                    answerCallbackQuery.setText("Order has been taken already!");
                    answerCallbackQuery.setShowAlert(true);
                    try {
                        execute(answerCallbackQuery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Database.Update.updateOrderCourier(
                                connectionPool.getConnection(),
                                orderId,
                                callbackQuery.getMessage().getChatId()
                        );
                        Database.Update.updateOrderStatus(
                                connectionPool.getConnection(),
                                orderId,
                                STATUS_TAKEN
                        );
                    } catch (SQLException sqlE) {
                        System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
                        sqlE.printStackTrace();
                    }
                }

            }
        }
    }

    //handle users states
    private void stateHandler(Update update) {
        /*
        TODO
            split states into classes and  divide responsibility between them
            more abstract classes and more abstract methods
         */
        int state = usersState.get(update.getMessage().getChatId());
        System.out.println(update.getMessage().getChat().getUserName());
        System.out.println(state);
        if (Objects.equals(update.getMessage().getText(), "/start"))
            startupMessage(update);
        if (state == 1)
            switch (update.getMessage().getText()) {
                case "Add order":
                    state = 2;
                    break;
                case "View orders":
                    state = 4;
                    break;
                case "Change price":
                    state = 6;
                    break;
            }

        else if (state == 5)
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

        else if (state == 7)
            if (isValidId(update))
                orderId = Integer.parseInt(update.getMessage().getText());
            else {
                sendInvalidInputMessage(update, "Invalid id.");
                return;
            }

        else if (state == 8)
            if (isValidPrice(update))
                newPrice = Integer.parseInt(update.getMessage().getText());
            else {
                sendInvalidInputMessage(update, "Invalid price.");
                return;
            }

        switch (state) {
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
            usersState.replace(update.getMessage().getChatId(), 3);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //state 3 -> 1
    private void addOrder(Update update) {
        String order = update.getMessage().getText();
        boolean success = false;
        try {
            long s = System.currentTimeMillis();
            long orderId = Insert.insertNewOrder(connectionPool.getConnection(), order);
            if (orderId > 0)
                success = true;
            System.out.println("Added in: " + (System.currentTimeMillis() - s));
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
        }
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        if (success)
            message.setText("Order added.");
        else
            message.setText("Something went wrong.");

        try {
            execute(message);
            if (success)
                notifyCouriers(update, order, orderId);
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
        try {
            execute(message);
            usersState.replace(update.getMessage().getChatId(), 5);
        } catch (TelegramApiException e) {
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

        try {
            execute(message);
            usersState.replace(update.getMessage().getChatId(), 7);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //state 7 -> 8
    private void askNewPrice(Update update) {
        SendMessage message = new SendMessage();
        long chatId;
        if(update.hasMessage())
            chatId = update.getMessage().getChatId();
        else if(update.hasCallbackQuery())
            chatId = update.getCallbackQuery().getMessage().getChatId();
        else
            throw new NullPointerException("Update has no Message nor CallbackQuery");
        message.setChatId(chatId);
        message.setText("Enter new price:");

        try {
            execute(message);
            usersState.replace(chatId, 8);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //state 8 -> 1
    private void showNewPriceMessage(Update update, boolean updateSuccess) {
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
            usersState.replace(update.getMessage().getChatId(), 1);
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
        List<String> orders = null;
        try {
            //get orders from db
            orders = Select.selectAllOrders(connectionPool.getConnection());
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }

        if(orders != null)
            sendOrders(update, orders);
    }

    private void viewMyOrders(Update update) {
        List<String> orders = null;
        try {
            //get orders from db
            orders = Select.selectMyOrders(
                    connectionPool.getConnection(),
                    update.getMessage().getChatId()
            );
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }

        if(orders != null)
            sendOrders(update, orders);
    }

    private void viewUncheckedOrders(Update update) {
        List<String> orders = null;
        try {
            //get orders from db
            orders = Select.selectUncheckedOrders(connectionPool.getConnection());
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }

        if( orders != null)
            sendOrders(update, orders);
    }

    //send each order as a message
    private void sendOrders(Update update, List<String> orders) {
        for (String order : orders) {
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId().toString());
            message.setText(order);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    //----------------------------------------------------------------------

    //Notifying couriers that new order has been placed
    private void notifyCouriers(Update update, String order, long orderId) {

        for (Long id : userInfo.keySet()) {
            //build message
            SendMessage message = new SendMessage();
            message.setChatId(id);
            message.setText(
                    "New order.\nID: " + orderId +
                            "\nOrder:\n" + order);

            //build inline keyboard
            InlineKeyboardMarkup ikm = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> buttons = new ArrayList<>();

            InlineKeyboardButton buttonTake = new InlineKeyboardButton();
            InlineKeyboardButton buttonAddPrice = new InlineKeyboardButton();

            buttonTake.setText("Take");
            buttonAddPrice.setText("Add price");

            buttonTake.setCallbackData("order:take:" + orderId);
            buttonAddPrice.setCallbackData("order:price:" + orderId);

            buttons.add(buttonTake);
            buttons.add(buttonAddPrice);
            rows.add(buttons);
            ikm.setKeyboard(rows);
            message.setReplyMarkup(ikm);

            try {
                //execute
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        sendMessageForState1(update, "Couriers notified!");
    }

    //update price in db
    private boolean updatePrice(Update update, int orderId, int newPrice) {
        try {
            return Database.Update.updatePrice(connectionPool.getConnection(), orderId, newPrice);
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
            return false;
        }
    }

    private void sendTakeOrderMessage(Update update) {
        /*
        TODO
            message that is sent after order is taken by one of the couriers
         */
        SendMessage message = new SendMessage();
        //message.setChatId();
    }

    //input validation methods----------------------------------------------
    private boolean isValidPrice(Update update) {
        try {
            Integer.parseInt(update.getMessage().getText());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidId(Update update) {
        try {
            long id = Long.parseLong(update.getMessage().getText());
            return Select.isIdInOrderTable(connectionPool.getConnection(), id);
        } catch (NumberFormatException | SQLException e) {
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
