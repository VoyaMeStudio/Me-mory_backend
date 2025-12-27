package zim.tave.memory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zim.tave.memory.domain.Board;
import zim.tave.memory.domain.BoardStickerMap;

import java.util.List;

public interface BoardStickerMapRepository extends JpaRepository<BoardStickerMap, Long> {

    List<BoardStickerMap> findByBoard(Board board);
    List<BoardStickerMap> findAllByBoardIn(List<Board> boards);
    void deleteByBoard(Board board);
}
