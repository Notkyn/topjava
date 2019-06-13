package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExceed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Инструменты для работы с едой
 */
public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> mealList = Arrays.asList(
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31, 20, 0), "Ужин", 510)
        );
        System.out.println("*** Test 1: Sycle ***");
        print(getFilteredWithExceeded(mealList, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
        System.out.println("*** Test 2: Stream ***");
        print(getFilteredWithExceededOfStreams(mealList, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
        System.out.println("*** Test 3: One Sycle ***");
        print(getFilteredWithExceededOneCycle(mealList, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
//        .toLocalDate();
//        .toLocalTime();
    }

    /**
     * Фильтрация еды
     *
     * @param mealList       - список еды
     * @param startTime      - начало периода
     * @param endTime        - конец периода
     * @param caloriesPerDay - калорий за день
     * @return - отфильтрованый список еды
     */
    public static List<UserMealWithExceed> getFilteredWithExceeded(List<UserMeal> mealList, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        List<UserMealWithExceed> filterMealList = new ArrayList<>();
        Map<LocalDate, Integer> sumCalories = new HashMap<>();
        for (UserMeal userMeal : mealList) {
            int tempSum = userMeal.getCalories();
            if (sumCalories.containsKey(userMeal.getDateTime().toLocalDate())) {
                tempSum += sumCalories.get(userMeal.getDateTime().toLocalDate());
            }
            sumCalories.put(userMeal.getDateTime().toLocalDate(), tempSum);
        }
        for (UserMeal userMeal : mealList) {
            if (TimeUtil.isBetween(userMeal.getDateTime().toLocalTime(), startTime, endTime)) {
                boolean tempExced = sumCalories.get(userMeal.getDateTime().toLocalDate()) > caloriesPerDay;
                UserMealWithExceed userMealWithExceed = new UserMealWithExceed(
                        userMeal.getDateTime(),
                        userMeal.getDescription(),
                        userMeal.getCalories(),
                        tempExced);
                filterMealList.add(userMealWithExceed);
            }
        }
        return filterMealList;
    }

    public static List<UserMealWithExceed> getFilteredWithExceededOfStreams(List<UserMeal> mealList, LocalTime startTime, LocalTime endTime, int caloriesPerDay){
        Map<LocalDate, Integer> sumCalories = mealList
                .stream()
                .collect(Collectors.groupingBy(
                        (p) -> p.getDateTime().toLocalDate(),
                        Collectors.summingInt(UserMeal::getCalories)));

        return mealList.stream()
                .filter(p -> TimeUtil.isBetween(p.getDateTime().toLocalTime(), startTime, endTime))
                .map(p -> new UserMealWithExceed(p.getDateTime(),
                        p.getDescription(),
                        p.getCalories(),
                        caloriesPerDay < sumCalories.get(p.getDateTime().toLocalDate())))
                .collect(Collectors.toList());
    }

    public static List<UserMealWithExceed> getFilteredWithExceededOneCycle(List<UserMeal> mealList, LocalTime startTime, LocalTime endTime, int caloriesPerDay){
        mealList.sort(new Comparator<UserMeal>() {
            @Override
            public int compare(UserMeal o1, UserMeal o2) {
                return o1.getDateTime().toLocalDate().compareTo(o2.getDateTime().toLocalDate());
            }
        });

        LocalDate tempLocalDate = null;
        int tempCalories = 0;
        List<UserMealWithExceed> mealsTrue = new ArrayList<>();
        List<UserMealWithExceed> mealsFalse = new ArrayList<>();
        List<UserMealWithExceed> result = new ArrayList<>();

        for(UserMeal userMeal : mealList){
            if(tempLocalDate == null){
                tempLocalDate = userMeal.getDateTime().toLocalDate();
            }

            if(tempLocalDate.equals(userMeal.getDateTime().toLocalDate())){
                tempCalories += userMeal.getCalories();
            } else {
                if(tempCalories > caloriesPerDay){
                    result.addAll(mealsTrue);
                } else {
                    result.addAll(mealsFalse);
                }
                mealsTrue.clear();
                mealsFalse.clear();
                tempCalories = userMeal.getCalories();
                tempLocalDate = userMeal.getDateTime().toLocalDate();
            }

            if(TimeUtil.isBetween(userMeal.getDateTime().toLocalTime(), startTime, endTime)){
                UserMealWithExceed userMealWithExceedFalse = new UserMealWithExceed(
                        userMeal.getDateTime(),
                        userMeal.getDescription(),
                        userMeal.getCalories(),
                        false);
                mealsFalse.add(userMealWithExceedFalse);
                UserMealWithExceed userMealWithExceedTrue = new UserMealWithExceed(
                        userMeal.getDateTime(),
                        userMeal.getDescription(),
                        userMeal.getCalories(),
                        true);
                mealsTrue.add(userMealWithExceedTrue);
            }
        }
        if(tempCalories > caloriesPerDay){
            result.addAll(mealsTrue);
        } else {
            result.addAll(mealsFalse);
        }

        return result;
    }

    private static void print(List<UserMealWithExceed> list) {
        for (UserMealWithExceed meal : list) {
            System.out.println(meal.toString());
        }
    }
}
