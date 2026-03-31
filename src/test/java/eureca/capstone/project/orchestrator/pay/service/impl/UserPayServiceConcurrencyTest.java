package eureca.capstone.project.orchestrator.pay.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.pay.repository.UserPayRepository;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserPayServiceConcurrencyTest {

    private UserRepository userRepository;
    private UserPayRepository userPayRepository;
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
    }

    @Test
    @DisplayName("동시에 같은 사용자가 열 번 차감하면 한 건만 성공해야 한다")
    void 동시에_같은_사용자가_열_번_차감하면_한_건만_성공해야_한다() throws Exception {
        // 2개의 스레드가 특정 지점에 도달할 때까지 대기
        CyclicBarrier readBarrier = new CyclicBarrier(2);

        when(userPayRepository.findById(1L)).thenAnswer(invocation -> {
            long snapshot = storedPay.get();
            awaitBarrier(readBarrier); // 동시에 잔액 조회하는 상황

            return Optional.of(
                    UserPay.builder()
                            .userId(1L)
                            .user(user)
                            .pay(snapshot)
                            .build()
            );
        });

        when(userPayRepository.save(any(UserPay.class))).thenAnswer(invocation -> {
            UserPay saved = invocation.getArgument(0); // == userPay, invocation = mock 메서드가 호출된 상황 정보
            storedPay.set(saved.getPay());
            return saved;
        });

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

        // 10개 스레드가 동시 실행
        executor.submit(task);
        executor.submit(task);
        executor.submit(task);
        executor.submit(task);
        executor.submit(task);
        executor.submit(task);
        executor.submit(task);
        executor.submit(task);
        executor.submit(task);
        executor.submit(task);

        assertThat(ready.await(1, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        assertThat(done.await(3, TimeUnit.SECONDS)).isTrue();
        executor.shutdownNow();

        assertThat(successCount.get())
                .as("동시 차감에서는 정확히 1건만 성공해야 한다")
                .isEqualTo(1);

        assertThat(failures)
                .as("나머지 1건은 잔액 부족 등으로 실패해야 한다")
                .hasSize(1);

        assertThat(storedPay.get())
                .as("최종 잔액은 한 번만 차감된 2,000원이어야 한다")
                .isEqualTo(2_000L);
    }

    private static void awaitBarrier(CyclicBarrier barrier) {
        try {
            barrier.await(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
