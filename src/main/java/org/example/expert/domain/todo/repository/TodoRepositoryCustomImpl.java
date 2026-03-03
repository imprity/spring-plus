package org.example.expert.domain.todo.repository;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.Todo;

import static org.example.expert.domain.todo.entity.QTodo.todo;

@RequiredArgsConstructor
public class TodoRepositoryCustomImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        List<Todo> todos = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetch();

        if (todos.isEmpty()) {
            return Optional.empty();
        }else {
            return Optional.of(todos.get(0));
        }
    }
}
