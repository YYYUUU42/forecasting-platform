package org.example.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GeneralListener<T> extends AnalysisEventListener<T> {
 
    private List<T> list= Lists.newArrayList();
 
    @Override
    public void invoke(T data, AnalysisContext context) {
        Assert.notNull(data,"导入数据不能为null");
        log.info("start read list of data :{}", JSON.toJSONString(data));
        list.add(data);
    }
 
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("read data complete！");
    }
 
    public List<T> getList() {
        return list;
    }
}