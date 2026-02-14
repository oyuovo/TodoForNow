package justtodobe.controller;

import jakarta.annotation.Resource;
import justtodobe.DTO.LoginDTO;
import justtodobe.DTO.ResultDTO;
import justtodobe.DTO.UpdatePhotoRequest;
import justtodobe.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping("/profile")
    public ResultDTO getProfile() {
        return userService.getProfile();
    }

    @PatchMapping("/profile/photo")
    public ResultDTO updatePhoto(@RequestBody UpdatePhotoRequest request) {
        String photo = request != null && request.getPhoto() != null ? request.getPhoto() : "";
        return userService.updatePhoto(photo);
    }

    @PostMapping("/login")
    public ResultDTO login(@RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @PostMapping("/logout")
    public ResultDTO logout() {
        return userService.logout();
    }
}
