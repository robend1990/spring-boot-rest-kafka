package com.faceit.assessment.user.domain;

import com.faceit.assessment.user.dto.UserSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

class UserCustomRepositoryImpl implements UserCustomRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public Page<User> findByCriteria(UserSearchRequest userSearchRequest) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<User> usersQuery = cb.createQuery(User.class);
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

        List<Predicate> predicates = new ArrayList<>();
        Root<User> user = usersQuery.from(User.class);
        user.alias("alias1");
        String firstName = userSearchRequest.getFirst_name();
        String lastName = userSearchRequest.getLast_name();
        String country = userSearchRequest.getCountry();
        String email = userSearchRequest.getEmail();

        if (firstName != null) {
            System.out.println("adding first name");
            predicates.add(cb.equal(user.get("firstName"), firstName));
        }
        if (lastName != null) {
            System.out.println("adding last name");
            predicates.add(cb.equal(user.get("lastName"), lastName));
        }
        if (country != null) {
            System.out.println("adding country");
            predicates.add(cb.equal(user.get("country"), country));
        }
        if (email != null) {
            System.out.println("adding email");
            predicates.add(cb.equal(user.get("email"), email));
        }

        Predicate[] finalPredicates = new Predicate[predicates.size()];
        predicates.toArray(finalPredicates);
        usersQuery.where(finalPredicates);


        Root<User> countRoot = countQuery.from(User.class);
        countRoot.alias("alias1");
        countQuery.select(cb.count(countRoot));
        countQuery.where(finalPredicates);
        Long usersCount = entityManager.createQuery(countQuery).getSingleResult();

        TypedQuery<User> query = entityManager.createQuery(usersQuery);
        query.setFirstResult(userSearchRequest.getSize() * userSearchRequest.getPage());
        query.setMaxResults(userSearchRequest.getSize());
        List<User> resultList = query.getResultList();
        return new PageImpl<User>(resultList, PageRequest.of(userSearchRequest.getPage(), userSearchRequest.getSize()), usersCount);
    }
}
