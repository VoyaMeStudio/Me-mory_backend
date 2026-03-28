package zim.tave.memory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zim.tave.memory.domain.UserSticker;

import java.util.List;

public interface UserStickerRepository extends JpaRepository<UserSticker, Long> {

    List<UserSticker> findByUserId(Long userId);
}
