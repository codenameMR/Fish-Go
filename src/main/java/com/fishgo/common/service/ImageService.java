package com.fishgo.common.service;

import com.fishgo.common.constants.UploadPaths;
import com.fishgo.users.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ProfileRepository profileRepository;

    /**
     * MultipartFile이 실제로 이미지인지 확인한다.
     *
     * @param files 이미지 여부를 확인할 대상 파일 리스트
     * @return 이미지이면 true, 아니면 false
     */
    public boolean isImageFile(List<MultipartFile> files) {

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                return false;
            }
            try {
                ImageIO.read(file.getInputStream());
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * MultipartFile이 실제로 이미지인지 확인한다.
     *
     * @param file 이미지 여부를 확인할 대상 파일
     * @return 이미지이면 true, 아니면 false
     */
    public boolean isImageFile(MultipartFile file) {

            if (file == null || file.isEmpty()) {
                return false;
            }
            try {
                ImageIO.read(file.getInputStream());
            } catch (IOException e) {
                return false;
            }
        return true;
    }

    /**
     * 게시글 이미지 업로드
     *
     * @param file 게시글 이미지
     * @param postId 게시글 아이디
     * @return 업로드 된 파일 이름
     */
    public String uploadPostImage(MultipartFile file, long postId) throws FileSystemException {
        String postDirectory = UploadPaths.POST.getPath() + postId;
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
        String userDirectory = UploadPaths.PROFILE.getPath() + userId + "/"; // uploads/profile/{userId}/
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


}
