# SPRING PLUS

# 변경 사항들

## Level 1

### 1. 코드 개선 퀴즈 - @Transactional의 이해 1

``` java
    @Transactional() // 이거 추가
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        ...
    }
    
```

### 2. 코드 추가 퀴즈 - JWT의 이해

``` java
public class User extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
    private String nickname; // 이거를 추가했습니다

    ...
```

그리고 
``` java
public class UserResponse {

    private final Long id;
    private final String email;
    private final String nickname; // 여기도 추가했습니다.

    ...
```

또 JWT에도 정보를 담도록 수정했습니다.
``` java
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            ...

            UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));

            httpRequest.setAttribute("userId", Long.parseLong(claims.getSubject()));
            httpRequest.setAttribute("email", claims.get("email"));
            httpRequest.setAttribute("userRole", claims.get("userRole"));
            httpRequest.setAttribute("nickname", claims.get("nickname")); // 여기 추가
```

그 이후 `User`와 UserResponse의 생성자를 수정하여 컴파일 에러가 난 부분들을 수정했습니다.

### 3. 코드 개선 퀴즈 - JPA의 이해

JPQL을 많이 사용해

- weather
- 수정일 시작
- 수정일 끝

을 기반으로 Todo를 검색할 수 있는 method를 만들었습니다.

``` java
public interface TodoRepository extends JpaRepository<Todo, Long> {

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
```

### 4. 테스트 코드 퀴즈 - 컨트롤러 테스트의 이해

**전**
``` java
    @Test
    void todo_단건_조회_시_todo가_존재하지_않아_예외가_발생한다() throws Exception {
        // given
        long todoId = 1L;

        // when
        when(todoService.getTodo(todoId))
                .thenThrow(new InvalidRequestException("Todo not found"));

        // then
        mockMvc.perform(get("/todos/{todoId}", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.name()))
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Todo not found"));
    }
```

**후**
``` java
    @Test
    void todo_단건_조회_시_todo가_존재하지_않아_예외가_발생한다() throws Exception {
        // given
        long todoId = 1L;

        // when
        when(todoService.getTodo(todoId))
                .thenThrow(new InvalidRequestException("Todo not found"));

        // then
        mockMvc.perform(get("/todos/{todoId}", todoId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Todo not found"));
    }
```

### 5. 코드 개선 퀴즈 - AOP의 이해

**전**
``` java
public class AdminAccessLoggingAspect {

    private final HttpServletRequest request;

    @After("execution(* org.example.expert.domain.user.controller.UserController.getUser(..))")
    public void logAfterChangeUserRole(JoinPoint joinPoint) {
```

**후**
``` java
public class AdminAccessLoggingAspect {

    private final HttpServletRequest request;

    @Before("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public void logAfterChangeUserRole(JoinPoint joinPoint) {
}
```

## Level 2

### 6. JPA Cascade

**전**
``` java
public class Todo extends Timestamped {

    ...

    @OneToMany(mappedBy = "todo")
    private List<Manager> managers = new ArrayList<>();

    ...
```

**후**
``` java
public class Todo extends Timestamped {

    ...

    @OneToMany(mappedBy = "todo", cascade = CascadeType.PERSIST)
    private List<Manager> managers = new ArrayList<>();

    ...
```

### 7. N+1

**전**
``` java
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN c.user WHERE c.todo.id = :todoId")
    List<Comment> findByTodoIdWithUser(@Param("todoId") Long todoId);
}
```

**후**
``` java
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.todo.id = :todoId")
    List<Comment> findByTodoIdWithUser(@Param("todoId") Long todoId);
}
```

### 8. QueryDSL

전에는 JPQL로 작성되어있던 query를
``` java
public interface TodoRepository extends JpaRepository<Todo, Long> {

    ...

    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
```

QueryDSL로 다시 작성했습니다.

``` java
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
```

### 9. Spring Security

``` java
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // .csrf().disable() 방식은 더 이상 사용 안함.
                .csrf(AbstractHttpConfigurer::disable)
                // BasicAuthenticationFilter 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // UsernamePasswordAuthenticationFilter, DefaultLoginPageGeneratingFilter 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtFilter, SecurityContextHolderAwareRequestFilter.class)
                .authorizeHttpRequests(
                        auth ->
                                auth
                                        .requestMatchers("/auth/signup").permitAll()
                                        .requestMatchers("/auth/signin").permitAll()
                                        .requestMatchers("/admin/**").hasAnyAuthority("ADMIN")
                                        .anyRequest().authenticated()
                );

        http.sessionManagement(
                (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
```

Security Config를 생성한뒤

JwtAuthentication이라는 class를 생성

``` java
public class JwtAuthentication implements Authentication, CredentialsContainer {
    ...
}
```

JwtFilter가 이를 사용하도록 수정하였습니다.

``` java
public class JwtFilter implements Filter {

    ...

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        ...

        String jwt = jwtUtil.substringToken(bearerJwt);

        ...

            Long userId = Long.parseLong(claims.getSubject());
            String email = claims.get("email", String.class);
            UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));
            String nickname = claims.get("nickname", String.class);

            AuthUser authUserDetails = new AuthUser(
                    userId,
                    email,
                    userRole,
                    nickname
            );

            SecurityContextHolder.getContext()
                    .setAuthentication(
                            new JwtAuthentication(
                                    jwt, authUserDetails, true));

        ...
}
```