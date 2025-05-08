package com.github.karma.controller;

import com.github.karma.common.Result;
import com.github.karma.dto.DataSource;
import com.github.karma.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/dataSource")
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;


    @PostMapping("/save")
    public Result<Void> saveDataSource(@RequestBody DataSource dataSource) {
        dataSourceService.save(dataSource);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteDataSource(Long id) {
        dataSourceService.delete(id);
        return Result.success();
    }

    @GetMapping("/query")
    public Result<DataSource> queryDataSource(Long id) {
        DataSource dataSource = dataSourceService.query(id);
        return Result.success(dataSource);
    }

    @GetMapping("/queryByCode")
    public Result<DataSource> queryByCode(String dsCode) {
        DataSource dataSource = dataSourceService.queryByCode(dsCode);
        return Result.success(dataSource);
    }

    @GetMapping("/queryByCodes")
    public Result<Map<String, DataSource>> queryByCodes(@RequestParam String dsCodes) {
        List<String> codeList = Arrays.asList(dsCodes.split(","));
        return Result.success(dataSourceService.queryByCodes(codeList));
    }

    @GetMapping("/list")
    public Result<List<DataSource>> listDataSource(String queryStr,String account) {
        return Result.success(dataSourceService.list(queryStr,account));
    }

}
