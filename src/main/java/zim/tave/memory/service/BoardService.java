package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.*;
import zim.tave.memory.dto.request.BoardCreateRequestDto;
import zim.tave.memory.dto.request.BoardStickerAddRequestDto;
import zim.tave.memory.dto.request.BoardUpdateRequestDto;
import zim.tave.memory.dto.request.StickerPositionUpdateRequestDto;
import zim.tave.memory.dto.response.*;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        // createdAt과 updatedAt은 @PrePersist, @PreUpdate에서 자동 설정됨

        Board saved = boardRepository.save(board);

        return BoardCreateResponseDto.from(saved);
    }

    // 유저 전체 보드 리스트 조회
    public BoardListResponseDto getUserBoards(Long userId) {

        List<Board> boards = boardRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<BoardStickerMap> allStickerMaps = boardStickerMapRepository.findAllByBoardIn(boards);

        Map<Long, List<BoardStickerMap>> stickerMapByBoardId =
                allStickerMaps.stream()
                        .collect(Collectors.groupingBy(
                                map -> map.getBoard().getBoardId()
                        ));

        List<BoardInformResponseDto> result = boards.stream()
                .map(board -> {

                    List<BoardStickerMap> stickerMaps =
                            stickerMapByBoardId.getOrDefault(
                                    board.getBoardId(),
                                    List.of()
                            );

                    List<String> stickerUrls = stickerMaps.stream()
                            .map(map -> map.getSticker().getImageUrl())
                            .toList();

                    return BoardInformResponseDto.from(board, stickerUrls);
                })
                .toList();

        return BoardListResponseDto.from(result);
    }

    //특정 보드 상세 정보 조회
    public BoardDetailResponseDto getBoardDetail(Long userId, Long boardId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BOARD_ACCESS_FORBIDDEN);
        }

        List<BoardStickerMap> stickerMaps = boardStickerMapRepository.findByBoard(board);

        return BoardDetailResponseDto.from(board, stickerMaps);
    }

    // 보드 삭제
    @Transactional
    public void deleteBoard(Long userId, Long boardId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BOARD_DELETE_FORBIDDEN);
        }

        // 보드에 붙은 스티커 먼저 삭제
        boardStickerMapRepository.deleteByBoard(board);

        // 보드 삭제
        boardRepository.delete(board);
    }

    //보드에 스티커 붙이기
    @Transactional
    public BoardStickerAddResponseDto addSticker(
            Long userId, Long boardId,
            BoardStickerAddRequestDto dto) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BOARD_UPDATE_FORBIDDEN);
        }

        // Sticker 존재 여부 확인
        Sticker sticker = stickerRepository.findById(dto.getStickerId())
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_STICKER_NOT_FOUND));

        BoardStickerMap map = new BoardStickerMap();
        map.setBoard(board);
        map.setSticker(sticker);
        map.setPosX(BigDecimal.valueOf(dto.getPosX()));
        map.setPosY(BigDecimal.valueOf(dto.getPosY()));
        map.setRotation(BigDecimal.valueOf(dto.getRotation()));

        BoardStickerMap saved = boardStickerMapRepository.save(map);

        // 보드 업데이트 시간 갱신
        board.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        return BoardStickerAddResponseDto.from(saved);
    }

    // 보드에서 스티커 위치 수정
    @Transactional
    public BoardStickerUpdateResponseDto updateSticker(
            Long userId,
            Long boardId,
            Long boardStickerId,
            StickerPositionUpdateRequestDto dto) {

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

        board.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        return BoardStickerUpdateResponseDto.from(map);
    }

    // 보드에서 스티커 삭제
    @Transactional
    public void deleteStickerFromBoard(Long userId, Long boardId, Long boardStickerId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BOARD_UPDATE_FORBIDDEN);
        }

        BoardStickerMap map = boardStickerMapRepository.findById(boardStickerId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_STICKER_MAP_NOT_FOUND));

        // 해당 스티커가 정말 이 보드에 속하는지 체크
        if (!map.getBoard().getBoardId().equals(boardId)) {
            throw new CustomException(ErrorCode.BOARD_STICKER_MAP_NOT_FOUND);
        }

        boardStickerMapRepository.delete(map);

        board.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

}
