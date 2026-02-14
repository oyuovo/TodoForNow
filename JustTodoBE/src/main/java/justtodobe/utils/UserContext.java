package justtodobe.utils;

/**
 * 使用 ThreadLocal 在当前请求线程中保存当前登录用户 id，用于业务层按用户筛选数据。
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void clear() {
        USER_ID.remove();
    }
}
