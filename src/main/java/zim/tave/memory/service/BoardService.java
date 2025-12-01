package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.*;
import zim.tave.memory.dto.request.BoardCreateRequestDto;
import zim.tave.memory.dto.request.BoardUpdateRequestDto;
import zim.tave.memory.dto.request.StickerPositionUpdateRequestDto;
import zim.tave.memory.dto.response.*;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardThemeRepository boardThemeRepository;
    private final UserRepository userRepository;
    private final BoardStickerMapRepository boardStickerMapRepository;
    private final StickerRepository stickerRepository;

    @Transactional
    public BoardCreateResponseDto createBoard(Long userId, BoardCreateRequestDto requestDto) {

        if (requestDto.getTitle() == null || requestDto.getBoardThemeId() == null) {
            throw new CustomException(ErrorCode.BOARD_REQUIRED_FIELDS_MISSING);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        BoardTheme theme = boardThemeRepository.findById(requestDto.getBoardThemeId())
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_THEME_NOT_FOUND));

        Board board = new Board();
        board.setUser(user);
        board.setBoardTheme(theme);
        board.setTitle(requestDto.getTitle());
        board.setCreatedAt(LocalDateTime.now());
        board.setUpdatedAt(LocalDateTime.now());

        Board saved = boardRepository.save(board);

        return BoardCreateResponseDto.from(saved);
    }

    public BoardListResponseDto getUserBoards(Long userId) {

        List<Board> boards = boardRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<BoardInformResponseDto> result = boards.stream()
                .map(board -> {

                    List<BoardStickerMap> stickerMaps =
                            boardStickerMapRepository.findByBoard(board);

                    List<String> stickerUrls = stickerMaps.stream()
                            .map(map -> map.getSticker().getImageUrl())
                            .toList();

                    return BoardInformResponseDto.from(board, stickerUrls);
                })
                .toList();

        return BoardListResponseDto.from(result);
    }

    @Transactional
    public void deleteBoard(Long userId, Long boardId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        // 권한 체크
        if (!board.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BOARD_DELETE_FORBIDDEN);
        }

        // 보드에 붙은 스티커 먼저 삭제
        boardStickerMapRepository.deleteByBoard(board);

        // 보드 삭제
        boardRepository.delete(board);
    }

    @Transactional
    public BoardUpdateResponseDto updateBoardStickers(
            Long userId, Long boardId, BoardUpdateRequestDto requestDto) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BOARD_UPDATE_FORBIDDEN);
        }

        // 수정시 기존 스티커 모두 삭제하고 다시 저장
        boardStickerMapRepository.deleteByBoard(board);

        List<BoardStickerMap> savedStickers = new ArrayList<>();

        for (BoardUpdateRequestDto.StickerUpdateItem item : requestDto.getStickers()) {

            Sticker sticker = stickerRepository.findById(item.getStickerId())
                    .orElseThrow(() -> new CustomException(ErrorCode.BOARD_STICKER_NOT_FOUND));

            BoardStickerMap map = new BoardStickerMap();
            map.setBoard(board);
            map.setSticker(sticker);

            map.setPosX(BigDecimal.valueOf(item.getPosX()));
            map.setPosY(BigDecimal.valueOf(item.getPosY()));
            map.setRotation(BigDecimal.valueOf(item.getRotation()));

            savedStickers.add(boardStickerMapRepository.save(map));
        }

        board.setUpdatedAt(LocalDateTime.now());
        boardRepository.save(board);

        return BoardUpdateResponseDto.from(board, savedStickers);
    }

    @Transactional
    public BoardStickerUpdateResponseDto updateSticker(
            Long userId, Long boardId, Long boardStickerId, StickerPositionUpdateRequestDto dto) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BOARD_UPDATE_FORBIDDEN);
        }

        BoardStickerMap map = boardStickerMapRepository.findById(boardStickerId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_STICKER_NOT_FOUND));

        // boardStickerId가 해당 보드의 것인지 확인
        if (!map.getBoard().getBoardId().equals(boardId)) {
            throw new CustomException(ErrorCode.BOARD_STICKER_NOT_FOUND);
        }

        // 위치 업데이트
        map.setPosX(BigDecimal.valueOf(dto.getPosX()));
        map.setPosY(BigDecimal.valueOf(dto.getPosY()));
        map.setRotation(BigDecimal.valueOf(dto.getRotation()));

        board.setUpdatedAt(LocalDateTime.now());

        return BoardStickerUpdateResponseDto.from(map);
    }
}
