package com.neo.omen.mapper;

import com.neo.omen.domain.User;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface UserMapper {

    @Select("select * from user")
    List<User> getList();



}
