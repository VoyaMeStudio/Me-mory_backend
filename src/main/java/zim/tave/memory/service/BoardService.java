package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.Board;
import zim.tave.memory.domain.BoardTheme;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.request.BoardCreateRequestDto;
import zim.tave.memory.dto.response.BoardCreateResponseDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.BoardRepository;
import zim.tave.memory.repository.BoardThemeRepository;
import zim.tave.memory.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardThemeRepository boardThemeRepository;
    private final UserRepository userRepository;

    @Transactional
    public BoardCreateResponseDto createBoard(Long userId, BoardCreateRequestDto requestDto) {

        if (requestDto.getTitle() == null || requestDto.getBoardThemeId() == null) {
            throw new CustomException(ErrorCode.BOARD_REQUIRED_FIELDS_MISSING);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        BoardTheme theme = boardThemeRepository.findById(requestDto.getBoardThemeId())
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_THEME_NOT_FOUND));

        // 보드 생성
        Board board = new Board();
        board.setUser(user);
        board.setTitle(requestDto.getTitle());
        board.setBoardTheme(theme);
        board.setCreatedAt(LocalDateTime.now());
        board.setUpdatedAt(LocalDateTime.now());

        Board saved = boardRepository.save(board);

        return BoardCreateResponseDto.from(saved);
    }
}
