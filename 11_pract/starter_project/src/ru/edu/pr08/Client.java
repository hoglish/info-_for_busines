package ru.edu.pr08;

/** Принятая карточка клиента. Поле website добавлено вариантом 26 (часть А). */
public record Client(String inn, String name, String email, String website) {}
