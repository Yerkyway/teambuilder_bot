package com.footballbot.telegram;

import com.footballbot.models.MatchEntity;
import com.footballbot.services.MatchService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
public class TeamBuilderBot extends TelegramLongPollingBot {

    private final MatchService matchService;
    private static final String ADMIN_USERNAME = "yerkeshhhh";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private void sendText(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text); // Using constructor

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Failed to send message to chat ID " + chatId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public TeamBuilderBot(MatchService matchService) {
        this.matchService = matchService;
    }


    @Override
    public String getBotUsername() {
        return "MatchLineupBot";
    }

    @Override
    public String getBotToken() {
        return "8145502018:AAFLAFNpCYonE5xSNM5zQnW2ND4m-HYGE98";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        String username = update.getMessage().getFrom().getUserName();
        Long telegramId = update.getMessage().getFrom().getId();

        if (UserStateManager.hasUserState(telegramId)) {
            handleUserState(telegramId, messageText, chatId);
            return;
        }

        switch (messageText) {
            case "/start" -> {
                sendText(chatId, "Добро пожаловать в футбольного бота!\n" +
                        "Доступные команды:\n" +
                        "/create_match - создать матч\n" +
                        "/join - присоединиться к матчу\n" +
                        "/match_list - список матчей\n" +
                        "/players_list - список игроков на ближайший матч");
            }
            case "/create_match" -> {
                LocalDate matchDate = LocalDate.now();
                if (!username.equals(ADMIN_USERNAME)) {
                    sendText(chatId, "⚠️ Только администратор может создавать матчи.");
                    return;
                }
                UserStateManager.setUserState(telegramId, UserStateManager.UserState.WAITING_FOR_MATCH_DATE);
                sendText(chatId, "Введите дату матча в формате dd-MM-yyyy:");
            }
            case "/join" -> {
                LocalDate matchDate = LocalDate.now();
                try {
                    matchService.addPlayer(telegramId, username, matchDate);
                    sendText(chatId, "✅ Вы успешно присоединились к матчу на " + matchDate + ".");
                } catch (IllegalArgumentException e) {
                    // Обработка исключения IllegalArgumentException
                    sendText(chatId, "⚠️ " + e.getMessage());  // Показываем причину ошибки
                } catch (Exception e) {
                    sendText(chatId, "❌ Произошла ошибка при попытке присоединиться к матчу.");
                }
            }
            case "/match_list" -> {
                List<MatchEntity> matches = matchService.getAllMatches();
                if (matches.isEmpty()) {
                    sendText(chatId, "Нет доступных матчей");
                } else {
                    StringBuilder response = new StringBuilder("Список матчей:\n");
                    for (MatchEntity match : matches) {
                        response.append("Матч на ").append(match.getDate()).append("\n");
                    }
                    sendText(chatId, response.toString());
                }
            }
            case "/players_list" -> {
                LocalDate matchDate = LocalDate.now();
                List<String> players = matchService.playersList(matchDate);
                if (players.isEmpty()) {
                    sendText(chatId, "Нет игроков на матч на " + matchDate + ".");
                } else {
                    StringBuilder response = new StringBuilder("Игроки на матч " + matchDate + ":\n");
                    for (int i = 0; i < players.size(); i++) {
                        response.append(i + 1).append(". ").append(players.get(i)).append("\n");
                    }
                    ;
                    sendText(chatId, response.toString());
                }
            }
        }
    }

    private void handleUserState(Long telegramId, String messageText, Long chatId) {
        UserStateManager.UserState userState = UserStateManager.getUserState(telegramId);

        if (userState == UserStateManager.UserState.WAITING_FOR_MATCH_DATE) {
            handleMatchDateInput(telegramId, chatId, messageText);
        }
    }

    private void handleMatchDateInput(Long telegramId, Long chatId, String messageText) {
        try {
            LocalDate matchDate = LocalDate.parse(messageText, DATE_FORMATTER);
            Optional<MatchEntity> existingMatch = matchService.findMatchByDate(matchDate);

            if (existingMatch.isPresent()) {
                MatchEntity match = existingMatch.get();
                StringBuilder response = new StringBuilder("Матч на эту дату уже существует. \n");
                sendText(chatId, response.toString());
            } else {
                MatchEntity match = matchService.createMatchIfNotExists(matchDate);
                sendText(chatId, "✅ Матч на " + matchDate + " успешно создан.");
            }

            UserStateManager.removeUserState(telegramId);
        } catch (DateTimeParseException e) {
            sendText(chatId, "❌ Неверный формат даты. Пожалуйста, используйте формат dd-MM-yyyy.");
        } catch (Exception e) {
            sendText(chatId, "❌ Произошла ошибка при создании матча: " + e.getMessage());
        }
    }
}

