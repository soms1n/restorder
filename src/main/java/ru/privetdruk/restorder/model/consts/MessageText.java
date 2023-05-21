package ru.privetdruk.restorder.model.consts;

import static java.lang.System.lineSeparator;

public interface MessageText {
    String ENTER_FULL_NAME = "Введите ваше ФИО:";
    String ENTER_YOUR_NAME = "Введите ваше имя:";
    String ENTER_NAME = "Введите имя:";
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
    String NOTIFY_USER_BLOCK = """
            Вы добавлены в чёрный список
            Заведение: %s
            Причина: %s
            Дата блокировки: %s
            Дата снятия блокировки: %s
            """;
    String INCORRECT_PHONE_NUMBER = "Некорректный номер телефона. Повторите попытку.";
    String SUPPORT_MESSAGE = "Если у Вас возникли какие-то проблемы, можете задать вопрос в нашу поддержку https://t.me/restorder_support";
    String USER_NOT_FOUND = "Не удалось найти пользователя.";
    String APPLICATION_ALREADY_APPROVED = "Заявка уже подтверждена.";
    String HI_APPLICATION_APPROVED = "Добрый день. Ваша заявка подтверждена. Приятной работы!";
    String APPLICATION_APPROVED = "Заявка подтверждена.";
    String UNEXPECTED_ERROR = "Произошла непредвиденная ошибка. Обратитесь в поддержку.";
    String LINK_NOT_AVAILABLE = "Данная ссылка больше недоступна.";
    String LINK_IS_INVALID = "Некорректная ссылка";
    String YOU_ALREADY_HAVE_TAVERN = "Вы уже является владельцем/сотрудником другого заведения: %s . Удалите в настройках ваш профиль и попробуйте повторно перейти по ссылке.";
    String OPEN_MENU = "Открываю главное меню.";
    String CLAIM_APPROVE_WAIT = "Ваша заявка на модерации, ожидайте.";
    String TAVERN_SET_UP = "Чтобы воспользоваться бронированием, выполните настройку заведения:" + lineSeparator();
    String CHOICE_RESERVE_FOR_CONFIRM = "Выберите бронирование для завершения.";
    String ALL_RESERVES_WILL_BE_CONFIRM = "Все бронирования были завершены." + lineSeparator() + lineSeparator();
    String IS_CLIENT_HERE = "Клиент пришёл?";
    String DOES_BLOCK_USER = "Клиент не пришел по бронированию уже %s %s. Заблокировать его?";
    String INCORRECT_DATE_RETRY = "Введенная дата не соответствует формату. Пример - 05092022 <i>(5 сентября 2022 года)</i>. Повторите попытку:";
    String INCORRECT_TIME_RETRY = "Введенное время не соответствует формату. Пример - 0305 <i>(3 часа 5 минут)</i>. Повторите попытку:";
    String MANUAL_OR_AUTO = "Хотите сами выбрать место или мы подберем его автоматически?";
    String INCORRECT_MORE_DATE_RETRY = "Дата бронирования должна быть больше, либо равна текущей дате. Повторите попытку:";
    String INCORRECT_MORE_TIME_RETRY = "Время бронирования должно быть больше, либо равно текущему времени.";
    String RESERVE_TABLE_SUCCESS = "Столик забронирован.";
    String RESERVE_TABLE_ERROR = "Столик не удалось забронировать.";
    String RESERVE_CONFIRMED = "Выбранное бронирование завершено." + lineSeparator() + lineSeparator();
    String RESERVES_NOT_FOUND = "У Вас нет бронирований за выбранную дату.";
    String CHOICE_RESERVE_DATE_FOR_CONFIRMED = "Выберите дату, за которую хотите завершить бронирование.";
    String ENTER_RESERVE_DATE = "Введите дату в формате ДДММГГГГ <i>(пример: если хотите забронировать 24.05.2022, то введите 24052022)</i>:";
    String ENTER_RESERVE_TIME = "Введите время в формате ЧЧММ <i>(пример: если хотите забронировать в 17:48, то введите 1748)</i>:";
    String ENTER_OR_CHOICE_PERSONS = "Введите или выберите кол-во персон:";
    String ACTIVE_RESERVES_NOT_FOUND = "У Вас нет активных бронирований.";
    String TAVERN_IS_NOT_WORKING = "В указанное время заведение не работает. Введите другое время:";
    String RESERVES_EMPTY = "Список бронирований пуст.";
    String REMOVE_DATA_SUCCESS = "Данные успешно удалены. Хорошего дня!";
    String ENTER_NEW_TAVERN_NAME = "Введите новое название:";
    String ENTER_NEW_TAVERN_DESCRIPTION = "Введите новое описание:";
    String ENTER_NEW_TAVERN_LINK = "Введите новую ссылку:";
    String ENTER_NUMBER_PERSONS = "Введите кол-во персон:";
    String ENTER_NEW_TAVERN_ADDRESS = "Введите новый адрес:";
    String CHOICE_NEW_TAVERN_CATEGORY = "Выберите новую категорию.";
    String RESERVE_CANCEL = "Вы отменили бронирование.";
    String RESERVE_SUCCESS = "Вы успешно забронировали столик.";
    String CANCEL_OPERATION_RETURN_TO_MENU = "Операция отменена. Возврат в главное меню.";

