package eureca.capstone.project.orchestrator.market_statistics.repository;

import eureca.capstone.project.orchestrator.market_statistics.entity.MarketStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MarketStatisticsRepository extends JpaRepository<MarketStatistic, Long> {
    @Query("select ms from MarketStatistic ms join fetch ms.telecomCompany tc " +
            "where ms.staticsTime >= :from and ms.staticsTime < :to " +
            "order by ms.staticsTime asc")
    List<MarketStatistic> findAllByStaticsTimeRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
