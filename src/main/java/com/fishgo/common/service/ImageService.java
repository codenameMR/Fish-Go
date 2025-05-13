package com.fishgo.common.service;

import com.fishgo.common.constants.UploadPaths;
import com.fishgo.users.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ProfileRepository profileRepository;

    @Value("${user.upload.path}")
    String uploadPath;

    /**
     * 게시글 이미지 업로드
     *
     * @param file 게시글 이미지
     * @param postId 게시글 아이디
     * @return 업로드 된 파일 이름
     */
    public String uploadPostImage(MultipartFile file, long postId) throws FileSystemException {
        String postDirectory = uploadPath + "posts/" + postId;
        // 디렉토리가 없으면 생성
        File directory = new File(postDirectory);
        if (!directory.exists()) {
            boolean wasMkdirSuccessful = directory.mkdirs();

            if(!wasMkdirSuccessful){
                throw new FileSystemException("게시글 디렉토리 생성 실패");
            }
        }
        return uploadFileToServer(file, postDirectory);
    }

    /**
     * 프로필 이미지 업로드 및 기존 이미지 삭제
     * @param file 프로필 이미지
     * @param userId 유저 아이디
     * @return 업로드 된 파일 이름
     */
    public String uploadProfileImage(MultipartFile file, long userId) {
        String userDirectory = uploadPath.replace("///","") + UploadPaths.PROFILE.getPath() + userId + "/"; // {uploadDir}/uploads/profile/{userId}/
        // 기존 프로필 이미지
        String oldFileName = profileRepository.findProfileImgByUserId(userId);

        // 기존 프로필이 있으면 시스템에서 삭제 처리
        if(oldFileName != null && !oldFileName.isEmpty()) {
            String oldFilePath = userDirectory + oldFileName;
            deleteOldImage(oldFilePath);

        }

        return uploadFileToServer(file, userDirectory);
    }

    /**
     * 서버에 파일 업로드 처리
     *
     * @param file 클라이언트에서 요청한 파일
     * @param userDirectory  디렉토리 경로
     * @return 파일명
     */
    private String uploadFileToServer(MultipartFile file, String userDirectory) {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(userDirectory, fileName);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }

    /**
     * 기존 이미지 삭제
     * @param imgPath 기존 이미지 경로
     */
    public void deleteOldImage(String imgPath) {
        if (imgPath != null && !imgPath.isEmpty()) {
            Path oldImagePath = Paths.get(imgPath);
            try {
                Files.deleteIfExists(oldImagePath);
            } catch (IOException e) {
                throw new RuntimeException("기존 이미지 삭제 실패", e);
            }
        }
    }

    public long countFilesInDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            return 0;
        }
        return Objects.requireNonNull(directory.listFiles()).length;
    }


}
