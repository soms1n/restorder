package ru.privetdruk.restorder.model.consts;

public interface MessageText {
    String ENTER_FULL_NAME = "Введите ваше ФИО:";
    String ENTER_TAVERN_NAME = "Введите название заведения:";
    String CHOICE_CITY = "Выберите город:";
    String ENTER_ADDRESS = "Введите адрес:";
    String ENTER_PHONE_NUMBER = "Введите номер телефона:";
    String ENTER_EMPTY_VALUE = "Вы ввели пустое значение.";
    String CITY_IS_EMPTY = "Вы не выбрали город.";
    String WAITING_APPROVE_APPLICATION = "Спасибо за регистрацию, ваша заявка на модерации.";
    String REGISTER = "Пройдите регистрацию, чтобы получить доступ к функционалу бота.";
    String TAVERN_INVALID = "Добавьте в настройках хотя бы один стол и номер телефона, чтобы было доступно управление бронированием.";
    String SELECT_ELEMENT_FOR_EDIT = "Выберите, что хотите отредактировать.";
    String YOUR_CLAIM_WAS_APPROVED = "Ваша заявка подтверждена.";
    String ADMIN_APPROVED_CLAIM = "Заявка подтверждена, спасибо.";
    String INCORRECT_VALUE_TRY_AGAIN = "Выбрано некорректное значение. Повторите попытку.";
    String GREETING = """
            Добро пожаловать!
            Я твой персональный помощник по заведениям города.
            Я помогу тебе быстро найти и забронировать столик в любом заведении города, чтобы ты и твои друзья смогли хорошо провести время.
            Выберите город.
            """;
}
