package org.example;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.*;
public class MyTelegramBot extends TelegramLongPollingBot {
    private Map<Long, User> users = new HashMap<>();
    private Map<Long, Long> pendingRequests = new HashMap<>();
    private Map<Long, String> playerMoves = new HashMap<>();
    private Set<Long> confirmedPlayers = new HashSet<>();
    @Override
    public String getBotUsername() {
        return "Tasatrua_bot";
    }
    @Override
    public String getBotToken() {
        return "7815646462:AAFSudp2ElzomO8OlCwMpYxK81NXbdCMRYA";
    }
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
    }
    private void handleTextMessage(Message message) {
        String text = message.getText();
        long fromChatId = message.getChatId();
        String fromUser = message.getFrom().getUserName();
        users.put(fromChatId, new User(fromChatId, fromUser));
        if (text.startsWith("@")) {
            String targetUsername = text.substring(1);
            sendConfirmInvite(fromChatId, targetUsername);
        }
    }
    private void sendConfirmInvite(long fromChatId, String targetUsername) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> row = Arrays.asList(
                createButton("Так", "invite_yes_" + targetUsername + "_" + fromChatId),
                createButton("Ні", "invite_no")
        );
        markup.setKeyboard(Collections.singletonList(row));
        send(fromChatId, "Ви хочете зіграти з @" + targetUsername + "?", markup);
    }
    private void handleCallback(CallbackQuery callback) {
        String data = callback.getData();
        long userChatId = callback.getMessage().getChatId();
        String userName = callback.getFrom().getUserName();
        if (data.startsWith("invite_yes_")) {
            String[] parts = data.split("_");
            String targetUsername = parts[2];
            long initiatorId = Long.parseLong(parts[3]);
            Optional<Long> targetIdOpt = users.entrySet().stream()
                    .filter(e -> targetUsername.equals(e.getValue().userName))
                    .map(Map.Entry::getKey).findFirst();
            if (targetIdOpt.isPresent()) {
                long targetChatId = targetIdOpt.get();
                pendingRequests.put(targetChatId, initiatorId);
                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<InlineKeyboardButton> row = Arrays.asList(
                        createButton("Так", "accept_" + initiatorId),
                        createButton("Ні", "decline")
                );
                markup.setKeyboard(Collections.singletonList(row));
                send(targetChatId, "З вами хоче зіграти @" + userName + ". Грати?", markup);
            } else {
                send(userChatId, "Не знайдено @" + targetUsername + " серед користувачів.");
            }
        } else if (data.startsWith("accept_")) {
            long initiatorId = Long.parseLong(data.split("_")[1]);
            confirmedPlayers.add(userChatId);
            confirmedPlayers.add(initiatorId);
            sendGameButtons(userChatId);
            sendGameButtons(initiatorId);
        } else if (data.equals("decline") || data.equals("invite_no")) {
            send(userChatId, "Гру скасовано.");
        } else if (data.startsWith("move_")) {
            String move = data.split("_")[1];
            playerMoves.put(userChatId, move);
            send(userChatId, "Ви обрали: " + move);
            if (confirmedPlayers.size() == 2 && playerMoves.size() == 2) {
                Long[] ids = confirmedPlayers.toArray(new Long[0]);
                long id1 = ids[0], id2 = ids[1];
                String move1 = playerMoves.get(id1);
                String move2 = playerMoves.get(id2);
                String result = getWinner(move1, move2, id1, id2);
                send(id1, result);
                send(id2, result);
                sendPlayAgainButtons(id1);
                sendPlayAgainButtons(id2);
                confirmedPlayers.clear();
                playerMoves.clear();
            }
        } else if (data.equals("play_again")) {
            sendGameButtons(userChatId);
        } else if (data.equals("new_game")) {
            send(userChatId, "Введіть @username для нової гри.");
        }
    }
    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setCallbackData(callbackData);
        return btn;
    }
    private void sendGameButtons(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = Arrays.asList(
                Collections.singletonList(createButton("Камінь", "move_rock")),
                Collections.singletonList(createButton("Ножиці", "move_scissors")),
                Collections.singletonList(createButton("Папір", "move_paper"))
        );
        markup.setKeyboard(rows);
        send(chatId, "Оберіть свій хід:", markup);
    }
    private void sendPlayAgainButtons(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = Arrays.asList(
                Collections.singletonList(createButton("Спробувати ще раз", "play_again")),
                Collections.singletonList(createButton("Нова гра з іншим", "new_game"))
        );
        markup.setKeyboard(rows);
        send(chatId, "Що хочеш зробити?", markup);
    }
    private String getWinner(String m1, String m2, long id1, long id2) {
        if (m1.equals(m2)) return "Нічия! Обидва обрали: " + m1;
        boolean p1Wins = (m1.equals("rock") && m2.equals("scissors")) ||
                (m1.equals("scissors") && m2.equals("paper")) ||
                (m1.equals("paper") && m2.equals("rock"));
        if (p1Wins) return "Переміг @" + users.get(id1).userName + "! (" + m1 + " проти " + m2 + ")";
        else return "Переміг @" + users.get(id2).userName + "! (" + m2 + " проти " + m1 + ")";
    }
    private void send(long chatId, String text) {
        send(chatId, text, null);
    }
    private void send(long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        if (markup != null) {
            message.setReplyMarkup(markup);
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    static class User {
        String userName;
        long chatId;
        public User(long chatId, String userName) {
            this.chatId = chatId;
            this.userName = userName;
        }
    }
}
