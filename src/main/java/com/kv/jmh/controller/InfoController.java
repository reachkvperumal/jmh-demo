package com.kv.jmh.controller;

import com.kv.jmh.dto.InfoDO;
import com.kv.jmh.service.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class InfoController {

    @Autowired
    private InfoService infoService;

    @GetMapping("/api/list/product")
    public List<InfoDO> getDetails(){
        return infoService.getGadgetInfo();
    }
}
