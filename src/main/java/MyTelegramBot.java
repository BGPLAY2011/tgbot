import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
public class MyTelegramBot extends TelegramLongPollingBot {
    private final Map<Long, Integer> guessedNumbers = new HashMap<>();
    public HashMap <Long, String> users = new HashMap<>();
    private final Random random = new Random();
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
            String userMessage = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if (userMessage.equalsIgnoreCase("загадай мені число")) {
                int number = random.nextInt(100) + 1;
                guessedNumbers.put(chatId, number);
                send(chatId, "Я загадав число від 1 до 100. Спробуй вгадати!");
            } else if (guessedNumbers.containsKey(chatId)) {
                try {
                    int guess = Integer.parseInt(userMessage);
                    int secret = guessedNumbers.get(chatId);
                    if (guess < secret) {
                        send(chatId, "Більше!");
                    } else if (guess > secret) {
                        send(chatId, "Менше!");
                    } else {
                        send(chatId, "Вгадав! Молодець");
                        guessedNumbers.remove(chatId);
                    }
                } catch (NumberFormatException e) {
                    send(chatId, "Введи ціле число, будь ласка!");
                }
            } else {
                send(chatId, "Напиши 'загадай мені число', щоб почати гру.");
            }
        }
    }
    private void send(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println("Помилка надсилання повідомлення: " + e.getMessage());
        }
    }
}