package ru.edu.pr09;

/** Продажа. Иммутабельный носитель данных. */
public record Sale(String id, String manager, String region, double amount) {}
