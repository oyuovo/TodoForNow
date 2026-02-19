package justtodobe.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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
    public ResultDTO setMemoPath(@Valid @RequestBody SetMemoPathRequest request) {
        return memoPathService.setMemoPath(request.getMemopath());
    }
}
