package justtodobe.service;

import justtodobe.DTO.LoginDTO;
import justtodobe.DTO.ResultDTO;

public interface UserService {
    ResultDTO getProfile();

    ResultDTO updatePhoto(String photo);

    ResultDTO login(LoginDTO loginDTO);

    ResultDTO logout();
}
