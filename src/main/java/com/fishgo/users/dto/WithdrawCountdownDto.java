package com.fishgo.users.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawCountdownDto {
    private long days;
    private long hours;
    private long minutes;
}
