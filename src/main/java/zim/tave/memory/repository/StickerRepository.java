package zim.tave.memory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zim.tave.memory.domain.Sticker;

public interface StickerRepository extends JpaRepository<Sticker, Long> {
}
