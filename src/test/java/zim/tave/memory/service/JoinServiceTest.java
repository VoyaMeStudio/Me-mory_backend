package zim.tave.memory.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.request.JoinRequestDto;
import zim.tave.memory.repository.UserRepository;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class JoinServiceTest {

    @Autowired
    private JoinService joinService;

    @Autowired
    private UserRepository userRepository;


}
