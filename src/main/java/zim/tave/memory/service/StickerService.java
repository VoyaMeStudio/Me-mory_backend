package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import zim.tave.memory.repository.StickerRepository;
import zim.tave.memory.repository.UserStickerRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class StickerService {

    private final StickerRepository stickerRepository;
    private final UserStickerRepository userStickerRepository;
    private final S3Client s3Client;
    private final RestTemplate restTemplate;


}
