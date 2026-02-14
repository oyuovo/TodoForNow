package justtodobe.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import justtodobe.DTO.ResultDTO;
import justtodobe.entity.User;
import justtodobe.mapper.UserMapper;
import justtodobe.service.MemoPathService;
import justtodobe.utils.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemoPathServiceImpl implements MemoPathService {
    @Resource
    private UserMapper userMapper;
    @Override
    public ResultDTO getMemoPath() {
        Long userId = UserContext.getUserId();
        if (userId == null) return ResultDTO.fail("用户未登录");
        User user = userMapper.selectById(userId);
        if (user == null) return ResultDTO.fail("用户不存在");
        return ResultDTO.ok(user.getMemopath());
    }

    @Override
    @Transactional
    public ResultDTO setMemoPath(String path) {
        Long userId = UserContext.getUserId();
        if (userId == null) return ResultDTO.fail("用户未登录");
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId).set("memopath", path);
        userMapper.update(null, updateWrapper);
        return ResultDTO.ok();
    }
}
