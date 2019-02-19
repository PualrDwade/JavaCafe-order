package com.edu.xuanxuan;

import java.util.Random;

/**
 * 不同的咖啡类型品种
 */
public enum Coffee {
    MAOSHI,
    XIANGSHI,
    GOUSHI,
    NIUSHI;

    private static int random = (int) (Math.random() * 10);// 生成种子
    private static Random rand = new Random(random);

    public static <T extends Enum<T>> T random(Class<T> ec) {
        return random(ec.getEnumConstants());
    }

    public static <T> T random(T[] values) {
        return values[rand.nextInt(values.length)];
    }
}
