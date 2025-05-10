package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {
    /**
     * 根据分类id查询套餐数量
     * @param categoryId
     * @return
     */
    //category表中的id属性和setmeal表中的category_id属性是外键关系相关联
    //category表中的id属性是主键，setmeal表中的category_id属性是外键
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);
}
