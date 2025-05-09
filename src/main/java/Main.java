import org.example.MyTelegramBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
public class Main {
    public static void main(String[] args)  {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new MyTelegramBot());
            System.out.println("Бот запущено!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
