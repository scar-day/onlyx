package net.polix.system

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

var startTime: Long = 0

/*
    Я просто пишу свое дерьмо
    Я свободный человек пасщу что хочу
    Бро я пастил онлИкс уже в три лет
    Не ври что ты кодер это пащеный клиент
    Ишутов модель пузо размером с бургЕр
    Ты нервничаешь бесишь сам себя когда пастинга нет
    У у у у у я паста ботов писатель,сам себя уничтожитель, бургеров съедатель,
    Гелий но во нем ишутинг ты там привыкатель
    Бро это селф кодинг я им писатель
    Большие пинги,статы,добы,я их пастер
    У меня большие боты и я селф кодинг
    Бро реально ворует зовет не пастер
    Боты возьми из 10 говно и их пасщу утром
*/

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
class PolixApplication

fun main(args: Array<String>) {
    System.setProperty("file.encoding", "UTF-8")
    System.getProperty("user.timezone", "Europe/Ulyanovsk")

    startTime = System.currentTimeMillis()
    runApplication<PolixApplication>(*args)

}