    String ENTER_NEW_NAME = "Введите новое имя:";
    String PHONE_IS_BLOCKED = "Номер заблокирован.";
    String PHONE_IS_UNBLOCKED = "Номер разблокирован.";
    String ENTER_BLACKLIST_REASON = "Введите причину блокировки или выберите в меню.";
    String EMPLOYEE_REGISTRATION = """
            Регистрация по ссылке доступна в течении одного часа и только для одного человека.
            Перешлите данное сообщение Вашему сотруднику.
                        
            <a href="https://t.me/%s?start=%s">> РЕГИСТРАЦИЯ</a>""";
    String CHOICE_DAY_OF_WEEK = "Выберите день недели.";
    String ENTER_LABEL = "Введите метку стола:";
    String ENTER_OR_CHOICE_LABEL = "Введите метку стола или выберите нужный в меню:";
    String NOTHING_DELETE = "Нечего удалять.";
    String NOTHING_CHOICE_CANCEL = "Вы ни кого не выбрали! Операция отменяется.";
    String NOTHING_PERSONS_CHOICE_CANCEL = "Вы ничего не выбрали! Операция отменяется.";
    String YOU_CANT_DELETE_YOURSELF = "Себя нельзя удалить.";
    String CONFIRM_DELETE_LINK = "Вы действительно хотите удалить ссылку?";
    String ACCESS_DENIED = "Выбранный пункт меню недоступен вашей роли.";
    String YOU_DONT_CHOICE_NUMBER = "Вы не выбрали номер. Операция отменяется.";
    String YOU_DONT_CHOICE_TAVERN = "Вы не выбрали заведение. Операция отменяется.";
    String CANT_MAKE_RESERVATION = "Нельзя забронировать столик в выбранном заведении.";
    String DO_YOU_WANT_TO_REMOVE_PROFILE = "Вы действительно хотите удалить профиль?";
    String DO_YOU_WANT_TO_REMOVE_PROFILE_TAVERN = "Вы является владельцем заведения. Вместе с вашим профилем будет удалено и заведение. Продолжить удаление?";
    String CYRILLIC_INVALID = "Имя должно содержать только символы кириллицы. Повторите попытку.";
    String FOUND_MANY_CONTACTS = "Найдено больше одного номера! Операция отменяется. Обратитесь в поддержку.";
    String ENTER_TIMES = "Введите кол-во раз, когда человек не пришел в заведение (0 отключить), чтобы автоматически его заблокировать:";
    String ENTER_DAYS = "Введите кол-во дней блокировки (0 навсегда). По истечению установленного кол-ва дней, блокировка будет автоматически снята.";
    String BLACKLIST_NOT_FOUND = "Блокировка для указанного номера не найдена.";
    String BLACKLIST_DUPLICATE = "Указанный номер уже есть в чёрном списке вашего заведения." + lineSeparator();
    String CHOICE_START_HOUR = "Выберите час начала периода.";
    String CHOICE_END_HOUR = "Выберите час окончания периода.";
    String CHOICE_START_MINUTE = "Выберите минуту начала периода.";
    String CHOICE_END_MINUTE = "Выберите минуту окончания периода.";
    String ENTER_PRICE = "Введите стоимость входа:";
    String ENTER_NUMBER_SEATS = "Введите кол-во мест:";
    String TABLE_IS_DUPLICATE = "Стол с указанным маркером уже существует." + lineSeparator() + lineSeparator();
    String OPEN_ALL_SETTINGS = "Открываю все настройки.";
    String OPEN_BLACKLIST = "Открываю управление блокировками.";
    String LINK_IS_DELETED = "Ссылка успешно удалена.";
    String CHOICE_PHONE_FOR_DELETE = "Выберите номер телефона, который хотите удалить.";
    String BLACKLIST_IS_EMPTY = "Нет заблокированных номеров.";
    String ENTER_CHOICE_PHONE_INFO = "Выберите или введите номер телефона для получения дополнительной информации.";
    String THERES_NO_ONE_TO_REMOVE = "Некого удалять.";
    String CHOICE_EMPLOYEE_TO_DELETE = "Выберите сотрудника, которого хотите удалить.";
    String CHOICE_TABLE_TO_DELETE = "Выберите стол, который хотите удалить.";
    String CHOICE_ENTRY_TO_DELETE = "Выберите запись, которую хотите удалить.";
    String CHOICE_TABLE_WITH_LINK = "Выберите стол." + lineSeparator() + lineSeparator() + "Схема расположения столов: [\u200B](%s)";
    String TABLE_FREE_UP = "В указанное вами время столик свободен до %s. Данный столик нужно будет освободить не позднее %s." +
            " Подтверждаете бронирование?";
    String TABLE_NOT_FOUND = "К сожалению не удалось найти подходящий столик на %s %s в выбранную дату и время.";
    String CHOICE_TAVERN = "Выберите заведение.";
    String SOMETHING_WENT_WRONG = "Что-то пошло не так...";
}
