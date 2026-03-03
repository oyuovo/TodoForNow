package justtodobe.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    public ResultDTO updatePhoto(@Valid @RequestBody UpdatePhotoRequest request) {
        String photo = request.getPhoto() != null ? request.getPhoto() : "";
        return userService.updatePhoto(photo);
    }

    @PostMapping("/login")
    public ResultDTO login(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @PostMapping("/logout")
    public ResultDTO logout(HttpServletRequest request) {
        String token = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        return userService.logout(token);
    }
}
