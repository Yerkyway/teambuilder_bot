package com.footballbot.telegram;

import java.util.concurrent.ConcurrentHashMap;

public class UserStateManager {

    private static final ConcurrentHashMap<Long, UserState> userStates = new ConcurrentHashMap<>();

    public static UserState setUserState(Long userId, UserState userState) {
        return userStates.put(userId, userState);
    }

    public static UserState getUserState(Long userId) {
        return userStates.get(userId);
    }

    public static void removeUserState(Long userId) {
        userStates.remove(userId);
    }

    public static boolean hasUserState(Long userId) {
        return userStates.containsKey(userId);
    }

    public enum UserState {
        WAITING_FOR_MATCH_DATE,
        WAITING_FOR_PLAYERS_LIST_DATE,
        WAITING_FOR_JOIN_MATCH_DATE
    }
}
