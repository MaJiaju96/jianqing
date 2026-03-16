package com.jianqing.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jianqing.module.system.dto.DictTypeSaveRequest;
import com.jianqing.module.system.dto.DictTypeSummary;
import com.jianqing.module.system.entity.SysDictType;

import java.util.List;

public interface DictTypeService extends IService<SysDictType> {

    List<DictTypeSummary> listDictTypes();

    DictTypeSummary createDictType(DictTypeSaveRequest request);

    DictTypeSummary updateDictType(Long id, DictTypeSaveRequest request);

    void deleteDictType(Long id);

    boolean existsByDictType(String dictType);
}
