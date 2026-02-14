package justtodobe.controller;

import jakarta.annotation.Resource;
import justtodobe.DTO.ResultDTO;
import justtodobe.DTO.SetMemoPathRequest;
import justtodobe.service.MemoPathService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
public class MemoController {
    @Resource
    private MemoPathService memoPathService;

    @GetMapping("/memo-path")
    public ResultDTO getMemoPath() {
        return memoPathService.getMemoPath();
    }

    @PostMapping("/memo-path")
    public ResultDTO setMemoPath(@RequestBody SetMemoPathRequest request) {
        String path = request != null && request.getMemopath() != null ? request.getMemopath() : "";
        return memoPathService.setMemoPath(path);
    }
}
