package jeff.persistent.model.mysql.dao;

import jeff.persistent.model.mysql.po.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberDAO extends JpaRepository<Member, Integer> {


}
