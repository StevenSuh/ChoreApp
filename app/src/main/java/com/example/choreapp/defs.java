package com.example.choreapp;

public class defs {

    public static float LOW_OPACITY = 0.5f;
    public static float EXTRA_LOW_OPACITY = 0.25f;

    public static String CREATE_PROFILE_NAME = "CREATE_PROFILE_NAME";

    public static String[] USER_COLORS = { "#DAAFEA", "#9ADDB6", "#9AE0F7", "#BDB8FF", "#F6B880", "#FFDF7E" };

    public static int getColorInt(String colorToCompare) {
        int index = 0;

        for (String userColor : USER_COLORS) {
            if (userColor.equals(colorToCompare)) {
                break;
            }
            index++;
        }

        switch (index) {
            case 0:
                return R.drawable.color_one;
            case 1:
                return R.drawable.color_two;
            case 2:
                return R.drawable.color_three;
            case 3:
                return R.drawable.color_four;
            case 4:
                return R.drawable.color_five;
            case 5:
                return R.drawable.color_six;
            default:
                return R.drawable.color_seven;
        }
    }

    public static String SHARED_PREF = "SHARED_PREFS_CHOREAPP";

    public static String IS_LOGGED_IN = "IS_LOGGED_IN";

    public static String TASK_FILTER_ALL = "All";

    public static String GROUP_PAGE = "group";
    public static String TASKS_PAGE = "tasks";
    public static String MESSAGES_PAGE = "messages";
    public static String ACCOUNT_PAGE = "account";

    public static String[] REASSIGN_INTERVAL = { "Daily", "Weekly", "Monthly", "Yearly", "Never" };
}
