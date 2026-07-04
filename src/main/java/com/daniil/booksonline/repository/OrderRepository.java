package com.daniil.booksonline.repository;

import com.daniil.booksonline.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select distinct o from Order o left join fetch o.items i left join fetch i.product where o.id = :id")
    Optional<Order> findByIdWithItemsAndProducts(@Param("id") Long id);

    @Query("select distinct o from Order o left join fetch o.items i left join fetch i.product order by o.createdAt desc")
    List<Order> findAllWithItemsAndProducts();
}

