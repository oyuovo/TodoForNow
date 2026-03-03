package justtodobe.service;

import justtodobe.DTO.LoginDTO;
import justtodobe.DTO.ResultDTO;

public interface UserService {
    ResultDTO getProfile();

    ResultDTO updatePhoto(String photo);

    ResultDTO login(LoginDTO loginDTO);

    /** 登出并移除 Redis 中的 token；token 可为 null（兼容前端已清 token） */
    ResultDTO logout(String token);
}
