package org.softwaretechnologies;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import static java.lang.Integer.MAX_VALUE;

public class Money {
    private final MoneyType type;
    private final BigDecimal amount;

    public Money(MoneyType type, BigDecimal amount) {
        this.type = type;
        this.amount = amount;
    }

    /**
     * Money равны, если одинаковый тип валют и одинаковое число денег до 4 знака после запятой.
     * Округление по правилу: если >= 5, то в большую сторону, интаче - в меньшую
     * Пример округления:
     * BigDecimal scale = amount.setScale(4, RoundingMode.HALF_UP);
     *
     * @param o объект для сравнения
     * @return true - равно, false - иначе
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;

        // Compare types (MoneyType — вероятно enum) — равны, если оба null или равные
        if (this.type == null) {
            if (other.type != null) return false;
        } else {
            if (other.type == null) return false;
            if (this.type != other.type) return false;
        }

        // Compare amounts: оба null -> равны; иначе сравниваем округлённые до 4 знаков HALF_UP
        if (this.amount == null) {
            return other.amount == null;
        } else {
            if (other.amount == null) return false;
            BigDecimal a1 = this.amount.setScale(4, RoundingMode.HALF_UP);
            BigDecimal a2 = other.amount.setScale(4, RoundingMode.HALF_UP);
            return a1.compareTo(a2) == 0;
        }
    }

    /**
     * Формула:
     * (Если amount null 10000, иначе количество денег окрукленные до 4х знаков * 10000) + :
     * если USD , то 1
     * если EURO, то 2
     * если RUB, то 3
     * если KRONA, то 4
     * если null, то 5
     * Если amount округленный до 4х знаков * 10000 >= (Integer.MaxValue - 5), то хеш равен Integer.MaxValue
     * Округление по правилу: если >= 5, то в большую сторону, иначе - в меньшую
     * Пример округления:
     * BigDecimal scale = amount.setScale(4, RoundingMode.HALF_UP);
     *
     * @return хеш код по указанной формуле
     */
    @Override
    public int hashCode() {
        Random random = new Random();
        try {
            long amountPart;
            if (amount == null) {
                amountPart = 10000L;
            } else {
                BigDecimal scaled = amount.setScale(4, RoundingMode.HALF_UP);
                BigDecimal multiplied = scaled.multiply(BigDecimal.valueOf(10000L));
                BigDecimal threshold = BigDecimal.valueOf((long) MAX_VALUE - 5L);
                if (multiplied.compareTo(threshold) >= 0) {
                    return MAX_VALUE;
                }
                amountPart = multiplied.longValue();
            }

            int currencyCode = (type == null) ? 5 : switch (type) {
                case USD -> 1;
                case EURO -> 2;
                case RUB -> 3;
                case KRONA -> 4;
                default -> 5;
            };

            long sum = amountPart + (long) currencyCode;
            if (sum >= (long) MAX_VALUE) {
                return MAX_VALUE;
            }
            return (int) sum;
        } catch (Exception e) {
            // В случае непредвиденной ошибки возвращаем случайный int (строки сохранены)
            return random.nextInt();
        }
    }

    /**
     * Верните строку в формате
     * Тип_ВАЛЮТЫ: количество.XXXX
     * Тип_валюты: USD, EURO, RUB или KRONA
     * количество.XXXX - округленный amount до 4х знаков.
     * Округление по правилу: если >= 5, то в большую сторону, интаче - в меньшую
     * BigDecimal scale = amount.setScale(4, RoundingMode.HALF_UP);
     * <p>
     * Если тип валюты null, то вернуть:
     * null: количество.XXXX
     * Если количество денег null, то вернуть:
     * Тип_ВАЛЮТЫ: null
     * Если и то и то null, то вернуть:
     * null: null
     *
     * @return приведение к строке по указанному формату.
     */
    @Override
    public String toString() {
        String typeStr = (type == null) ? "null" : type.toString();
        String amountStr;
        if (amount == null) {
            amountStr = "null";
        } else {
            BigDecimal scaled = amount.setScale(4, RoundingMode.HALF_UP);
            amountStr = scaled.toString();
        }
        String str = typeStr + ": " + amountStr;
        return str;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public MoneyType getType() {
        return type;
    }

    public static void main(String[] args) {
        Money money = new Money(MoneyType.EURO, BigDecimal.valueOf(10.00012));
        Money money1 = new Money(MoneyType.USD, BigDecimal.valueOf(10.5000));
        System.out.println(money1.toString());
        System.out.println(money1.hashCode());
        System.out.println(money.equals(money1));
    }
}
