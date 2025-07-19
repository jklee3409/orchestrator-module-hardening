package eureca.capstone.project.orchestrator.transaction_feed.repository;

import eureca.capstone.project.orchestrator.transaction_feed.entity.Liked;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.LikedRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikedRepository extends JpaRepository<Liked, Long>, LikedRepositoryCustom {
}
