package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.accounting;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.Account;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByCode(String code);
    
    List<Account> findByActiveTrue();
    
    List<Account> findByTypeAndActiveTrue(AccountType type);
    
    @Query("SELECT a FROM Account a WHERE a.active = true ORDER BY a.code")
    List<Account> findAllActiveOrderByCode();
    
    @Query("SELECT a FROM Account a WHERE a.type = :type AND a.active = true ORDER BY a.code")
    List<Account> findByTypeActiveOrderByCode(@Param("type") AccountType type);
    
    @Query("SELECT a FROM Account a WHERE " +
           "(:code IS NULL OR UPPER(a.code) LIKE UPPER(CONCAT('%', :code, '%'))) AND " +
           "(:name IS NULL OR UPPER(a.name) LIKE UPPER(CONCAT('%', :name, '%'))) AND " +
           "(:type IS NULL OR a.type = :type) AND " +
           "(:active IS NULL OR a.active = :active) " +
           "ORDER BY a.code")
    List<Account> findAccountsWithFilters(@Param("code") String code,
                                         @Param("name") String name,
                                         @Param("type") AccountType type,
                                         @Param("active") Boolean active);
}