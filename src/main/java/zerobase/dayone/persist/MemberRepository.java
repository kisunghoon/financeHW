package zerobase.dayone.persist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.dayone.persist.entity.MemberEntity;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity , Long> {

    Optional<MemberEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
