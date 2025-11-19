package com.ticket.member.repository;

import com.ticket.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("select case when count(m) > 0 then true else false end from Member m where m.email.email = :email")
    boolean existsByEmailAddress(String email);
}
