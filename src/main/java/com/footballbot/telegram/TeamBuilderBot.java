package com.footballbot.telegram;

import com.footballbot.models.MatchEntity;
import com.footballbot.services.MatchService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
public class TeamBuilderBot extends TelegramLongPollingBot {

    private final MatchService matchService;

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

        switch (messageText) {
            case "/start" -> {
                sendText(chatId, "Добро пожаловать в футбольного бота!\n" +
                        "Доступные команды:\n" +
                        "/create_match - создать матч\n" +
                        "/join - присоединиться к матчу\n" +
                        "/match_list - список матчей\n" +
                        "/players_list - список игроков на ближайший матч");
            } case "/create_match" -> {
                LocalDate matchDate = LocalDate.now();
                try {
                    MatchEntity match = matchService.createMatchIfNotExists(matchDate);
                    if (match.getId() != null) {
                        sendText(chatId, "Матч на " + matchDate + " уже существует.");
                        return;
                    }
                    sendText(chatId, "Матч на " + matchDate + " успешно создан!");
                } catch (IllegalArgumentException e) {
                    sendText(chatId, e.getMessage());
                }
            } case "/join" -> {
                LocalDate matchDate = LocalDate.now();
                try {
                    matchService.addPlayer(telegramId, username, matchDate);
                    sendText(chatId, "✅ Вы успешно присоединились к матчу на " + matchDate + ".");
                } catch (IllegalArgumentException e) {
                    sendText(chatId, "⚠️ " + e.getMessage());  // Показываем причину ошибки
                } catch (Exception e) {
                    sendText(chatId, "❌ Произошла ошибка при попытке присоединиться к матчу.");
                }
        } case "/match_list" -> {
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
            } case "/players_list" -> {
                LocalDate matchDate = LocalDate.now();
                List<String> players = matchService.playersList(matchDate);
                if (players.isEmpty()) {
                    sendText(chatId, "Нет игроков на матч на " + matchDate + ".");
                } else {
                    StringBuilder response = new StringBuilder("Игроки на матч " + matchDate + ":\n");
                    for(int i = 0; i < players.size(); i++) {
                        response.append(i + 1).append(". ").append(players.get(i)).append("\n");
                    };
                    sendText(chatId, response.toString());
                    }
                }
            }
        }
    }

