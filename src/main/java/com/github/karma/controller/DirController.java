package com.github.karma.controller;

import com.github.karma.common.Result;
import com.github.karma.dto.Dir;
import com.github.karma.service.DirService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 目录 前端控制器
 * </p>
 *
 * @author junming.ljm
 * @since 2025-01-26
 */
@RestController
@RequestMapping("/api/dir")
public class DirController {

    @Autowired
    private DirService dirService;

    /**
     * 创建目录
     * @param dir
     * @return
     */
    @PostMapping("/save")
    public Result<Void> saveDir(@RequestBody Dir dir) {
        dirService.saveDir(dir);
        return Result.success();
    }

    /**
     * 查询目录树
     * @param account
     * @param id
     * @param name
     * @return
     */
    @GetMapping("/list")
    public List<Dir> listDir(@RequestParam(value = "account") String account,
                                     @RequestParam(value = "id", required = false) Long id,
                                     @RequestParam(value = "name", required = false) String name) {
        return dirService.listDir(account, id, name);
    }

    /**
     * 删除目录（不能删除非空目录）
     * @param id
     */
    @PostMapping("/delete")
    public Result<Void> deleteDir(@RequestParam("id") Long id) {
        dirService.deleteDir(id);
        return Result.success();
    }

    /**
     * 重命名
     * @param dir
     * @return
     */
    @PostMapping("/rename")
    public Result<Void> renameDir(@RequestBody Dir dir) {
        dirService.renameDir(dir.getId(), dir.getName());
        return Result.success();
    }

}
