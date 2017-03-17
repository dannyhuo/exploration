package com.study.mybatis.mapper;

import java.util.List;
import java.util.Map;

import com.study.mybatis.model.Customer;
import com.study.mybatis.model.vo.CustomerVo;


public interface CustomerMapper {
    int deleteByPrimaryKey(Long customerId);

    int insert(Customer record);

    int insertSelective(Customer record);

    Customer selectByPrimaryKey(Long customerId);

    int updateByPrimaryKeySelective(Customer record);

    int updateByPrimaryKey(Customer record);
    
    List<CustomerVo> querySelective(Map<String, Object> params);
}