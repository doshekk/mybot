package org.example;

import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PythonOopQuizBot implements LongPollingSingleThreadUpdateConsumer {

    private static final String BOT_TOKEN = "8742131267:AAHIu6b-KNnT91WcB_ut5LwiV_DKnGj2XGA";

    private final TelegramClient telegramClient = new OkHttpTelegramClient(BOT_TOKEN);
    private final Map<Long, UserSession> userSessions = new ConcurrentHashMap<>();
    private final List<Question> quizBank = new ArrayList<>();

    public PythonOopQuizBot() {
        loadQuestions();
    }

    private void loadQuestions() {
        quizBank.add(new Question("Який метод є конструктором класу в Python?", "__init__", "__new__", "constructor", "init"));
        quizBank.add(new Question("Що означає ключове слово 'self' у методах класу?", "Посилання на поточний екземпляр класу", "Посилання на сам клас", "Ключове слово для створення змінної", "Аналог super() в інших мовах"));
        quizBank.add(new Question("Як реалізувати інкапсуляцію (закриту змінну) в Python за угодою?", "Додати два підкреслення перед назвою (напр. __username)", "Використати ключове слово private", "Використати декоратор @private", "Захистити змінну за допомогою фігурних дужок"));
        quizBank.add(new Question("Яка функція використовується для виклику методів батьківського класу?", "super()", "parent()", "this()", "base()"));
        quizBank.add(new Question("Що буде результатом створення класу без явного успадкування в Python 3?", "Він автоматично успадкується від object", "Виникне помилка синтаксису", "Він не буде мати жодних базових методів", "Створиться статичний інтерфейс"));
        quizBank.add(new Question("Який декоратор використовується для створення статичного методу в класі?", "@staticmethod", "@classmethod", "@property", "@static"));
        quizBank.add(new Question("Що таке 'множинне успадкування' в Python?", "Здатність класу мати більше одного батьківського класу", "Створення багатьох екземплярів одного класу", "Наявність кількох конструкторів у класі", "Успадкування методів через декілька рівнів ланцюжком"));
        quizBank.add(new Question("Який магічний метод відповідає за текстове представлення об'єкта для користувача (функція print)?", "__str__", "__repr__", "__doc__", "__print__"));
        quizBank.add(new Question("Який декоратор дозволяє звертатися до методу класу як до звичайної змінної (геттера)?", "@property", "@getter", "@attribute", "@classmethod"));
        quizBank.add(new Question("Який порядок пошуку методів (MRO) використовує Python для вирішення конфліктів?", "C3-лінеаризація (зліва направо, знизу вгору)", "Случайний вибір першого знайденого", "Справа наліво, зверху вниз", "Пріоритет завжди має перший вказаний батько"));
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                startNewQuiz(chatId);
            }
        }
        else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackData = update.getCallbackQuery().getData();
            if (callbackData.equals("restart_quiz")) {
                startNewQuiz(chatId);
            } else {
                processUserAnswer(chatId, callbackData);
            }
        }
    }

    private void startNewQuiz(long chatId) {
        UserSession session = new UserSession(quizBank);
        userSessions.put(chatId, session);

        sendMessage(chatId, "Вітаю у тесті з теми: <b>Основи ООП у Python</b>! \nВам потрібно відповісти на 10 питань. Успіхів!");
        sendNextQuestion(chatId);
    }

    private void sendNextQuestion(long chatId) {
        UserSession session = userSessions.get(chatId);
        if (session == null) return;

        Question currentQuestion = session.getCurrentQuestion();
        if (currentQuestion != null) {
            String text = "Питання №" + session.getCurrentNumber() + ": " + currentQuestion.getText();

            InlineKeyboardMarkup.InlineKeyboardMarkupBuilder keyboardBuilder = InlineKeyboardMarkup.builder();
            List<String> options = currentQuestion.getShuffledOptions();

            for (int i = 0; i < options.size(); i++) {
                InlineKeyboardButton button = InlineKeyboardButton.builder()
                        .text(options.get(i))
                        .callbackData(String.valueOf(i))
                        .build();

                keyboardBuilder.keyboardRow(new InlineKeyboardRow(button));
            }

            sendKeyboardMessage(chatId, text, keyboardBuilder.build());
        }
    }

    private void processUserAnswer(long chatId, String callbackData) {
        UserSession session = userSessions.get(chatId);
        if (session == null || session.isFinished()) return;

        Question currentQuestion = session.getCurrentQuestion();

        try {
            int selectedIndex = Integer.parseInt(callbackData);
            List<String> options = currentQuestion.getShuffledOptions();
            String selectedAnswer = options.get(selectedIndex);

            if (currentQuestion.isCorrect(selectedAnswer)) {
                session.incrementScore();
                sendMessage(chatId, "Правильно!");
            } else {
                sendMessage(chatId, "Неправильно.");
            }
        } catch (Exception e) {
            sendMessage(chatId, "Сталася помилка при зчитуванні відповіді.");
            e.printStackTrace();
        }

        session.advance();

        if (session.isFinished()) {
            sendFinalReport(chatId, session);
            userSessions.remove(chatId);
        } else {
            sendNextQuestion(chatId);
        }
    }

    private void sendFinalReport(long chatId, UserSession session) {
        LocalDateTime endTime = LocalDateTime.now();
        Duration quizDuration = Duration.between(session.getStartTime(), endTime);

        long minutes = quizDuration.toMinutes();
        long seconds = quizDuration.toSecondsPart();
        String durationStr = String.format("%d хв. %d сек.", minutes, seconds);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String formattedDate = endTime.format(formatter);

        String report = "<b>Тест завершено!</b>\n\n" +
                "<b>Ваш результат:</b> " + session.getScore() + " з 10 балів\n" +
                "<b>Дата завершення:</b> " + formattedDate + "\n" +
                "<b>Тривалість тесту:</b> " + durationStr;

        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder keyboardBuilder = InlineKeyboardMarkup.builder();
        InlineKeyboardButton restartButton = InlineKeyboardButton.builder()
                .text("Почати тест заново")
                .callbackData("restart_quiz")
                .build();
        keyboardBuilder.keyboardRow(new InlineKeyboardRow(restartButton));

        sendKeyboardMessage(chatId, report, keyboardBuilder.build());
    }

    private void sendMessage(long chatId, String text) {
        try {
            SendMessage message = SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text(text)
                    .parseMode("HTML")
                    .build();
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendKeyboardMessage(long chatId, String text, InlineKeyboardMarkup keyboard) {
        try {
            SendMessage message = SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text(text)
                    .parseMode("HTML")
                    .replyMarkup(keyboard)
                    .build();
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(BOT_TOKEN, new PythonOopQuizBot());
            System.out.println("Бот успішно запущений! Натисніть /start або скористайтеся кнопками в Telegram.");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}