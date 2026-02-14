package justtodobe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import justtodobe.DTO.LoginDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import justtodobe.DTO.ResultDTO;
import justtodobe.DTO.UserDTO;
import justtodobe.entity.User;
import justtodobe.mapper.UserMapper;
import justtodobe.service.UserService;
import justtodobe.utils.JwtUtil;
import justtodobe.utils.UserContext;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public ResultDTO updatePhoto(String photo) {
        Long userId = UserContext.getUserId();
        if (userId == null) return ResultDTO.fail("用户未登录");
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userId));
        if (user == null) return ResultDTO.fail("用户不存在");
        user.setPhoto(photo);
        int update = userMapper.updateById(user);
        return update > 0 ? ResultDTO.ok() : ResultDTO.fail("更新头像失败");
    }

    @Override
    public ResultDTO getProfile() {
        Long userId = UserContext.getUserId();
        if (userId == null) return ResultDTO.fail("用户未登录");
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userId));
        if (user == null) return ResultDTO.fail("用户不存在");
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setPhoto(user.getPhoto());
        return ResultDTO.ok(userDTO);
    }

    @Override
    public ResultDTO login(LoginDTO loginDTO) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", loginDTO.getId()));
        if (user == null) return ResultDTO.fail("用户id或密码错误");
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return ResultDTO.fail("用户id或密码错误");
        }
        String token = jwtUtil.generateToken(user);
        return ResultDTO.ok(token);
    }

    @Override
    public ResultDTO logout() {
        return ResultDTO.ok();
    }
}
