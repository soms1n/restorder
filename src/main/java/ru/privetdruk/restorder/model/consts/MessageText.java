package ru.privetdruk.restorder.model.consts;

public interface MessageText {
    String ENTER_FULL_NAME = "Введите ваше ФИО:";
    String ENTER_NAME = "Введите ваше имя:";
    String ENTER_TAVERN_NAME = "Введите название заведения:";
    String ENTER_TAVERN_DESCRIPTION = "Введите описание заведения:";
    String CHOICE_CITY = "Выберите город:";
    String ENTER_ADDRESS = "Введите адрес без города:";
    String ENTER_PHONE_NUMBER = "Введите номер мобильного телефона в формате 89991110011:";
    String INCORRECT_ENTER_PHONE_NUMBER = "Вы ввели некорректный номер мобильного телефона. Повторите попытку.";
    String PHONE_NUMBER_DUPLICATE = "Указанный номер телефона уже добавлен. Повторите попытку.";
    String SHARE_PHONE_NUMBER = "Нажмите поделиться номером телефона.";
    String ENTER_EMPTY_VALUE = "Вы ввели пустое значение.";
    String ENTER_EMPTY_VALUE_RETRY = "Вы ввели пустое значение! Повторите попытку:";
    String WAITING_APPROVE_APPLICATION = "Спасибо за регистрацию, ваша заявка на модерации.";
    String REGISTER = "Добро пожаловать в <b>Restorder</b>! Пройдите регистрацию, чтобы получить доступ к функционалу бота.";
    String SELECT_ELEMENT_FOR_EDIT = "Выберите, что хотите отредактировать.";
    String INCORRECT_VALUE_TRY_AGAIN = "Выбрано некорректное значение. Повторите попытку.";
    String INCORRECT_VALUE_CANCELLED = "Вы ввели некорректное значение. Операция отменяется.";
    String GREETING = """
            Добро пожаловать!
            Я ваш персональный помощник по заведениям города.
            Я помогу вам быстро найти и забронировать столик в любом заведении города, чтобы вы и ваши друзья смогли хорошо провести время.
                        
            А теперь выберите или введите ваш город, чтобы продолжить.
            """;
    String CHOICE_TAVERN_TYPE = "Выберите тип заведения";
    String NOTIFY_USER_RESERVE_CANCELLED = "Ваша бронь %s %s в заведение %s завершена (отменена).";
    String NOTIFY_USER_BLOCK = "Вы добавлены в чёрный список в заведении ";
    String INCORRECT_PHONE_NUMBER = "Некорректный номер телефона. Повторите попытку.";
    String SUPPORT_MESSAGE = "Если у Вас возникли какие-то проблемы, можете задать вопрос в нашу поддержку https://t.me/restorder_support";
    String CLAIM_APPROVE_WAIT = "Ваша заявка на модерации, ожидайте";
    String SOMETHING_WENT_WRONG = "Что-то пошло не так...";
    String APPLICATION_ALREADY_CONFIRMED = "Заявка уже подтверждена";
    String APPLICATION_CONFIRMED = "Заявка подтверждена";
    String USER_NOT_FOUND = "Не удалось найти пользователя";
}
