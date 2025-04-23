package com.fishgo.common.util;

import com.fishgo.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

@Slf4j
public class ImageValidator {

    private static final Tika tika = new Tika();

    // 허용할 MIME 타입 화이트리스트
    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    // 허용할 확장자
    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    public static void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }
        try {
            String originalFileName = file.getOriginalFilename();
            String mimeType = tika.detect(file.getInputStream());

            // 1. MIME 타입 화이트리스트 확인
            if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
                throw new IllegalArgumentException("지원하지 않는 이미지 타입입니다: " + mimeType);
            }

            // 2. 확장자 검증 (선택적)
            if (originalFileName != null &&
                    ALLOWED_EXTENSIONS.stream().noneMatch(originalFileName.toLowerCase()::endsWith)) {
                throw new IllegalArgumentException("허용되지 않은 확장자입니다.");
            }

            // 3. 실제 이미지 파일인지 확인
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("손상된 이미지거나 유효하지 않은 이미지입니다.");
            }
        } catch (IOException e) {
            log.error("ImageValidator 에서 이미지 읽는 도중 IOException : {}", e.getMessage(), e);
            throw new CustomException(999999, "이미지 읽는 도중 오류 발생");
        }

    }
}

