package shinstyle.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shinstyle.userservice.entity.User;
import shinstyle.userservice.entity.UserLoginHistory;

import java.util.List;

@Repository
public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, Long> {

    List<UserLoginHistory> findByUserOrderByLoginTimeDesc(User user);
}
