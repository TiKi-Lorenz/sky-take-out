package com.sky.service.impl;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DisMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CategoryServiceimpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DisMapper disMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 新增分类
     *
     * @param categoryDTO
     */
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        // 将categoryDTO中的属性复制到category中,属性拷贝
        BeanUtils.copyProperties(categoryDTO, category);
        //设置分类的状态为禁用状态
        category.setStatus(StatusConstant.DISABLE);
        //设置分类的创建时间和更新时间、创建人和修改人
//        category.setCreateTime(LocalDateTime.now());
//        category.setUpdateTime(LocalDateTime.now());
//        category.setCreateUser(BaseContext.getCurrentId());
//        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.insert(category);
    }
    /**
     * 分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    public PageResult page(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id删除分类
     *
     * @param id
     */
    public void deleteById(Long id) {
        //查询当前分类是否关联了菜品，如果关联了就抛出异常
        Integer disCount = disMapper.countByCategoryId(id);
        if(disCount>0){//如果当前分类下有菜品，那么不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        //查询当前分类下是否关联了套餐，如果关联了就抛出异常
        Integer setmealCount = setmealMapper.countByCategoryId(id);
        if(setmealCount>0){ //如果当前分类下有套餐，那么不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        categoryMapper.deleteById(id);
        //删除分类数据
    }

    /**
     * 修改分类
     *
     * @param categoryDTO
     */
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        // 将categoryDTO中的属性复制到category中,属性拷贝
        BeanUtils.copyProperties(categoryDTO, category);
        //设置分类的更新时间、修改人
//        category.setUpdateTime(LocalDateTime.now());
//        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.update(category);
    }

    /**
     * 启用禁用分类
     *
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        //把要修改的id、状态封装到Category对象中，包括更新时间、修改人
        //因为mapper层接受的参数是Category对象，所以这里要封装成Category对象
        Category category = Category.builder()
                .id(id)
                .status(status)
                //.updateTime(LocalDateTime.now())
                //.updateUser(BaseContext.getCurrentId())
                .build();
        categoryMapper.update(category);
    }
    /**
     * 根据类型查询分类
     *
     * @param type
     * @return
     */
    public List<Category> list(Integer type) {
        //根据类型查询分类
        //如果type为null，查询所有分类
        CategoryPageQueryDTO categoryPageQueryDTO = CategoryPageQueryDTO.builder()
                .type(type)
                .pageSize(10) //每页显示10条数据
                .page(1) //当前页码
                .build();
        return categoryMapper.pageQuery(categoryPageQueryDTO);
    }
}
