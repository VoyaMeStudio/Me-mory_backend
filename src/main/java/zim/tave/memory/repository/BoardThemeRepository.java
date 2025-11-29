package zim.tave.memory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zim.tave.memory.domain.BoardTheme;

public interface BoardThemeRepository extends JpaRepository<BoardTheme, Long> {
}
