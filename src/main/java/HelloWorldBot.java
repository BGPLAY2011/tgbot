import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.util.*;
public class HelloWorldBot extends TelegramLongPollingBot {
    public HashMap<Long, User> users = new HashMap<>();
    public HashMap<Long, TempUserData> tempUsers = new HashMap<>();
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String msg = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if (users.containsKey(chatId)) {
                sendMsg(chatId, "Твої дані вже збережено. Скористайся кнопками нижче.");
                sendButtons(chatId);
                return;
            } else {
                if (!tempUsers.containsKey(chatId)) {
                    tempUsers.put(chatId, new TempUserData());
                    sendMsg(chatId, "Як тебе звати?");
                    return;
                }
            }
            TempUserData temp = tempUsers.get(chatId);
            if (temp.name == null) {
                temp.name = msg;
                sendMsg(chatId, "Скільки тобі років?");
                return;
            }
            if (temp.age == -1) {
                try {
                    temp.age = Integer.parseInt(msg);
                    sendMsg(chatId, "Який твій улюблений колір?");
                } catch (NumberFormatException e) {
                    sendMsg(chatId, "Будь ласка, введи число.");
                }
                return;
            }
            if (temp.favoriteColor == null) {
                temp.favoriteColor = msg;
                sendMsg(chatId, "Яке твоє улюблене слово?");
                return;
            }
            if (temp.favoriteWord == null) {
                temp.favoriteWord = msg;
                User user = new User(chatId, null, temp.name, temp.age, temp.favoriteWord, temp.favoriteColor);
                users.put(chatId, user);
                tempUsers.remove(chatId);
                sendMsg(chatId, "Дякую! Твої дані збережено.");
                sendButtons(chatId);
                return;
            }
        }
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (data.equals("show_info")) {
                User user = users.get(chatId);
                if (user != null) {
                    String info = String.format("Ім'я: %s\nВік: %d\nУлюблений колір: %s\nУлюблене слово: %s",
                            user.name, user.age, user.favoriteColor, user.favoriteWord);
                    sendMsg(chatId, info);
                } else {
                    sendMsg(chatId, "Дані не знайдено.");
                }
            }
            if (data.equals("reset_info")) {
                users.remove(chatId);
                tempUsers.remove(chatId);
                sendMsg(chatId, "Дані очищено. Почнемо спочатку:\nЯк тебе звати?");
                tempUsers.put(chatId, new TempUserData());
            }
        }
    }
    public void sendButtons(long chatId) {
        InlineKeyboardButton showButton = new InlineKeyboardButton();
        showButton.setText("Показати мої дані");
        showButton.setCallbackData("show_info");
        InlineKeyboardButton resetButton = new InlineKeyboardButton();
        resetButton.setText("Очистити і ввести заново");
        resetButton.setCallbackData("reset_info");
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(Collections.singletonList(showButton));
        rows.add(Collections.singletonList(resetButton));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Що бажаєш зробити?");
        message.setReplyMarkup(markup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public void sendMsg(long chatId, String msg) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(msg);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    @Override
    public String getBotUsername() {
        return "Tasatrua_bot";
    }
    @Override
    public String getBotToken() {
        return "7815646462:AAFSudp2ElzomO8OlCwMpYxK81NXbdCMRYA";
    }
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new HelloWorldBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    static class User {
        public String userName = null;
        public String name = null;
        public int age = -1;
        public String favoriteColor = null;
        public String favoriteWord = null;
        public long chatId;
        public User(long chatId, String userName, String name, int age, String favoriteWord, String favoriteColor) {
            this.chatId = chatId;
            this.userName = userName;
            this.name = name;
            this.age = age;
            this.favoriteWord = favoriteWord;
            this.favoriteColor = favoriteColor;
        }
    }
    static class TempUserData {
        public String name = null;
        public int age = -1;
        public String favoriteColor = null;
        public String favoriteWord = null;
    }
}
