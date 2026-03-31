package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.exception.custom.PayLackException;
import eureca.capstone.project.orchestrator.pay.repository.UserPayRepository;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPayServiceImplConcurrencyTest {

    private UserPayRepository userPayRepository;
    private UserRepository userRepository;
    private UserPayServiceImpl userPayService;

    private User user;
    private AtomicLong storedPay;

    @BeforeEach
    void setUp() {
        userPayRepository = mock(UserPayRepository.class);
        userRepository = mock(UserRepository.class);
        userPayService = new UserPayServiceImpl(userPayRepository, userRepository);

        user = mock(User.class, RETURNS_DEEP_STUBS);
        when(user.getUserId()).thenReturn(1L);

        storedPay = new AtomicLong(10_000L);

        when(userPayRepository.decreasePayIfEnough(1L, 8_000L)).thenAnswer(invocation -> {
            synchronized (storedPay) {
                if (storedPay.get() < 8_000L) {
                    return 0;
                }
                storedPay.addAndGet(-8_000L);
                return 1;
            }
        });
    }

    @Test
    @DisplayName("동시에 같은 사용자가 열 번 차감하면 한 건만 성공해야 한다")
    void 동시에_같은_사용자가_열_번_차감하면_한_건만_성공해야_한다() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch ready = new CountDownLatch(10);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(10);

        AtomicInteger successCount = new AtomicInteger();
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

        Runnable task = () -> {
            ready.countDown();
            awaitLatch(start);
            try {
                userPayService.usePay(user, 8_000L);
                successCount.incrementAndGet();
            } catch (Throwable throwable) {
                failures.add(throwable);
            } finally {
                done.countDown();
            }
        };

        for (int i = 0; i < 10; i++) {
            executor.submit(task);
        }

        assertThat(ready.await(1, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        assertThat(done.await(3, TimeUnit.SECONDS)).isTrue();
        executor.shutdownNow();

        assertThat(successCount.get())
                .as("동시 차감에서는 정확히 1건만 성공해야 한다")
                .isEqualTo(1);

        assertThat(failures)
                .as("나머지 9건은 잔액 부족으로 실패해야 한다")
                .hasSize(9);

        assertThat(failures.get(0))
                .isInstanceOf(PayLackException.class);

        assertThat(storedPay.get())
                .as("최종 잔액은 한 번만 차감된 2,000원이어야 한다")
                .isEqualTo(2_000L);

        verify(userPayRepository, times(10)).decreasePayIfEnough(1L, 8_000L);
        verify(userPayRepository, never()).save(any());
    }

    private static void awaitLatch(CountDownLatch latch) {
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("동시 실행 시작 래치를 기다리는 데 실패했습니다.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}