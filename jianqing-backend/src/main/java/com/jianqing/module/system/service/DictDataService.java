package com.jianqing.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jianqing.module.system.dto.DictDataSaveRequest;
import com.jianqing.module.system.dto.DictDataSummary;
import com.jianqing.module.system.dto.DictOptionItem;
import com.jianqing.module.system.entity.SysDictData;

import java.util.List;

public interface DictDataService extends IService<SysDictData> {

    List<DictDataSummary> listDictData(String dictType);

    DictDataSummary createDictData(DictDataSaveRequest request);

    DictDataSummary updateDictData(Long id, DictDataSaveRequest request);

    void deleteDictData(Long id);

    List<DictOptionItem> listEnabledOptions(String dictType);
}
