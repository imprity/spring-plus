package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.util.SqlUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoRepositoryCustom {

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    @Query("""
SELECT t from Todo t left join fetch t.user u
where
    coalesce(:weatherPattern, NULL) is NULL or t.weather like %:weatherPattern% escape '#' and
    coalesce(:minModifiedAt, NULL) is NULL or :minModifiedAt < t.modifiedAt and
    coalesce(:maxModifiedAt, NULL) is NULL or t.modifiedAt < :maxModifiedAt
order by
    t.modifiedAt DESC
    """)
    Page<Todo> findAllByWeatherAndModifiedAtImpl(
            @Nullable @Param("weatherPattern") String weatherPattern,
            @Nullable @Param("minModifiedAt") LocalDateTime minModifiedAt,
            @Nullable @Param("maxModifiedAt") LocalDateTime maxModifiedAt,
            Pageable pageable
    );

    default Page<Todo> findAllByWeatherAndModifiedAt(
            @Nullable String weatherPattern,
            @Nullable LocalDateTime minModifiedAt,
            @Nullable LocalDateTime maxModifiedAt,
            Pageable pageable
    ) {
        if (weatherPattern != null) {
            weatherPattern = SqlUtil.escapeStringForLike(weatherPattern, "#");
        }

        return findAllByWeatherAndModifiedAtImpl(
                weatherPattern, minModifiedAt, maxModifiedAt, pageable);
    }
}
