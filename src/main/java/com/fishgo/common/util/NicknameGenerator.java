package com.fishgo.common.util;

import java.util.Random;

public class NicknameGenerator {

    private static final Random RANDOM = new Random();

    private static final String[] FISHES = {
            "고등어", "가자미", "자리돔", "말쥐치", "쏘가리",
            "동갈치", "학꽁치", "개서대", "벵에돔", "쑤기미",
            "망상어", "백조기", "달고기", "개복치", "산천어",
            "가물치", "놀래기", "정어리", "청새치", "노래미"
    };


    /**
     * FISHES[] + #0000 (4자리) 형식으로 닉네임을 생성합니다.
     */
    public static String generateNickname() {
        String fish = FISHES[RANDOM.nextInt(FISHES.length)];

        int number = RANDOM.nextInt(10000);
        return String.format(fish + "#%04d", number);
    }

}
