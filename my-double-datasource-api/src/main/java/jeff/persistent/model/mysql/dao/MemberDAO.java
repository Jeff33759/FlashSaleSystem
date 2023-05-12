package jeff.persistent.model.mysql.dao;

import jeff.persistent.model.mysql.po.Members;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberDAO extends JpaRepository<Members, Integer> {


}
