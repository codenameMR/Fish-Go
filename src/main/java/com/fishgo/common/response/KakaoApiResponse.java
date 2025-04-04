package com.fishgo.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class KakaoApiResponse {
    private long id;

    @JsonProperty("connected_at")  // JSON에서는 "connected_at"으로 내려오지만, 필드는 "connectedAt"으로 매핑
    private String connectedAt;

    private Map<String, Object> properties;

    @JsonProperty("kakao_account")
    private Map<String, Object> kakaoAccount;
}
